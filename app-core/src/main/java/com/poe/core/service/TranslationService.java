package com.poe.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poe.cache.dao.TranslationDao;
import com.poe.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 英文→中文翻译服务。
 * <p>
 * 翻译优先级：
 * <ol>
 *   <li>用户自定义翻译（内存 Map，运行时添加）</li>
 *   <li>SQLite 翻译表（含 PoeCharm2 导入数据）</li>
 *   <li>原文（未找到翻译时直接返回原文）</li>
 * </ol>
 * <p>
 * 翻译领域：item / skill / passive / mod / map。
 * <p>
 * 缺失翻译会通过日志记录，可通过 {@link #getMissingTranslations()} 查询。
 */
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /** PoeCharm2 子模块相对路径 */
    private static final String POECHARM2_PATH = "poecharm2";

    private final TranslationDao translationDao;

    /** 用户自定义翻译，领域 → (源文本 → 翻译) */
    private final Map<String, Map<String, String>> customTranslations = new ConcurrentHashMap<>();

    /** 缺失翻译记录（source → domain 集合），用于日志和后续补充 */
    private final Set<String> missingTranslations = ConcurrentHashMap.newKeySet();

    public TranslationService(TranslationDao translationDao) {
        this.translationDao = translationDao;
    }

    // ── 翻译查询 ──

    /**
     * 单条翻译。优先级：自定义翻译 → DB → 原文。
     *
     * @param source 英文原文
     * @param domain 翻译领域（item/skill/passive/mod/map）
     * @return 中文翻译，未找到时返回原文
     */
    public String translate(String source, String domain) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        // 1. 查用户自定义翻译
        String custom = getCustomTranslation(source, domain);
        if (custom != null) {
            return custom;
        }
        // 2. 查 SQLite 翻译表
        Optional<String> result = translationDao.translate(source, domain);
        if (result.isPresent()) {
            return result.get();
        }
        // 3. 未找到 → 记录缺失并返回原文
        recordMissing(source, domain);
        return source;
    }

    /**
     * 批量翻译。自定义翻译覆盖 DB 结果。
     *
     * @param sources 源文本列表
     * @param domain  领域
     * @return 源文本 → 翻译文本 的映射，仅包含找到翻译的条目
     */
    public Map<String, String> batchTranslate(List<String> sources, String domain) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }
        // 1. 批量查 DB
        Map<String, String> results = new HashMap<>(translationDao.batchTranslate(sources, domain));

        // 2. 自定义翻译覆盖 DB 结果
        Map<String, String> domainCustom = customTranslations.getOrDefault(domain, Collections.emptyMap());
        for (String source : sources) {
            if (domainCustom.containsKey(source)) {
                results.put(source, domainCustom.get(source));
            }
        }

        // 3. 记录缺失
        Set<String> found = results.keySet();
        for (String source : sources) {
            if (!found.contains(source)) {
                recordMissing(source, domain);
            }
        }

        return results;
    }

    // ── 自定义翻译管理 ──

    /**
     * 添加用户自定义翻译（内存运行时，优先级高于 DB）。
     * <p>
     * 自定义翻译仅保存在内存中，不写入 DB，以保证与内置翻译数据隔离。
     * 如需持久化到 DB，请使用 {@link #importTranslations(Map, String)}。
     *
     * @param source 英文原文
     * @param target 中文翻译
     * @param domain 领域
     */
    public void addCustomTranslation(String source, String target, String domain) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(target) || StringUtils.isBlank(domain)) {
            return;
        }
        customTranslations
            .computeIfAbsent(domain, k -> new ConcurrentHashMap<>())
            .put(source, target);
        log.debug("Custom translation added: [{}] {} -> {}", domain, source, target);
    }

    /**
     * 移除用户自定义翻译。
     *
     * @param source 英文原文
     * @param domain 领域
     */
    public void removeCustomTranslation(String source, String domain) {
        Map<String, String> domainCustom = customTranslations.get(domain);
        if (domainCustom != null) {
            domainCustom.remove(source);
            log.debug("Custom translation removed: [{}] {}", domain, source);
        }
    }

    /**
     * 获取指定领域的自定义翻译数量。
     */
    public int getCustomTranslationCount(String domain) {
        Map<String, String> domainCustom = customTranslations.get(domain);
        return domainCustom != null ? domainCustom.size() : 0;
    }

    /**
     * 清空指定领域的自定义翻译。
     */
    public void clearCustomTranslations(String domain) {
        Map<String, String> domainCustom = customTranslations.get(domain);
        if (domainCustom != null) {
            domainCustom.clear();
        }
    }

    // ── 数据导入 ──

    /**
     * 导入翻译数据（直接写入 DB，不覆盖运行时自定义翻译）。
     *
     * @param translations 源文本 → 翻译文本 的映射
     * @param domain       领域
     */
    public void importTranslations(Map<String, String> translations, String domain) {
        if (translations == null || translations.isEmpty()) {
            return;
        }
        translationDao.batchSave(translations, domain);
        log.info("Imported {} translations for domain {}", translations.size(), domain);
    }

    /**
     * 导入社区翻译包（JSON 格式）。
     * <pre>
     * {
     *   "domain": "item",
     *   "version": "3.24",
     *   "entries": {
     *     "Mageblood": "法师之血",
     *     "Headhunter": "猎首"
     *   }
     * }
     * </pre>
     *
     * @param jsonContent JSON 字符串
     * @return 导入条目数，格式无效时返回 0
     */
    public int importTranslationPack(String jsonContent) {
        if (StringUtils.isBlank(jsonContent)) {
            return 0;
        }
        try {
            JsonNode root = mapper.readTree(jsonContent);
            if (root == null) {
                return 0;
            }
            String domain = root.has("domain") ? root.get("domain").asText() : null;
            String version = root.has("version") ? root.get("version").asText() : null;
            JsonNode entries = root.get("entries");

            if (domain == null || entries == null || !entries.isObject()) {
                log.warn("Invalid translation pack format: missing domain or entries");
                return 0;
            }

            Map<String, String> translations = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = entries.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                translations.put(field.getKey(), field.getValue().asText());
            }

            if (!translations.isEmpty()) {
                importTranslations(translations, domain);
                log.info("Imported translation pack: domain={} version={} count={}",
                    domain, version, translations.size());
            }
            return translations.size();
        } catch (Exception e) {
            log.warn("Failed to parse translation pack JSON: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 导入翻译包文件。
     *
     * @param filePath JSON 文件路径
     * @return 导入条目数
     */
    public int importTranslationPackFile(String filePath) {
        try {
            String content = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            return importTranslationPack(content);
        } catch (Exception e) {
            log.error("Failed to read translation pack file: {}", filePath, e);
            throw new RuntimeException("Failed to import translation pack from file: " + filePath, e);
        }
    }

    /**
     * 从 PoeCharm2 子模块导入基础翻译数据。
     * <p>
     * 扫描 {@code poecharm2/src/main/resources/} 目录下的 JSON 翻译包文件，
     * 逐文件导入 translations 表。
     *
     * @return 导入的总条目数
     */
    public int importFromPoeCharm2() {
        int total = 0;
        Path poecharm2Dir = Paths.get(POECHARM2_PATH);
        Path resourcesDir = poecharm2Dir.resolve("src/main/resources");

        if (!Files.exists(resourcesDir)) {
            log.info("PoeCharm2 resources directory not found: {}", resourcesDir.toAbsolutePath());
            return 0;
        }

        try (Stream<Path> files = Files.list(resourcesDir)) {
            List<Path> jsonFiles = files
                .filter(p -> p.toString().endsWith(".json"))
                .collect(Collectors.toList());

            for (Path file : jsonFiles) {
                try {
                    int count = importTranslationPackFile(file.toString());
                    total += count;
                } catch (Exception e) {
                    log.warn("Failed to import translation from {}: {}", file, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan PoeCharm2 resources", e);
        }

        log.info("PoeCharm2 import complete: {} translations imported", total);
        return total;
    }

    // ── 缺失翻译跟踪 ──

    /**
     * 获取缺失翻译的源文本列表。
     *
     * @return 不可变的缺失翻译列表
     */
    public List<String> getMissingTranslations() {
        return List.copyOf(missingTranslations);
    }

    /**
     * 清空缺失翻译记录。
     */
    public void clearMissingTranslations() {
        missingTranslations.clear();
    }

    // ── 内部辅助方法 ──

    /**
     * 从自定义翻译 Map 中查找指定领域下指定源文本的翻译。
     *
     * @return 翻译文本，未找到时返回 null
     */
    private String getCustomTranslation(String source, String domain) {
        Map<String, String> domainCustom = customTranslations.get(domain);
        return domainCustom != null ? domainCustom.get(source) : null;
    }

    /**
     * 记录缺失翻译（source||domain 作为 key 去重）。
     */
    private void recordMissing(String source, String domain) {
        String key = domain + "||" + source;
        if (missingTranslations.add(key)) {
            log.info("Translation missing: [{}] {}", domain, source);
        }
    }
}

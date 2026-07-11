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
     * 从 PoeCharm2 子模块导入基础翻译数据（默认路径）。
     * <p>
     * 扫描 {@code poecharm2/Data/Translate/zh-rCN/} 目录下的 CSV 翻译文件，
     * 解析 {@code "English",中文} 格式，按文件名映射到翻译领域并导入 translations 表。
     *
     * @return 导入的总条目数
     */
    public int importFromPoeCharm2() {
        Path translateDir = Paths.get(POECHARM2_PATH, "Data", "Translate", "zh-rCN");
        return importFromPoeCharm2(translateDir);
    }

    /**
     * 从 PoeCharm2 翻译目录导入基础翻译数据（可指定路径，便于测试）。
     *
     * @param translateDir PoeCharm2 zh-rCN 翻译数据目录
     * @return 导入的总条目数
     */
    public int importFromPoeCharm2(Path translateDir) {
        if (!Files.exists(translateDir) || !Files.isDirectory(translateDir)) {
            log.info("PoeCharm2 translate directory not found: {}", translateDir.toAbsolutePath());
            return 0;
        }

        int total = 0;
        try (Stream<Path> files = Files.list(translateDir)) {
            List<Path> csvFiles = files
                .filter(p -> p.toString().endsWith(".csv"))
                .collect(Collectors.toList());

            for (Path file : csvFiles) {
                try {
                    String filename = file.getFileName().toString();
                    String domain = mapFilenameToDomain(filename);
                    Map<String, String> entries = parsePoeCharm2Csv(file);
                    if (!entries.isEmpty()) {
                        importTranslations(entries, domain);
                        total += entries.size();
                        log.debug("PoeCharm2: imported {} entries from {} (domain={})",
                            entries.size(), filename, domain);
                    }
                } catch (Exception e) {
                    log.warn("Failed to import PoeCharm2 CSV {}: {}", file.getFileName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan PoeCharm2 translate directory", e);
        }

        log.info("PoeCharm2 import complete: {} translations imported from {}", total, translateDir);
        return total;
    }

    /**
     * 解析 PoeCharm2 CSV 翻译文件。
     * <p>
     * CSV 格式：{@code "English Name",中文译名} 或 {@code EnglishName,中文译名}。
     * 跳过空行和注释行。
     *
     * @param csvFile CSV 文件路径
     * @return 英文 → 中文 翻译映射
     */
    static Map<String, String> parsePoeCharm2Csv(Path csvFile) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                // 格式1: "English Name",中文译名
                // 格式2: EnglishName,中文译名
                String[] parts = parseCsvLine(line);
                if (parts != null && parts.length == 2) {
                    String source = parts[0].trim();
                    String target = parts[1].trim();
                    if (!source.isEmpty() && !target.isEmpty()) {
                        result.put(source, target);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PoeCharm2 CSV: " + csvFile, e);
        }
        return result;
    }

    /**
     * 解析单行 CSV：处理引号包裹的字段。
     * <p>
     * 支持格式：
     * <ul>
     *   <li>{@code "English",中文} → ["English", "中文"]</li>
     *   <li>{@code "English, text",中文} → ["English, text", "中文"]</li>
     *   <li>{@code English,中文} → ["English", "中文"]</li>
     * </ul>
     *
     * @param line 原始行
     * @return [source, target] 或 null
     */
    private static String[] parseCsvLine(String line) {
        // 引号包裹的字段（如 "Blue Pearl Amulet",碧珠护身符）
        if (line.startsWith("\"")) {
            int endQuote = findClosingQuote(line, 1);
            if (endQuote < 0) {
                return null;
            }
            String source = line.substring(1, endQuote);
            int commaIdx = line.indexOf(',', endQuote);
            if (commaIdx < 0) {
                return null;
            }
            // 去掉目标字段可能的引号
            String target = stripQuotes(line.substring(commaIdx + 1).trim());
            return new String[]{source, target};
        }

        // 无引号简单格式（如 Andvarius,贪欲之记）
        int commaIdx = line.indexOf(',');
        if (commaIdx < 0) {
            return null;
        }
        String source = stripQuotes(line.substring(0, commaIdx).trim());
        String target = stripQuotes(line.substring(commaIdx + 1).trim());
        return new String[]{source, target};
    }

    /** 查找闭合引号的位置，处理转义引号 "" */
    private static int findClosingQuote(String line, int start) {
        for (int i = start; i < line.length(); i++) {
            if (line.charAt(i) == '"') {
                // 检查是否是转义引号（两个连续引号）
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    i++; // 跳过转义引号
                    continue;
                }
                return i;
            }
        }
        return -1;
    }

    /** 去除首尾引号 */
    private static String stripQuotes(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * 根据 PoeCharm2 CSV 文件名映射到翻译领域。
     * <p>按具体程度从高到低匹配，避免 {@code items_*} 通配过早命中。
     */
    static String mapFilenameToDomain(String filename) {
        String lower = filename.toLowerCase();
        // 技能宝石
        if (lower.startsWith("items_gems") || lower.startsWith("gems_")) {
            return "skill";
        }
        // 被动技能树
        if (lower.startsWith("passivetree") || lower.startsWith("tree_")) {
            return "passive";
        }
        // 词缀 / 属性描述 / 怪物
        if (lower.startsWith("statdescriptions") || lower.startsWith("modmap")
            || lower.startsWith("query_mod") || lower.startsWith("monsters")
            || lower.startsWith("minions")) {
            return "mod";
        }
        // 物品（Items_*.csv / Uniques_*.csv）
        if (lower.startsWith("items_") || lower.startsWith("uniques")) {
            return "item";
        }
        // 默认归为 item
        return "item";
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

package com.poe.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poe.cache.dao.ItemDao;
import com.poe.cache.dao.SearchDao;
import com.poe.cache.model.Item;
import com.poe.cache.model.ItemSummary;
import com.poe.common.util.StringUtils;
import com.poe.core.model.ItemDetail;
import com.poe.core.model.ModLine;
import com.poe.core.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 物品搜索服务，基于 FTS5 全文搜索实现中英文混合搜索和分类过滤。
 * <p>
 * 搜索流程：
 * <ol>
 *   <li>使用 {@link SearchDao} 执行 FTS5 全文搜索</li>
 *   <li>统计匹配总数并计算分页信息</li>
 *   <li>封装为 {@link SearchResult} 返回</li>
 * </ol>
 * <p>
 * 详情查询从 {@link ItemDao} 获取完整物品数据，并解析 JSON 字段
 * 返回结构化的 {@link ItemDetail}。
 */
public class ItemSearchService {

    private static final Logger log = LoggerFactory.getLogger(ItemSearchService.class);
    /** 共享 JSON 解析器实例 */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** FTS5 全文搜索 DAO */
    private final SearchDao searchDao;
    /** 物品数据访问对象 */
    private final ItemDao itemDao;
    /** 翻译服务，用于详情页翻译 */
    private final TranslationService translationService;

    /**
     * @param searchDao          FTS5 全文搜索 DAO
     * @param itemDao            物品数据访问对象
     * @param translationService 翻译服务
     */
    public ItemSearchService(SearchDao searchDao, ItemDao itemDao,
                             TranslationService translationService) {
        this.searchDao = searchDao;
        this.itemDao = itemDao;
        this.translationService = translationService;
    }

    /**
     * 搜索物品。
     *
     * @param keyword   搜索关键词（中英文均可），为 null 或空白时返回空结果
     * @param itemClass 物品类别过滤，null 表示不过滤
     * @param page      页码（从 1 开始）
     * @param pageSize  每页数量
     * @return 分页搜索结果
     */
    public SearchResult<ItemSummary> search(String keyword, String itemClass,
                                             int page, int pageSize) {
        if (StringUtils.isBlank(keyword)) {
            log.debug("Empty keyword, returning empty result");
            return new SearchResult<>(Collections.emptyList(), 0, page, pageSize);
        }
        if (page < 1) {
            throw new IllegalArgumentException("Page must be >= 1, got: " + page);
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be >= 1, got: " + pageSize);
        }

        String normalizedKeyword = StringUtils.normalizeName(keyword);
        int offset = (page - 1) * pageSize;

        List<ItemSummary> items = searchDao.searchItems(normalizedKeyword, itemClass, pageSize, offset);
        int total = searchDao.searchItemCount(normalizedKeyword, itemClass);

        log.debug("Search '{}' class={} page={} size={} → {} results (total={})",
            keyword, itemClass, page, pageSize, items.size(), total);

        return new SearchResult<>(items, total, page, pageSize);
    }

    /**
     * 搜索物品（无类别过滤）。便捷方法。
     *
     * @param keyword  搜索关键词
     * @param page     页码（从 1 开始）
     * @param pageSize 每页数量
     * @return 分页搜索结果
     */
    public SearchResult<ItemSummary> search(String keyword, int page, int pageSize) {
        return search(keyword, null, page, pageSize);
    }

    /**
     * 获取物品完整详情。
     *
     * @param itemId 物品 ID
     * @return 物品详情，物品不存在时返回 null
     */
    public ItemDetail getItemDetail(int itemId) {
        Optional<Item> opt = itemDao.findById(itemId);
        if (opt.isEmpty()) {
            log.debug("Item not found: id={}", itemId);
            return null;
        }
        Item item = opt.get();

        ItemSummary summary = new ItemSummary(
            item.getId(),
            item.getName(),
            item.getNameZh(),
            item.getItemClass(),
            item.getDropLevel(),
            item.getWikiUrl(),
            0.0
        );

        List<String> implicits = parseImplicits(item.getImplicits());
        Map<String, Integer> requirements = parseRequirements(item.getRequirements());

        return new ItemDetail(
            summary,
            implicits,
            requirements,
            Collections.emptyList(), // explicit mods not available from base_items
            item.getFlavourText(),
            item.getWikiUrl()
        );
    }

    /**
     * 解析 implicits JSON 字段为字符串列表。
     * JSON 格式：[{"text":"+20 to maximum Life"}, ...]
     *
     * @param implicitsJson 基底词缀 JSON 字符串
     * @return 词缀文本列表，JSON 无效时返回空列表
     */
    List<String> parseImplicits(String implicitsJson) {
        if (StringUtils.isBlank(implicitsJson)) {
            return Collections.emptyList();
        }
        try {
            JsonNode array = mapper.readTree(implicitsJson);
            if (!array.isArray()) {
                return Collections.emptyList();
            }
            return StreamSupport.stream(array.spliterator(), false)
                .filter(node -> node.has("text"))
                .map(node -> node.get("text").asText())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to parse implicits JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 解析 requirements JSON 字段为属性名→值的映射。
     * JSON 格式：[{"name":"str","values":[["100"]]}, ...]
     *
     * @param requirementsJson 属性需求 JSON 字符串
     * @return 属性名（str/dex/int）→ 需求值的映射，JSON 无效时返回空 Map
     */
    Map<String, Integer> parseRequirements(String requirementsJson) {
        if (StringUtils.isBlank(requirementsJson)) {
            return Collections.emptyMap();
        }
        try {
            JsonNode array = mapper.readTree(requirementsJson);
            if (!array.isArray()) {
                return Collections.emptyMap();
            }
            Map<String, Integer> result = new LinkedHashMap<>();
            for (JsonNode node : array) {
                if (!node.has("name") || !node.has("values")) {
                    continue;
                }
                String name = node.get("name").asText();
                JsonNode values = node.get("values");
                if (values.isArray() && values.size() > 0) {
                    JsonNode inner = values.get(0);
                    if (inner.isArray() && inner.size() > 0) {
                        try {
                            result.put(name, inner.get(0).asInt());
                        } catch (NumberFormatException e) {
                            log.debug("Non-integer requirement value for {}: {}", name, inner.get(0));
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse requirements JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}

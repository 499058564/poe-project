# T-035a RAG 检索引擎

**版本**：v1.0.0  
**模块**：`app-core`  
**预估**：1 天  
**前置**：T-013, T-032a  
**状态**：待开始

---

## 任务描述

实现基于检索增强生成(RAG)的问答引擎：意图识别 → 多源检索 → 上下文组装 → AI 生成回答。

## 详细步骤

### 1. RagService

```java
public class RagService {
    private final AiProvider aiProvider;
    private final ItemSearchService itemService;
    private final WikiApiClient wikiClient;
    private final PassiveTreeService treeService;
    private final TranslationService translationService;

    /** 完整 RAG 问答 */
    public RagAnswer ask(String question) {
        // 1. 意图识别
        QueryIntent intent = classifyIntent(question);
        
        // 2. 多源检索（根据意图选择数据源）
        List<SearchResult> context = retrieve(intent, question);
        
        // 3. 构建 Prompt（system + context + question）
        String prompt = buildPrompt(intent, context, question);
        
        // 4. AI 生成回答
        String answer = aiProvider.chat(prompt, question);
        
        // 5. 返回结构化结果（回答 + 引用）
        return new RagAnswer(answer, context);
    }

    /** 流式问答 */
    public void askStream(String question, Consumer<String> onChunk, ...) { ... }
}
```

### 2. 意图识别

```java
enum QueryIntent {
    ITEM_INFO,          // "法师之血有什么效果？"
    SKILL_MECHANICS,    // "三位一体辅助宝石怎么触发？"
    BOSS_GUIDE,         // "希鲁斯怎么打？"
    CRAFTING,           // "怎么做法师弓？"
    LEAGUE_MECHANICS,   // "圣所机制是什么？"
    BUILD_ADVICE,       // "冰霜射击适合开荒吗？"
    GENERAL             // 其他
}

private QueryIntent classifyIntent(String question) {
    // 方案：关键词匹配 + 简单规则
    // 后续可升级为轻量级分类模型
    if (containsAny(question, "效果", "属性", "是什么", "怎么用")) return ITEM_INFO;
    if (containsAny(question, "辅助", "连線", "技能石")) return SKILL_MECHANICS;
    // ...
    return GENERAL;
}
```

### 3. 多源检索

```java
private List<SearchResult> retrieve(QueryIntent intent, String question) {
    List<SearchResult> results = new ArrayList<>();
    
    switch (intent) {
        case ITEM_INFO -> {
            // SQLite FTS 搜索物品
            results.addAll(itemService.search(extractItemName(question)));
        }
        case SKILL_MECHANICS -> {
            // SQLite 搜索技能 + Wiki 搜索机制页面
            results.addAll(itemService.searchSkills(question));
            results.addAll(wikiClient.searchPages(question, 3));
        }
        case BOSS_GUIDE, LEAGUE_MECHANICS -> {
            // 主要靠 Wiki 搜索
            results.addAll(wikiClient.searchPages(question, 5));
        }
        case BUILD_ADVICE -> {
            // 物品搜索 + 天赋搜索 + Wiki
            results.addAll(itemService.search(question));
            results.addAll(treeService.searchNodes(question));
        }
        default -> {
            results.addAll(itemService.search(question));
            results.addAll(wikiClient.searchPages(question, 3));
        }
    }
    
    return results;
}
```

### 4. Prompt 构建

```java
private String buildPrompt(QueryIntent intent, List<SearchResult> context, String question) {
    return """
        你是一个流放之路(Path of Exile)的游戏助手。
        请根据以下参考信息回答用户问题。如果参考信息不足，请诚实说明。
        
        【参考信息】
        %s
        
        【回答要求】
        - 准确引用游戏机制
        - 如果涉及装备，请说明具体数值范围
        - 如果是传奇装备，说明其核心机制
        - 回答简洁，重点突出
        
        【用户问题】
        %s
        """.formatted(formatContext(context), question);
}
```

### 5. RagAnswer

```java
class RagAnswer {
    String answer;
    List<SourceReference> sources;   // 引用来源
}

class SourceReference {
    String title;        // "PoE Wiki - Mageblood"
    String snippet;      // 摘要
    String url;          // 来源链接
    SourceType type;     // WIKI / DATABASE
}
```

## 验收标准

- [ ] 意图识别正确率 > 80%（常见问题类型）
- [ ] 检索结果与问题相关
- [ ] AI 回答基于检索到的上下文
- [ ] 引用来源正确标注
- [ ] 无相关数据时 AI 诚实回答"不知道"

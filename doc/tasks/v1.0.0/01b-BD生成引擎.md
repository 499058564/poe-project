# T-032b BD 生成引擎

**版本**：v1.0.0  
**模块**：`app-core`  
**预估**：1 天  
**前置**：T-032a  
**状态**：待开始

---

## 任务描述

实现 BuildGenerator，将用户偏好转换为结构化 BD 方案。

## 详细步骤

### 1. BuildPreferences

```java
public class BuildPreferences {
    private String ascendancy;       // 升华职业（可选，让 AI 推荐）
    private String mainSkill;        // 主技能（可选）
    private String playStyle;        // "melee"/"ranged"/"spell"/"minion"/"mine"
    private String budget;           // "league_start"/"mid"/"high"/"mirror"
    private boolean hardcore;
    private String league;
    private String additionalNotes;  // 自由文本
}
```

### 2. BuildGenerator

```java
public class BuildGenerator {
    private final AiProvider aiProvider;
    private final ItemSearchService itemService;
    private final PassiveTreeService treeService;

    /**
     * 生成 BD 方案
     * @return 结构化的 Build 推荐
     */
    public BuildRecommendation generate(BuildPreferences prefs) {
        // 1. 构建 system prompt（包含游戏版本、meta 信息）
        String systemPrompt = buildSystemPrompt();
        
        // 2. 构建 user message（用户偏好描述）
        String userMessage = buildUserMessage(prefs);
        
        // 3. 调用 AI 生成
        String aiResponse = aiProvider.chat(systemPrompt, userMessage);
        
        // 4. 解析 JSON 响应为 BuildRecommendation
        return parseResponse(aiResponse);
    }

    /** 流式生成（用于 UI 逐字显示） */
    public void generateStream(BuildPreferences prefs, 
                               Consumer<BuildRecommendation> onComplete) {
        // 流式接收 AI 输出，完成后解析 JSON
    }
}
```

### 3. BuildRecommendation（AI 输出结构）

```java
public class BuildRecommendation {
    String buildName;              // "龙卷射击-锐眼"
    String ascendancy;             // "Deadeye"
    String summary;                // 一句话概述
    List<String> pros;             // 优点
    List<String> cons;             // 缺点
    String estimatedBudget;        // 估算造价
    
    // 天赋树
    String treeUrl;                // POB 链接
    List<String> keyNodes;         // 关键节点描述
    String treeStrategy;           // 路线策略文字说明
    
    // 装备
    List<RecommendedItem> coreItems;      // 核心装备
    List<RecommendedItem> budgetItems;    // 廉价替代
    
    // 技能
    List<SkillLinkGroup> skillLinks;      // 技能连線方案
    
    // 防御
    List<String> defenseLayers;           // 防御机制
    
    // 升级
    String levelingGuide;                 // 升级指南
    
    // 生成元数据
    String aiModel;
    String generatedAt;
}
```

### 4. 校验与后处理

```java
// 校验 AI 输出：
// - 推荐装备是否在数据中存在（通过 ItemSearchService 校验）
// - 天赋树节点是否存在
// - 技能宝石是否合法
// 不合法项标记 [AI推测] 标签
```

## 验收标准

- [ ] 输入偏好 → 生成结构化 BD 方案
- [ ] 推荐装备可被 ItemSearchService 查询到
- [ ] 推荐天赋节点在天赋树中存在
- [ ] 流式生成正常（逐 chunk 解析）
- [ ] JSON 解析失败时有降级处理

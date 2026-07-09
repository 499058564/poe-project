# T-023 实现 POB XML 解析器

**版本**：v0.4.0  
**模块**：`pob-adapter`  
**预估**：1 天  
**前置**：T-022  
**状态**：待开始

---

## 任务描述

实现 POB Build XML 的双向解析：XML → Java VO 和 Java VO → XML。

## 详细步骤

### 1. BuildParser

```java
public class BuildParser {
    
    /** 解析 POB 导出的 XML 为 Java Build 对象 */
    public Build parse(String xmlContent) throws ParseException { ... }
    
    /** 解析 POB 文件 */
    public Build parseFile(Path pobXmlFile) throws ParseException { ... }
    
    /** 将 Build 对象序列化为 POB XML */
    public String serialize(Build build) { ... }
}
```

### 2. XML 结构映射

```xml
<PathOfBuilding>
  <Build level="95" className="Deadeye" ascendClassName="Deadeye"
         bandit="2" pantheonMajor="Solaris" pantheonMinor="Gruthkul">
    
    <Tree activeSpec="1" version="3_24">
      <Spec ascendClassId="2" masteryEffects="{...}" nodes="{...}">
        <URL>https://www.pathofexile.com/passive-skill-tree/...</URL>
        <Sockets>{...}</Sockets>
      </Spec>
    </Tree>
    
    <Skills>
      <SkillSet id="1" enabled="true" slot="Body Armour">
        <!-- 主动技能 + 辅助宝石 -->
      </SkillSet>
    </Skills>
    
    <Items>
      <Item id="1">...</Item>
      <!-- 所有装备物品 -->
    </Items>
    
    <Config>
      <!-- POB 配置选项 -->
    </Config>
    
  </Build>
</PathOfBuilding>
```

### 3. 特殊处理

- [ ] 天赋树节点：Base64 编码的节点列表 → `List<Integer>`
- [ ] 专精选择：嵌套 JSON → `Map<Integer, Integer>`
- [ ] 物品词缀：CDATA 段解析
- [ ] 技能宝石：多层嵌套结构

### 4. 容错处理

```java
// 未知字段 → 跳过并记录 WARN 日志
// 版本不兼容 → 尽力解析 + 标记不兼容字段
// XML 格式错误 → 明确错误信息
```

## 验收标准

- [ ] 解析 POB 社区版示例 Build 无错误
- [ ] 序列化后重新解析，数据一致
- [ ] 空 Build 可序列化为最小 XML
- [ ] 格式错误的 XML 有明确错误提示

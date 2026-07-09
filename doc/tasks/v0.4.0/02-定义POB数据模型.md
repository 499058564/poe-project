# T-022 定义 POB 数据模型 (Java VO)

**版本**：v0.4.0  
**模块**：`pob-adapter`（本次新建）  
**预估**：1 天  
**前置**：T-002  
**状态**：待开始

---

## 任务描述

创建 pob-adapter 模块，定义与 POB 数据结构对应的 Java POJO 模型。

## 详细步骤

### 1. 创建 `pob-adapter/build.gradle.kts`

```kotlin
dependencies {
    implementation(project(":common"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.1")
}
```

### 2. 核心模型

```java
// Build.java — 顶层 BD 配置
class Build {
    String version;
    String className;
    String ascendClassName;
    int level;
    String bandit;           // "oak"/"alira"/"kraityn"/"none"
    Tree tree;
    List<SkillSet> skills;
    List<ItemSlot> items;
    Config config;
}

// Tree.java — 天赋树
class Tree {
    String version;
    List<Integer> allocatedNodes;
    Map<Integer, Integer> masteries;  // nodeId → masteryIndex
    List<Integer> clusterJewelNodes;
}

// ItemSlot.java — 装备槽
class ItemSlot {
    String slotName;  // "Weapon 1", "Body Armour", ...
    Item item;        // null = 空槽位
}

// Item.java — 物品
class Item {
    String name;
    String baseType;
    String rarity;    // "UNIQUE"/"RARE"/"MAGIC"/"NORMAL"
    int itemLevel;
    int quality;
    boolean corrupted;
    boolean synthesised;
    List<String> implicits;
    List<String> explicits;
    List<String> craftedMods;
    List<String> enchantMods;
    List<SocketGroup> sockets;
    List<String> influences;
}

// SocketGroup.java — 插槽组
class SocketGroup {
    int groupId;
    List<Socket> sockets;
}

// Socket.java — 单个插槽
class Socket {
    String color;   // "R"/"G"/"B"/"W"/"A"
    Integer gemId;  // null = 空插槽
}

// SkillSet.java — 技能组合
class SkillSet {
    boolean enabled;
    SkillGem mainSkill;
    List<SupportGem> supports;
    String slotName;  // 所属装备槽
}

// Config.java — POB 配置
class Config {
    boolean enemyIsBoss;
    String enemyType;     // "Sirus"/"Shaper"/...
    boolean isEnemyShocked;
    boolean isEnemyIgnited;
    // ...
}
```

### 3. 枚举定义

```java
public enum SlotType { WEAPON_1, WEAPON_2, HELMET, BODY_ARMOUR, 
    GLOVES, BOOTS, AMULET, RING_1, RING_2, BELT, FLASK_1, /* ... */ }
public enum GemColor { RED, GREEN, BLUE, WHITE }
public enum DamageType { PHYSICAL, FIRE, COLD, LIGHTNING, CHAOS }
public enum Influence { SHAPER, ELDER, CRUSADER, REDEEMER, HUNTER, WARLORD }
```

## 验收标准

- [ ] 所有模型类编译通过
- [ ] 模型类可序列化为 JSON（Jackson）
- [ ] 字段命名与 POB XML 对应
- [ ] 枚举值覆盖游戏内所有类型

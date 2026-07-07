# POE Project — 流放之路集成工具

> 使用 Java + JavaFX 构建的流放之路(Path of Exile)一站式工具平台。

---

## 一、项目概述

poe_project 最终目标在于整合游玩过程中的所有工具，玩家使用单一工具就能满足日常使用，同时无需依赖于游戏窗口，避免破坏玩家游玩体验。

### 核心理念

- **一站式**：数据查询、BD 构筑、装备模拟、汉化翻译，一个工具全搞定
- **无侵入**：不依赖游戏进程，不读取游戏内存，完全合规
- **社区驱动**：基于 PoE Wiki、POB 等开源社区资源
- **智能化**：接入 AI 实现自动化 BD 构筑与装备制作推荐

---

## 二、技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 语言 | Java 17+ | LTS 版本，稳定可靠 |
| UI 框架 | JavaFX + AtlantaFX | 现代美观的暗黑主题 UI |
| 构建工具 | Gradle (Kotlin DSL) | 多模块项目管理 |
| 数据库 | SQLite (本地缓存) + H2 (测试) | 轻量级嵌入式数据库 |
| HTTP 客户端 | OkHttp | Wiki / poedb 数据抓取 |
| JSON 解析 | Jackson | 数据序列化与反序列化 |
| 日志 | SLF4J + Logback | 标准化日志方案 |
| 事件总线 | Guava EventBus | 模块间解耦通信 |
| 打包分发 | jpackage / jlink | 生成原生安装包 |

---

## 三、功能目标

### Phase 1 — 数据底座 (MVP)

- [ ] **Wiki 数据抓取**：通过 MediaWiki API 批量拉取 PoE Wiki Cargo 表数据
  ```
  资源链接: https://www.poewiki.net/wiki/Special:CargoTables
  核心表: items, skill_gems, passive_skills, mods, base_items, crafting_bench_options
  ```
- [ ] **本地数据缓存**：全量数据存储到 SQLite，支持增量更新
- [ ] **汉化翻译**：建立英文→中文映射词典，覆盖物品、技能、天赋等
  - 数据来源：poedb.tw、PoeCharm2 子模块、社区编年史翻译包
- [ ] **物品搜索**：支持中英文模糊搜索，Tooltip 展示详细属性

### Phase 2 — 核心功能

- [ ] **POB 集成**：JavaFX 做 UI + Headless POB 后台计算
  - POB 使用 MIT 协议，声明之后代码等可以直接使用
  - 美术资源属于 GGG 不能直接使用，提示用户安装 POB 后读取对应目录资源
  - Java 项目中引入 POB 作为子模块，打包时剔除 POB 的 Asset 资源（防止侵权）
  - 如果用户已安装 POB 则直接复用其美术资源
- [ ] **天赋树可视化**：基于 Canvas 自定义绘制，支持缩放、拖拽、搜索
- [ ] **装备模拟器**：模拟做装流程，计算期望成本
- [ ] **DPS 计算器**：实时计算技能伤害，对比装备提升

### Phase 3 — 智能化

- [ ] **AI BD 构筑**：根据用户偏好自动生成 Build 方案
- [ ] **自动化做装推荐**：基于成本和概率，推荐最优做装路径
- [ ] **市场分析**：结合市集数据，推荐性价比最高的装备搭配
- [ ] **智能问答**：基于 Wiki 数据训练的领域问答

---

## 四、POB 集成方案

### 方案概述

结合 POB 方案：**JavaFX 做 UI + Headless PoB 后台算**。

### 集成路径

#### 阶段一：进程间通信（快速上线）
```
Java ──(JSON via stdin/stdout)──▶ lua.exe (POB 计算引擎)
```
- 不改 POB 源码，启动子进程传递 JSON 输入/输出
- 优点：实现快，不侵入 POB
- 缺点：性能有损耗，需要管理进程生命周期

#### 阶段二：深度整合（长期方案）
```
Java ──(GraalVM Polyglot / JNI)──▶ LuaJIT Runtime
```
- 在 JVM 内直接调用 Lua 计算引擎
- 优点：性能最优，体验流畅
- 缺点：技术复杂度高

### 版权注意事项
1. POB 代码使用 MIT 协议，可自由使用，需保留版权声明
2. GGG 美术资源（图标、天赋树图片、技能特效等）**不可**直接打包分发
3. 解决方案：提示用户安装 POB 社区版，工具自动读取其本地资源目录

---

## 五、模块架构

```
poe-project/
├── app-ui/                 # JavaFX 界面层
│   ├── main/               # 主窗口、菜单、系统托盘
│   ├── views/              # 各功能页面 (FXML + Controller)
│   ├── components/         # 可复用 UI 组件 (天赋树Canvas、物品Tooltip等)
│   └── theme/              # CSS 暗黑主题
│
├── app-core/               # 业务协调层（粘合 UI / PoB / Data）
│   ├── event/              # 事件总线定义
│   ├── service/            # 业务服务 (搜索、翻译、BD管理等)
│   └── config/             # 应用配置管理
│
├── pob-runtime/            # PoB 运行时抽象（Submodule + Headless）
│
├── pob-adapter/            # PoB 数据适配（XML ↔ Java VO）
│   ├── model/              # Java POJO 映射 POB 数据结构
│   └── parser/             # XML 解析器
│
├── pob-ipc/                # 进程通信（Java ↔ LuaJIT）
│   ├── process/            # 进程管理
│   └── protocol/           # JSON 通信协议
│
├── data-provider/          # Wiki / poedb / 汉化数据获取
│   ├── wiki/               # PoE Wiki API 客户端
│   ├── poedb/              # poedb.tw 爬虫
│   ├── poecharm2/          # PoeCharm2 子模块（汉化词典来源）
│   └── ninja/              # poe.ninja 经济数据
│
├── data-cache/             # 本地缓存（SQLite / JSON）
│   ├── dao/                # 数据访问层
│   ├── migration/          # 数据库迁移脚本
│   └── sync/               # 增量同步策略
│
└── common/                 # 工具类、常量、异常定义
    ├── util/               # 通用工具方法
    ├── constant/           # 游戏常量（伤害类型、词缀标签等）
    └── exception/          # 自定义异常
```

### 模块依赖关系

```
app-ui ──▶ app-core ──▶ data-provider
              │              │
              │              └──▶ data-cache ──▶ common
              │
              ├──▶ pob-adapter ──▶ common
              │
              └──▶ pob-ipc ──▶ pob-runtime ──▶ common
```

---

## 六、数据流设计

### Wiki 数据同步流程

```
┌──────────┐     ┌──────────────┐     ┌──────────┐
│ PoE Wiki │ ──▶ │ data-provider │ ──▶ │ SQLite   │
│  Cargo   │     │   (OkHttp)    │     │  (缓存)   │
└──────────┘     └──────────────┘     └──────────┘
                                              │
                    ┌─────────────────────────┘
                    ▼
              ┌──────────┐     ┌──────────┐
              │ app-core │ ──▶ │ app-ui   │
              │ (搜索/翻译)│     │ (展示)    │
              └──────────┘     └──────────┘
```

### POB 计算流程

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│ 用户操作  │ ──▶ │ pob-adapter  │ ──▶ │  pob-ipc     │
│ (UI输入) │     │ (构建请求XML) │     │ (JSON通信)   │
└──────────┘     └──────────────┘     └──────────────┘
                                              │
                    ┌─────────────────────────┘
                    ▼
              ┌──────────┐     ┌──────────┐
              │ 计算结果  │ ◀── │ LuaJIT   │
              │ (Java VO) │     │ (计算引擎) │
              └──────────┘     └──────────┘
```

---

## 七、UI 设计要点

### 整体风格
- **暗黑主题**：模仿游戏内色调，深灰+暗金配色
- **响应式布局**：窗口缩放时面板自适应
- **多标签页**：类 IDE 布局，支持同时打开多个 BD

### 核心组件
| 组件 | 实现方式 | 说明 |
|------|----------|------|
| 天赋树 | 自定义 Canvas 绘制 | 1500+ 节点，需视口裁剪优化 |
| 物品 Tooltip | 自定义 Popup | 模仿游戏内悬浮提示样式 |
| 技能连線 | 自定义 Grid | 图形化展示 6 连配置 |
| 数据表格 | TableView | 物品列表、词缀浏览 |
| 搜索栏 | TextField + 自动补全 | 中英文模糊搜索 |

### 性能注意事项
- 天赋树 1500+ 节点 → 视口裁剪，只渲染可见区域
- 大量物品数据 → 虚拟化列表 (VirtualFlow)
- 图片资源 → 异步加载 + LRU 缓存

---

## 八、开发环境搭建

### 前置要求
- JDK 17+
- Gradle 8.x
- Git (用于拉取 POB 与 PoeCharm2 子模块)

### 快速开始

```bash
# 1. 克隆项目
git clone <repo-url>
cd poe-project

# 2. 初始化子模块
git submodule update --init --recursive

# 3. 构建项目
./gradlew build

# 4. 运行
./gradlew :app-ui:run
```

### IDE 配置
- IntelliJ IDEA: 直接导入 Gradle 项目
- 安装 JavaFX 插件 (如需要)
- 设置 Project SDK 为 JDK 17+

---

## 九、版本规划

| 版本 | 内容 | 目标 |
|------|------|------|
| v0.1.0 | 项目骨架 + 基础 UI 框架 | 跑通 Gradle + JavaFX |
| v0.2.0 | Wiki 数据抓取 + SQLite 缓存 | 数据底座可用 |
| v0.3.0 | 物品搜索 + 汉化翻译 | MVP 可发布 |
| v0.4.0 | POB 进程通信集成 | 核心计算可用 |
| v0.5.0 | 天赋树可视化 | 核心功能完成 |
| v0.6.0 | 装备模拟器 | 扩展功能 |
| v1.0.0 | AI 集成 + 全面优化 | 正式版发布 |

---

## 十、许可证与版权

- 本项目代码采用 [MIT License](https://opensource.org/licenses/MIT)
- POB (Path of Building) 代码遵循其 MIT 协议，版权归其原作者所有
- Path of Exile 游戏版权及所有美术资源归 Grinding Gear Games (GGG) 所有
- 本工具为社区开源项目，与 GGG 无任何关联
- 使用 POB 美术资源需由用户自行安装 POB 社区版

---

## 十一、参考资源

- [PoE Wiki CargoTables](https://www.poewiki.net/wiki/Special:CargoTables) — 核心数据源
- [Path of Building Community](https://github.com/PathOfBuildingCommunity/PathOfBuilding) — POB 社区版
- [poedb.tw](https://poedb.tw/) — 中文数据参考
- [Rayforward/PoeCharm2](https://github.com/Rayforward/PoeCharm2) — 汉化数据子模块来源
- [poe.ninja](https://poe.ninja/) — 经济数据
- [GGG 官方](https://www.pathofexile.com/) — 游戏官网
- [JavaFX 文档](https://openjfx.io/) — JavaFX 官方文档
- [AtlantaFX](https://github.com/mkpaz/atlantafx) — JavaFX 现代主题库
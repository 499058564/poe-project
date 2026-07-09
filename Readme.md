# POE Project — 流放之路集成工具

> 使用 Java + JavaFX 构建的流放之路(Path of Exile)一站式工具平台。

---

## 一、项目概述

poe_project 最终目标在于整合游玩过程中的所有工具，玩家使用单一工具就能满足日常使用，同时无需依赖于游戏窗口，避免破坏玩家游玩体验。

### 当前仓库状态

- 已提交 **Gradle 多模块项目骨架**，可通过 Gradle Wrapper 执行 `build`、`test`、`run` 等命令
- **v0.1.0 进度**：T-001~T-007 已完成——项目骨架、主窗口布局、配置管理、事件总线，详见 `doc/tasks/v0.1.0/`
- 大部分业务能力尚在 `doc/tasks/` 中分阶段设计，代码实现按任务逐步落地
- 仓库同时包含两个规划中的 Git 子模块：
  - `pob-runtime`：POB 运行时来源
  - `poecharm2`：中文翻译基础数据来源

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

> 推荐始终使用仓库内置的 **Gradle Wrapper**（`gradlew` / `gradlew.bat`），而不是依赖本机全局 Gradle。

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
│   ├── config/             # 应用配置管理（JSON 加载/保存/恢复）
│   ├── constant/           # 配置常量
│   └── event/              # 事件总线定义（导航、同步、配置变更）
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
├── data-provider/          # Wiki / poedb / poe.ninja 数据获取与导入协调
│   ├── wiki/               # PoE Wiki API 客户端
│   ├── poedb/              # poedb.tw 爬虫
│   └── ninja/              # poe.ninja 经济数据
│
├── poecharm2/              # PoeCharm2 独立模块（Git Submodule，只读汉化来源）
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
app-ui ──▶ app-core ──▶ data-provider ──▶ poecharm2
              │              │
              │              └──▶ data-cache ──▶ common
              │
              ├──▶ pob-adapter ──▶ common
              │
              └──▶ pob-ipc ──▶ pob-runtime ──▶ common
```

### 子模块职责

| 子模块 | 位置 | 用途 | 管理方式 |
|------|------|------|------|
| POB Runtime | `pob-runtime/` | 提供 Path of Building 运行时能力 | 作为独立子模块接入，业务侧通过 `pob-ipc` / `pob-adapter` 使用 |
| PoeCharm2 | `poecharm2/` | 提供基础汉化词典/资源 | 作为独立子模块接入，由 `data-provider` 负责导入与标准化 |

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

### 汉化数据导入流程

```
┌────────────┐     ┌──────────────┐     ┌──────────┐
│ PoeCharm2  │ ──▶ │ data-provider │ ──▶ │ SQLite   │
│ (Submodule)│     │ (导入/转换)    │     │ translations │
└────────────┘     └──────────────┘     └──────────┘
                                              │
                    ┌─────────────────────────┘
                    ▼
              ┌──────────┐     ┌──────────┐
              │ app-core │ ──▶ │ app-ui   │
              │ (翻译服务)│     │ (展示)    │
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
- JDK 17+（推荐 [Amazon Corretto 17](https://aws.amazon.com/corretto/) 或 [Eclipse Temurin 17](https://adoptium.net/)）
- Git (用于拉取 POB 与 PoeCharm2 子模块)

> 已提交 Gradle Wrapper，因此**不要求本机预装 Gradle**。

#### 配置 JAVA_HOME

项目构建依赖 `JAVA_HOME` 环境变量。若未配置，Gradle 会报错退出。

**Windows（用户级永久配置）：**

```powershell
# PowerShell（管理员）
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "E:\JDKs\openJdk_17", "User")
# 同时将 JDK bin 目录加入 Path
$path = [System.Environment]::GetEnvironmentVariable("Path", "User")
[System.Environment]::SetEnvironmentVariable("Path", "$path;E:\JDKs\openJdk_17\bin", "User")
```

**验证配置：**

```powershell
# 新开终端后执行
java -version
# 应输出：openjdk version "17.0.x" ...
```

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

Windows PowerShell 可使用：

```powershell
.\gradlew.bat build
.\gradlew.bat :app-ui:run
```

### 常用命令

| 目的 | 命令 |
|------|------|
| 查看模块 | `.\gradlew.bat projects` |
| 构建全部模块 | `.\gradlew.bat build` |
| 运行应用 | `.\gradlew.bat :app-ui:run` |
| 运行测试 | `.\gradlew.bat test` |
| 运行单个测试 | `.\gradlew.bat test --tests "com.poe.SomeTest"` |
| 运行指定模块测试 | `.\gradlew.bat :common:test --tests "com.poe.SomeTest"` |
| 查看依赖树 | `.\gradlew.bat :app-ui:dependencies --configuration runtimeClasspath` |
| 详细构建日志 | `.\gradlew.bat build --info` |
| 完整堆栈跟踪 | `.\gradlew.bat build --stacktrace` |
| 初始化子模块 | `git submodule update --init --recursive` |

> `jpackage` 为规划中的打包命令，待对应任务落地后启用。

### IDE 配置

#### IntelliJ IDEA

1. **导入项目**：`File` → `Open` → 选择 `build.gradle.kts`，以 Gradle 项目方式导入
2. **设置 JDK**：`File` → `Project Structure` → `SDK` → 选择 JDK 17+
3. **委托构建给 Gradle**（推荐）：
   `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
   - `Build and run using:` 选 `Gradle`
   - `Run tests using:` 选 `Gradle`

#### 正确运行方式

> ⚠️ **不能直接点击 `PoeApplication.main()` 运行！** JavaFX 应用需要 JavaFX 模块参数，直接以 `java.exe` 启动会报 `exit value 1`。

**方式一：Gradle 面板（推荐）**

右侧 `Gradle` 面板 → `app-ui` → `Tasks` → `application` → 双击 `run`

**方式二：创建 Gradle 运行配置**

`Run` → `Edit Configurations` → `+` → `Gradle`：
- Gradle project: `poe-project:app-ui`
- Tasks: `run`

配置后即可通过右上角运行按钮直接启动。

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

---

## 十二、故障排查

### `JAVA_HOME is not set`

Gradle 找不到 JDK。请参照 [配置 JAVA_HOME](#配置-java_home) 设置环境变量，并重启终端。

### `Process 'java.exe' finished with non-zero exit value 1`

在 IDEA 中直接运行 `PoeApplication.main()` 导致的 JavaFX 模块缺失错误。请改用 [Gradle 运行方式](#正确运行方式)。

### Gradle 下载依赖缓慢

项目默认使用阿里云 Maven 镜像。若仍缓慢，检查 `build.gradle.kts` 中 `repositories` 配置。

### 端口被占用 / Gradle Daemon 异常

```powershell
# 停止所有 Gradle Daemon
.\gradlew.bat --stop
# 重新构建
.\gradlew.bat build
```
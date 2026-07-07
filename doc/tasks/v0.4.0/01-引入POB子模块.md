# T-021 引入 POB 子模块

**版本**：v0.4.0  
**模块**：`pob-runtime`  
**预估**：0.25 天  
**前置**：无  
**状态**：待开始

---

## 任务描述

将 Path of Building Community 作为 Git 子模块引入项目。

## 详细步骤

### 1. 添加子模块

```bash
git submodule add https://github.com/PathOfBuildingCommunity/PathOfBuilding.git \
    pob-runtime/src/main/pob
```

### 2. 配置 `.gitmodules`

```ini
[submodule "pob-runtime/src/main/pob"]
    path = pob-runtime/src/main/pob
    url = https://github.com/PathOfBuildingCommunity/PathOfBuilding.git
```

### 3. 创建 `pob-runtime/build.gradle.kts`

```kotlin
plugins {
    java
}

// 打包时排除 POB 美术资源（防止侵权）
tasks.processResources {
    exclude("src/main/pob/assets/**")
    exclude("src/main/pob/.git/**")
}
```

### 4. 文档说明

- [ ] README 中说明 POB 子模块版本和用途
- [ ] 声明 POB MIT 许可证

## 验收标准

- [ ] `git submodule status` 显示 POB 子模块已拉取
- [ ] 打包产物不包含 `assets/` 目录
- [ ] 项目根 README 包含 POB 许可声明

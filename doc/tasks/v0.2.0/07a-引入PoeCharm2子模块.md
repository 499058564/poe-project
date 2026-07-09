# T-015a 引入 PoeCharm2 子模块

**版本**：v0.2.0  
**模块**：`poecharm2`  
**预估**：0.25 天  
**前置**：无  
**状态**：待开始

---

## 任务描述

将 `Rayforward/PoeCharm2` 作为独立 Git 子模块引入项目，作为基础汉化数据来源，为后续翻译导入与同步提供稳定输入。

## 详细步骤

### 1. 添加子模块

```bash
git submodule add https://github.com/Rayforward/PoeCharm2.git \
    poecharm2
```

### 2. 配置 `.gitmodules`

```ini
[submodule "poecharm2"]
    path = poecharm2
    url = https://github.com/Rayforward/PoeCharm2.git
```

### 3. 约定子模块用途

- [ ] 将 PoeCharm2 视为**只读数据源**，不在本项目内直接修改其内容
- [ ] 在 `data-provider` 中通过导入/转换逻辑读取 `poecharm2` 模块中的词典或资源文件
- [ ] 导入后的标准化结果落库到 SQLite `translations` 表
- [ ] 若 PoeCharm2 与 poedb.tw 数据冲突，先保留原始来源标记，后续由翻译导入策略统一裁决

### 4. 目录与打包约束

- [ ] 子模块路径固定为 `poecharm2/`
- [ ] 将 PoeCharm2 作为独立模块管理，而非挂载到 `data-provider` 目录下
- [ ] 不将子模块内 `.git` 元数据打包进产物

### 5. 文档说明

- [ ] README 中说明 PoeCharm2 是汉化来源子模块
- [ ] 翻译服务任务文档引用该子模块作为基础词典来源

## 验收标准

- [ ] `git submodule status` 显示 PoeCharm2 子模块已拉取
- [ ] 仓库执行 `git submodule update --init --recursive` 可正确初始化 PoeCharm2
- [ ] 翻译导入逻辑可读取 `poecharm2/` 下的数据文件
- [ ] README 与相关任务文档已说明 PoeCharm2 的用途

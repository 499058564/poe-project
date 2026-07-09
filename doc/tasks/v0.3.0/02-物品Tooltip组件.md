# T-017 物品 Tooltip 组件

**版本**：v0.3.0  
**模块**：`app-ui`  
**预估**：1 天  
**前置**：T-016  
**状态**：待开始

---

## 任务描述

实现游戏风格的物品悬浮提示组件，在鼠标悬停物品名时显示完整物品信息。

## 详细步骤

### 1. ItemTooltip 组件

```java
public class ItemTooltip extends PopupControl {
    private static final double MAX_WIDTH = 350;
    
    public ItemTooltip(ItemDetail item) {
        // 构建 Tooltip 内容
        VBox content = new VBox(2);
        
        // 头部：物品名称（稀有度着色 + 双行显示中英文）
        content.getChildren().add(buildHeader(item));
        content.getChildren().add(buildSeparator());
        
        // 基础属性
        if (item.hasProperties()) {
            content.getChildren().add(buildProperties(item));
            content.getChildren().add(buildSeparator());
        }
        
        // 需求
        if (item.hasRequirements()) {
            content.getChildren().add(buildRequirements(item));
            content.getChildren().add(buildSeparator());
        }
        
        // 词缀
        if (item.hasImplicits()) {
            content.getChildren().add(buildImplicits(item));
        }
        if (item.hasExplicits()) {
            content.getChildren().add(buildExplicits(item));
        }
        
        // 传奇描述
        if (item.hasFlavourText()) {
            content.getChildren().add(buildSeparator());
            content.getChildren().add(buildFlavourText(item));
        }
        
        getContent().add(content);
    }
}
```

### 2. 样式细节

```css
.item-tooltip {
    -fx-background-color: rgba(10, 10, 20, 0.95);
    -fx-border-color: #d4a853;
    -fx-border-width: 1px;
    -fx-padding: 8px;
}

.item-name-normal  { -fx-text-fill: #c8c8c8; }
.item-name-magic   { -fx-text-fill: #8888ff; }
.item-name-rare    { -fx-text-fill: #ffff77; }
.item-name-unique  { -fx-text-fill: #af6025; }

.mod-implicit { -fx-text-fill: #8888ff; }  /* 蓝色固定词缀 */
.mod-prefix   { -fx-text-fill: #8888ff; }  /* 蓝色前缀 */
.mod-suffix   { -fx-text-fill: #ffff77; }  /* 黄色后缀 */
.flavour-text { -fx-text-fill: #af6025; -fx-font-style: italic; }
```

### 3. 显示/隐藏行为

- [ ] 鼠标进入物品名 → 延迟 200ms 弹出
- [ ] 鼠标离开 → 立即隐藏
- [ ] 弹出位置：鼠标右下方偏移 10px
- [ ] 不超出屏幕边界（自动调整方向）
- [ ] 淡入动画（200ms opacity 0→1）

### 4. 性能

- [ ] Tooltip 内容懒加载（鼠标悬停时才构建）
- [ ] 相同物品不重复构建 Tooltip

## 验收标准

- [ ] 悬停物品名时显示游戏风格 Tooltip
- [ ] 不同稀有度显示不同颜色
- [ ] 词缀颜色正确（蓝色前缀、黄色后缀）
- [ ] Tooltip 不超出屏幕边界
- [ ] 离开后 Tooltip 消失

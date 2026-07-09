package com.poe.ui.components;

import com.poe.ui.constants.LayoutConstants;
import com.poe.ui.constants.PageIds;
import com.poe.ui.constants.StyleClasses;
import com.poe.ui.enums.PageDefEnum;
import com.poe.ui.i18n.Messages;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * 左侧导航侧边栏，支持二级菜单。
 * 一级菜单有子页面时，点击展开/收起子菜单；
 * 无子页面时，直接导航。
 * 默认宽度 200px，可通过折叠按钮缩至 48px。
 * 页面定义由外部通过构造参数注入（{@link PageDefEnum}）。
 */
public class Sidebar extends VBox {

    /** 页面定义（外部注入） */
    private final PageDefEnum[] pageDefs;

    /** 仅叶节点（无子页面的按钮）共享 ToggleGroup，保证互斥选中 */
    private final ToggleGroup toggleGroup = new ToggleGroup();

    /** 所有参与导航的叶子按钮（pageId → NavButton） */
    private final Map<String, NavButton> leafButtons = new LinkedHashMap<>();

    /** 二级菜单父节点（pageId → ToggleButton），用于展开/收起 */
    private final Map<String, ToggleButton> parentButtons = new LinkedHashMap<>();

    /** 二级菜单子节点容器（parentPageId → VBox），用于显隐控制 */
    private final Map<String, VBox> childContainers = new LinkedHashMap<>();

    /** 折叠/展开切换按钮 */
    private final ToggleButton collapseBtn;

    /** 导航回调：选中页面时通知外部 */
    private Consumer<String> onNavigate;

    /** 当前是否处于折叠态 */
    private boolean collapsed = false;

    private static final Logger log = LoggerFactory.getLogger(Sidebar.class);

    public Sidebar(PageDefEnum[] pageDefs) {
        this.pageDefs = pageDefs;
        getStyleClass().add(StyleClasses.SIDEBAR);
        setPrefWidth(LayoutConstants.SIDEBAR_EXPANDED_WIDTH);
        setMinWidth(LayoutConstants.SIDEBAR_COLLAPSED_WIDTH);
        setFillWidth(true);

        collapseBtn = new ToggleButton(LayoutConstants.COLLAPSE_ICON);
        collapseBtn.getStyleClass().add(StyleClasses.COLLAPSE_BTN);
        collapseBtn.setMaxWidth(Double.MAX_VALUE);
        collapseBtn.setOnAction(e -> toggleCollapse());
        getChildren().add(collapseBtn);

        buildMenu();
    }

    /** 根据 PageDefEnum 构建一级/二级菜单结构 */
    private void buildMenu() {
        // 按 parentPageId 分组
        Map<String, List<PageDefEnum>> childrenByParent = new LinkedHashMap<>();
        for (PageDefEnum def : pageDefs) {
            if (!def.isTopLevel()) {
                childrenByParent
                    .computeIfAbsent(def.parentPageId(), k -> new ArrayList<>())
                    .add(def);
            }
        }

        for (PageDefEnum def : pageDefs) {
            if (!def.isTopLevel()) {
                continue; // 子页面在父菜单内部渲染
            }

            List<PageDefEnum> children = childrenByParent.get(def.pageId());
            if (children != null && !children.isEmpty()) {
                buildParentItem(def, children);
            } else {
                buildLeafItem(def);
            }
        }
    }

    /** 构建有子菜单的一级菜单项：父按钮 + 可折叠子容器 */
    private void buildParentItem(PageDefEnum def, List<PageDefEnum> children) {
        String arrow = "\u25B6"; // ▶ 收起态
        ToggleButton parentBtn = new ToggleButton(arrow + " " + Messages.get(def.i18nKey()));
        parentBtn.getStyleClass().add(StyleClasses.NAV_BUTTON);
        parentBtn.setMaxWidth(Double.MAX_VALUE);
        parentBtn.setAlignment(Pos.CENTER_LEFT);

        if (def.disabled()) {
            parentBtn.setDisable(true);
            parentBtn.setOpacity(LayoutConstants.NAV_BUTTON_DISABLED_OPACITY);
        }

        // 子按钮容器（默认隐藏）
        VBox childBox = new VBox();
        childBox.setVisible(false);
        childBox.setManaged(false);

        // 构建子菜单按钮
        for (PageDefEnum child : children) {
            NavButton childBtn = new NavButton(
                "    " + Messages.get(child.i18nKey()),
                child.pageId(),
                child.disabled()
            );
            childBtn.setToggleGroup(toggleGroup);
            childBtn.getStyleClass().add(StyleClasses.NAV_SUB_ITEM);
            childBtn.setOnAction(e -> {
                if (onNavigate != null && !childBtn.isDisabled()) {
                    onNavigate.accept(child.pageId());
                }
            });
            leafButtons.put(child.pageId(), childBtn);
            childBox.getChildren().add(childBtn);
        }

        childContainers.put(def.pageId(), childBox);
        parentButtons.put(def.pageId(), parentBtn);

        // 父按钮点击：展开/收起子菜单
        parentBtn.setOnAction(e -> toggleSubMenu(def.pageId()));

        getChildren().add(parentBtn);
        getChildren().add(childBox);
    }

    /** 构建无子菜单的一级叶子节点 */
    private void buildLeafItem(PageDefEnum def) {
        NavButton btn = new NavButton(
            Messages.get(def.i18nKey()),
            def.pageId(),
            def.disabled()
        );
        btn.setToggleGroup(toggleGroup);
        btn.setOnAction(e -> {
            if (onNavigate != null && !btn.isDisabled()) {
                onNavigate.accept(def.pageId());
            }
        });
        leafButtons.put(def.pageId(), btn);
        getChildren().add(btn);
    }

    /** 展开或收起指定父菜单的子节点 */
    private void toggleSubMenu(String parentPageId) {
        VBox childBox = childContainers.get(parentPageId);
        ToggleButton parentBtn = parentButtons.get(parentPageId);
        if (childBox == null || parentBtn == null) {
            return;
        }

        boolean expanded = childBox.isVisible();
        if (expanded) {
            log.debug("Collapse submenu: {}", parentPageId);
            childBox.setVisible(false);
            childBox.setManaged(false);
            parentBtn.setText("\u25B6 " + getOriginalText(parentPageId)); // ▶
            parentBtn.setSelected(false);
        } else {
            childBox.setVisible(true);
            childBox.setManaged(true);
            log.debug("Expand submenu: {}", parentPageId);
            parentBtn.setText("\u25BC " + getOriginalText(parentPageId)); // ▼
        }
    }

    /** 展开指定父菜单（用于 selectPage 时自动展开） */
    private void expandSubMenu(String parentPageId) {
        VBox childBox = childContainers.get(parentPageId);
        ToggleButton parentBtn = parentButtons.get(parentPageId);
        if (childBox == null || parentBtn == null || childBox.isVisible()) {
            return;
        }
        childBox.setVisible(true);
        childBox.setManaged(true);
        parentBtn.setText("\u25BC " + getOriginalText(parentPageId)); // ▼
    }

    public void setOnNavigate(Consumer<String> handler) {
        this.onNavigate = handler;
    }

    /**
     * 选中指定页面。
     * 若目标为子页面，先展开父菜单再选中。
     */
    public void selectPage(String pageId) {
        // 查找 PageDefEnum 以确定层级关系
        for (PageDefEnum def : pageDefs) {
            if (def.pageId().equals(pageId)) {
                if (!def.isTopLevel()) {
                    // 子页面：先展开父菜单
                    expandSubMenu(def.parentPageId());
                }
                break;
            }
        }

        NavButton btn = leafButtons.get(pageId);
        if (btn != null) {
            btn.setSelected(true);
        }
    }

    /**
     * 切换侧边栏折叠/展开状态。
     * 折叠时隐藏按钮文字及所有子菜单，仅保留图标宽度。
     */
    private void toggleCollapse() {
        collapsed = !collapsed;
        log.debug("Sidebar collapsed: {}", collapsed);
        double targetWidth = collapsed ? LayoutConstants.SIDEBAR_COLLAPSED_WIDTH
                                       : LayoutConstants.SIDEBAR_EXPANDED_WIDTH;
        setPrefWidth(targetWidth);

        double iconOnlyWidth = LayoutConstants.SIDEBAR_COLLAPSED_WIDTH
                               - LayoutConstants.SIDEBAR_ICON_PADDING;

        // 折叠时收起所有子菜单
        if (collapsed) {
            for (Map.Entry<String, VBox> entry : childContainers.entrySet()) {
                entry.getValue().setVisible(false);
                entry.getValue().setManaged(false);
                ToggleButton parentBtn = parentButtons.get(entry.getKey());
                if (parentBtn != null) {
                    parentBtn.setText("\u25B6 " + getOriginalText(entry.getKey()));
                    parentBtn.setSelected(false);
                }
            }
        }

        // 折叠/展开所有父按钮
        for (ToggleButton btn : parentButtons.values()) {
            btn.setText(collapsed ? "" : buildParentText(btn));
            btn.setPrefWidth(collapsed ? iconOnlyWidth
                                       : LayoutConstants.SIDEBAR_EXPANDED_WIDTH
                                         - LayoutConstants.SIDEBAR_ICON_PADDING);
        }

        // 折叠/展开所有叶子按钮
        for (NavButton btn : leafButtons.values()) {
            btn.setText(collapsed ? "" : getOriginalText(btn.getPageId()));
            btn.setPrefWidth(collapsed ? iconOnlyWidth
                                       : LayoutConstants.SIDEBAR_EXPANDED_WIDTH
                                         - LayoutConstants.SIDEBAR_ICON_PADDING);
        }
    }

    /** 根据父按钮当前展开状态重建按钮文本（含箭头） */
    private String buildParentText(ToggleButton parentBtn) {
        for (Map.Entry<String, ToggleButton> entry : parentButtons.entrySet()) {
            if (entry.getValue() == parentBtn) {
                VBox childBox = childContainers.get(entry.getKey());
                boolean expanded = childBox != null && childBox.isVisible();
                String arrow = expanded ? "\u25BC " : "\u25B6 "; // ▼ or ▶
                return arrow + getOriginalText(entry.getKey());
            }
        }
        return parentBtn.getText();
    }

    /** 根据 pageId 查找原始 i18n 文本（不含箭头/缩进） */
    private String getOriginalText(String pageId) {
        for (PageDefEnum def : pageDefs) {
            if (def.pageId().equals(pageId)) {
                return Messages.get(def.i18nKey());
            }
        }
        return pageId;
    }
}
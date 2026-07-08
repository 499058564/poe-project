package com.poe.ui.components;

import com.poe.ui.constants.LayoutConstants;
import com.poe.ui.constants.PageDef;
import com.poe.ui.constants.StyleClasses;
import com.poe.ui.i18n.Messages;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 左侧导航侧边栏。
 * 默认宽度 200px，可通过 ToggleButton 折叠至 48px。
 * 页面定义由外部通过构造参数注入（{@link PageDef}）。
 */
public class Sidebar extends VBox {

    /** 页面定义（外部注入） */
    private final PageDef[] pageDefs;

    /** ToggleGroup 保证导航按钮互斥选中 */
    private final ToggleGroup toggleGroup = new ToggleGroup();
    /** 所有导航按钮 */
    private final List<NavButton> buttons = new ArrayList<>();
    /** 折叠/展开切换按钮 */
    private final ToggleButton collapseBtn;
    /** 导航回调：选中页面时通知外部 */
    private Consumer<String> onNavigate;
    /** 当前是否处于折叠态 */
    private boolean collapsed = false;

    public Sidebar(PageDef[] pageDefs) {
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

        for (PageDef def : pageDefs) {
            addNavButton(Messages.get(def.i18nKey()), def.pageId(), def.disabled());
        }
    }

    private void addNavButton(String text, String pageId, boolean disabled) {
        NavButton btn = new NavButton(text, pageId, disabled);
        btn.setToggleGroup(toggleGroup);
        btn.setOnAction(e -> {
            if (onNavigate != null && !btn.isDisabled()) {
                onNavigate.accept(pageId);
            }
        });
        buttons.add(btn);
        getChildren().add(btn);
    }

    public void setOnNavigate(Consumer<String> handler) {
        this.onNavigate = handler;
    }

    public void selectPage(String pageId) {
        for (NavButton btn : buttons) {
            if (btn.getPageId().equals(pageId)) {
                btn.setSelected(true);
                break;
            }
        }
    }

    /**
     * 切换侧边栏折叠/展开状态。
     * 折叠时隐藏按钮文字，仅保留图标宽度。
     */
    private void toggleCollapse() {
        collapsed = !collapsed;
        double targetWidth = collapsed ? LayoutConstants.SIDEBAR_COLLAPSED_WIDTH
                                       : LayoutConstants.SIDEBAR_EXPANDED_WIDTH;
        setPrefWidth(targetWidth);

        double iconOnlyWidth = LayoutConstants.SIDEBAR_COLLAPSED_WIDTH
                               - LayoutConstants.SIDEBAR_ICON_PADDING;
        for (NavButton btn : buttons) {
            btn.setText(collapsed ? "" : getOriginalText(btn.getPageId()));
            btn.setPrefWidth(collapsed ? iconOnlyWidth
                                       : LayoutConstants.SIDEBAR_EXPANDED_WIDTH
                                         - LayoutConstants.SIDEBAR_ICON_PADDING);
        }
    }

    private String getOriginalText(String pageId) {
        for (PageDef def : pageDefs) {
            if (def.pageId().equals(pageId)) {
                return Messages.get(def.i18nKey());
            }
        }
        return pageId;
    }
}

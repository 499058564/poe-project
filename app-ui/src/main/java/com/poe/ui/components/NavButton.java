package com.poe.ui.components;

import com.poe.ui.i18n.Messages;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

/**
 * 侧边栏导航按钮。
 * 选中状态：左侧强调色边框 + 背景高亮。
 * 禁用状态：灰度显示 + Tooltip。
 */
public class NavButton extends ToggleButton {

    private final String pageId;

    public NavButton(String text, String pageId, boolean disabled) {
        super(text);
        this.pageId = pageId;
        getStyleClass().add("nav-button");
        setMaxWidth(Double.MAX_VALUE);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (disabled) {
            setDisable(true);
            setTooltip(new Tooltip(Messages.get("nav.disabled.tooltip")));
            setOpacity(0.4);
        }
    }

    public NavButton(String text, String pageId) {
        this(text, pageId, false);
    }

    public String getPageId() {
        return pageId;
    }
}

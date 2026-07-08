package com.poe.ui.components;

import com.poe.ui.constants.LayoutConstants;
import com.poe.ui.constants.StyleClasses;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.NavKeys;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

/**
 * 侧边栏导航按钮。
 * 选中状态：左侧强调色边框 + 背景高亮。
 * 禁用状态：灰度显示 + Tooltip。
 */
public class NavButton extends ToggleButton {

    /** 关联的功能页面标识 */
    private final String pageId;

    public NavButton(String text, String pageId, boolean disabled) {
        super(text);
        this.pageId = pageId;
        getStyleClass().add(StyleClasses.NAV_BUTTON);
        setMaxWidth(Double.MAX_VALUE);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (disabled) {
            setDisable(true);
            setTooltip(new Tooltip(Messages.get(NavKeys.DISABLED_TOOLTIP)));
            setOpacity(LayoutConstants.NAV_BUTTON_DISABLED_OPACITY);
        }
    }

    public NavButton(String text, String pageId) {
        this(text, pageId, false);
    }

    public String getPageId() {
        return pageId;
    }
}

package com.poe.core.event;

/**
 * 导航事件，在 UI 页面间切换时发布。
 *
 * <p>由侧边栏或 Tab 切换触发，携带来源页面和目标页面 ID。
 * 页面 ID 参见 {@link com.poe.ui.constants.PageIds}。
 */
public class NavigationEvent {

    /** 来源页面 ID */
    private final String source;
    /** 目标页面 ID */
    private final String target;

    /**
     * @param source 来源页面 ID
     * @param target 目标页面 ID
     */
    public NavigationEvent(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() { return source; }
    public String getTarget() { return target; }

    @Override
    public String toString() {
        return "NavigationEvent{source='" + source + "', target='" + target + "'}";
    }
}

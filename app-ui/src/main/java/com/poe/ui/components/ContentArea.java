package com.poe.ui.components;

import com.poe.ui.i18n.Messages;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多标签内容区。
 * 管理功能页面的打开、切换、关闭。
 * 同一功能不可重复打开；设置页不可关闭。
 */
public class ContentArea extends TabPane {

    private static final String SETTINGS_PAGE = "settings";

    private static final String[][] PAGE_DEFS = {
        { "item-search", "nav.item-search" },
        { "skill-gems",  "nav.skill-gems" },
        { "passive-tree","nav.passive-tree" },
        { "gear-sim",    "nav.gear-sim" },
        { "settings",    "nav.settings" },
    };

    private final Map<String, Tab> tabMap = new LinkedHashMap<>();

    public ContentArea() {
        getStyleClass().add("content-area");
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
    }

    /**
     * 打开或切换到指定功能页面。
     * 如果页面已存在则选中，否则创建新 Tab。
     */
    public void openPage(String pageId) {
        if (tabMap.containsKey(pageId)) {
            getSelectionModel().select(tabMap.get(pageId));
            return;
        }

        String title = resolveTitle(pageId);
        Tab tab = new Tab(title, createPlaceholder(pageId));

        if (SETTINGS_PAGE.equals(pageId)) {
            tab.setClosable(false);
        }

        tab.setOnCloseRequest(e -> tabMap.remove(pageId));

        tabMap.put(pageId, tab);
        getTabs().add(tab);
        getSelectionModel().select(tab);
    }

    private String resolveTitle(String pageId) {
        for (String[] def : PAGE_DEFS) {
            if (def[0].equals(pageId)) {
                return Messages.get(def[1]);
            }
        }
        return pageId;
    }

    private StackPane createPlaceholder(String pageId) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("page-placeholder");

        Label label = new Label(Messages.fmt("placeholder.coming-soon", resolveTitle(pageId)));
        label.getStyleClass().add("placeholder-label");
        pane.getChildren().add(label);
        return pane;
    }
}

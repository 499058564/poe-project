package com.poe.ui.components;

import com.poe.ui.constants.PageIds;
import com.poe.ui.constants.StyleClasses;
import com.poe.ui.enums.PageDefEnum;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.PlaceholderKeys;
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
 * 页面定义由外部通过构造参数注入（{@link PageDefEnum}）。
 */
public class ContentArea extends TabPane {

    /** 页面定义（外部注入） */
    private final PageDefEnum[] pageDefs;

    /** 已打开的标签页（保持插入顺序） */
    private final Map<String, Tab> tabMap = new LinkedHashMap<>();

    public ContentArea(PageDefEnum[] pageDefs) {
        this.pageDefs = pageDefs;
        getStyleClass().add(StyleClasses.CONTENT_AREA);
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
    }

    /**
     * 打开或切换到指定功能页面。
     * 如果页面已存在则选中，否则创建新 Tab。
     * 父菜单页面（有子页的一级菜单）不能作为 Tab 打开。
     */
    public void openPage(String pageId) {
        // 父菜单页面不可作为 Tab 打开
        if (isParentPage(pageId)) {
            return;
        }

        if (tabMap.containsKey(pageId)) {
            getSelectionModel().select(tabMap.get(pageId));
            return;
        }

        String title = resolveTitle(pageId);
        Tab tab = new Tab(title, createPlaceholder(pageId));

        if (PageIds.SETTINGS.equals(pageId)) {
            tab.setClosable(false);
        }

        tab.setOnCloseRequest(e -> tabMap.remove(pageId));

        tabMap.put(pageId, tab);
        getTabs().add(tab);
        getSelectionModel().select(tab);
    }

    /** 判断指定 pageId 是否为一父菜单（有子页但不能作为 Tab 打开） */
    private boolean isParentPage(String pageId) {
        for (PageDefEnum def : pageDefs) {
            if (def.isTopLevel() && def.pageId().equals(pageId)) {
                // 检查是否有子页
                for (PageDefEnum child : pageDefs) {
                    if (pageId.equals(child.parentPageId())) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private String resolveTitle(String pageId) {
        for (PageDefEnum def : pageDefs) {
            if (def.pageId().equals(pageId)) {
                return Messages.get(def.i18nKey());
            }
        }
        return pageId;
    }

    private StackPane createPlaceholder(String pageId) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add(StyleClasses.PAGE_PLACEHOLDER);

        Label label = new Label(Messages.fmt(PlaceholderKeys.COMING_SOON, resolveTitle(pageId)));
        label.getStyleClass().add(StyleClasses.PLACEHOLDER_LABEL);
        pane.getChildren().add(label);
        return pane;
    }
}

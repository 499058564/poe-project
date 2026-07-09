package com.poe.ui.components;

import com.poe.ui.constants.StyleClasses;
import com.poe.ui.enums.PageDefEnum;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.PlaceholderKeys;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多标签内容区。
 * <ul>
 *   <li>同一功能不可重复打开（Tab 去重）；</li>
 *   <li>父菜单页面（有子页的一级菜单）不能作为 Tab 打开；</li>
 *   <li>关闭 Tab 时自动清理缓存。</li>
 * </ul>
 * 页面定义由外部通过构造参数注入（{@link PageDefEnum}）。
 */
public class ContentArea extends TabPane {

    /** 页面定义（外部注入） */
    private final PageDefEnum[] pageDefs;

    /** 已打开的标签页（保持插入顺序） */
    private final Map<String, Tab> tabMap = new LinkedHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(ContentArea.class);

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
            log.debug("Skip parent page: {}", pageId);
            return;
        }

        if (tabMap.containsKey(pageId)) {
            log.debug("Switch to existing tab: {}", pageId);
            getSelectionModel().select(tabMap.get(pageId));
            return;
        }

        log.debug("Open new tab: {}", pageId);
        String title = resolveTitle(pageId);
        Tab tab = new Tab(title, createPlaceholder(pageId));

        tab.setOnCloseRequest(e -> {
            log.debug("Close tab: {}", pageId);
            tabMap.remove(pageId);
        });

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

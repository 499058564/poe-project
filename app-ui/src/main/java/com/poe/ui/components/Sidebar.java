package com.poe.ui.components;

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
 */
public class Sidebar extends VBox {

    private static final double EXPANDED_WIDTH = 200;
    private static final double COLLAPSED_WIDTH = 48;

    private static final String[][] NAV_ITEMS = {
        { "item-search", "nav.item-search" },
        { "skill-gems", "nav.skill-gems" },
        { "passive-tree", "nav.passive-tree" },
        { "gear-sim",   "nav.gear-sim", "disabled" },
        { "settings",   "nav.settings" },
    };

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final List<NavButton> buttons = new ArrayList<>();
    private final ToggleButton collapseBtn;
    private Consumer<String> onNavigate;
    private boolean collapsed = false;

    public Sidebar() {
        getStyleClass().add("sidebar");
        setPrefWidth(EXPANDED_WIDTH);
        setMinWidth(COLLAPSED_WIDTH);
        setFillWidth(true);

        collapseBtn = new ToggleButton("\u2630");
        collapseBtn.getStyleClass().add("collapse-btn");
        collapseBtn.setMaxWidth(Double.MAX_VALUE);
        collapseBtn.setOnAction(e -> toggleCollapse());

        getChildren().add(collapseBtn);

        for (String[] item : NAV_ITEMS) {
            boolean disabled = item.length > 2 && "disabled".equals(item[2]);
            addNavButton(Messages.get(item[1]), item[0], disabled);
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

    private void addNavButton(String text, String pageId) {
        addNavButton(text, pageId, false);
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

    private void toggleCollapse() {
        collapsed = !collapsed;
        setPrefWidth(collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH);

        double iconOnlyWidth = COLLAPSED_WIDTH - 16;
        for (NavButton btn : buttons) {
            btn.setText(collapsed ? "" : getOriginalText(btn.getPageId()));
            btn.setPrefWidth(collapsed ? iconOnlyWidth : EXPANDED_WIDTH - 16);
        }
    }

    private String getOriginalText(String pageId) {
        for (String[] item : NAV_ITEMS) {
            if (item[0].equals(pageId)) {
                return Messages.get(item[1]);
            }
        }
        return pageId;
    }
}

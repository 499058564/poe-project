package com.poe.ui.page;

import com.poe.cache.model.ItemSummary;
import com.poe.core.model.ItemDetail;
import com.poe.core.model.SearchResult;
import com.poe.core.service.ItemSearchService;
import com.poe.ui.constants.StyleClasses;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.SearchKeys;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 物品搜索页面：搜索栏 + 结果列表 + 详情面板 + 分页。
 * <p>
 * 布局：
 * <pre>
 * ┌────────────────────────────────────────────────┐
 * │  [搜索框] [类别▼] [搜索按钮]                     │
 * ├────────────┬───────────────────────────────────┤
 * │  结果列表   │  详情面板                         │
 * │  (ListView) │  (VBox)                         │
 * ├────────────┴───────────────────────────────────┤
 * │  共 N 条结果  第 M/P 页  [上一页] [下一页]       │
 * └────────────────────────────────────────────────┘
 * </pre>
 * <p>
 * 交互：
 * <ul>
 *   <li>回车键或点击按钮触发搜索</li>
 *   <li>输入停止 300ms 后自动搜索（防抖）</li>
 *   <li>搜索中显示加载动画</li>
 *   <li>点击列表项异步加载详情并显示在右侧面板</li>
 *   <li>分页按钮切换页面</li>
 * </ul>
 */
public class ItemSearchView extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(ItemSearchView.class);
    /**
     * 默认每页显示数量
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ItemSearchService searchService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "item-search");
        t.setDaemon(true);
        return t;
    });

    // Search bar components
    private final TextField searchField;
    private final ComboBox<String> categoryCombo;
    private final Button searchBtn;

    // Result list
    private final ListView<ItemSummary> resultList;
    private final ObservableList<ItemSummary> resultItems = FXCollections.observableArrayList();

    // Detail panel
    private final ItemDetailPanel detailPanel;

    // Pagination
    private final Label paginationLabel;
    private final Button prevBtn;
    private final Button nextBtn;

    // Debounce
    private final javafx.animation.PauseTransition debounce;

    // State
    private String lastKeyword = "";
    private String lastCategory = null;
    private int currentPage = 1;
    private int totalResults = 0;
    private ProgressIndicator loadingIndicator;
    private StackPane centerPane;

    /**
     * @param searchService 物品搜索服务
     */
    public ItemSearchView(ItemSearchService searchService) {
        this.searchService = searchService;
        getStyleClass().add("item-search-view");

        // 搜索输入框
        searchField = new TextField();
        searchField.setPromptText(Messages.get(SearchKeys.PLACEHOLDER));
        searchField.getStyleClass().add("search-field");

        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().setAll(
            Messages.get(SearchKeys.CATEGORY_ALL)
        );
        categoryCombo.getSelectionModel().selectFirst();

        searchBtn = new Button(Messages.get(SearchKeys.BUTTON));
        searchBtn.getStyleClass().add(StyleClasses.SEARCH_BTN);

        HBox searchBar = new HBox(8, searchField, categoryCombo, searchBtn);
        searchBar.getStyleClass().add(StyleClasses.SEARCH_BAR);
        setTop(searchBar);

        // 结果列表 + 详细信息面板
        resultList = new ListView<>(resultItems);
        resultList.getStyleClass().add(StyleClasses.RESULT_LIST);
        resultList.setCellFactory(lv -> new ItemResultCell());
        resultList.setPlaceholder(createPlaceholderLabel(""));

        detailPanel = new ItemDetailPanel();

        SplitPane splitPane = new SplitPane(resultList, detailPanel);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPosition(0, 0.4);

        // 加载覆盖层
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add(StyleClasses.SEARCH_LOADING);
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(48, 48);

        centerPane = new StackPane(splitPane, loadingIndicator);
        StackPane.setAlignment(loadingIndicator, javafx.geometry.Pos.CENTER);
        setCenter(centerPane);

        // 分页信息
        paginationLabel = new Label();
        paginationLabel.getStyleClass().add(StyleClasses.PAGINATION_LABEL);

        prevBtn = new Button(Messages.get(SearchKeys.PREV));
        nextBtn = new Button(Messages.get(SearchKeys.NEXT));

        HBox paginationBar = new HBox(12, paginationLabel, prevBtn, nextBtn);
        paginationBar.getStyleClass().add(StyleClasses.PAGINATION_BAR);
        setBottom(paginationBar);

        // Debounce must be initialized before event handlers that reference it
        debounce = new javafx.animation.PauseTransition(Duration.millis(300));
        debounce.setOnFinished(e -> doSearch());

        // ---- Events ----
        // Enter key triggers immediate search
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                debounce.stop();
                doSearch();
            }
        });

        searchBtn.setOnAction(e -> {
            debounce.stop();
            doSearch();
        });

        // Debounce: auto-search 300ms after typing stops
        searchField.textProperty().addListener((obs, old, val) -> {
            if (!val.equals(old)) {
                debounce.playFromStart();
            }
        });

        // Category change triggers search
        categoryCombo.setOnAction(e -> doSearch());

        // Result list selection → load detail
        resultList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                loadItemDetail(selected.getId());
            }
        });

        // Pagination
        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                goToPage(currentPage - 1);
            }
        });
        nextBtn.setOnAction(e -> {
            if (currentPage * DEFAULT_PAGE_SIZE < totalResults) {
                goToPage(currentPage + 1);
            }
        });

        // Initial state
        updatePagination(0);
    }

    /** Execute search with current field values. */
    private void doSearch() {
        String keyword = searchField.getText();
        String category = categoryCombo.getValue();
        // "全部类别" → null, otherwise use as-is
        String categoryFilter = Messages.get(SearchKeys.CATEGORY_ALL).equals(category) ? null : category;

        lastKeyword = keyword;
        lastCategory = categoryFilter;
        currentPage = 1;
        performSearch(keyword, categoryFilter, 1);
    }

    /** Navigate to a specific page. */
    private void goToPage(int page) {
        currentPage = page;
        performSearch(lastKeyword, lastCategory, page);
    }

    /** Execute the search on a background thread. */
    private void performSearch(String keyword, String category, int page) {
        // Show loading
        loadingIndicator.setVisible(true);

        Task<SearchResult<ItemSummary>> task = new Task<>() {
            @Override
            protected SearchResult<ItemSummary> call() {
                return searchService.search(keyword, category, page, DEFAULT_PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            SearchResult<ItemSummary> result = task.getValue();
            displayResults(result);
        });

        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            log.error("Search failed", task.getException());
            resultItems.clear();
            Label errorLabel = new Label(Messages.get(SearchKeys.ERROR));
            errorLabel.getStyleClass().add(StyleClasses.SEARCH_STATUS_ERROR);
            resultList.setPlaceholder(errorLabel);
            detailPanel.clear();
            updatePagination(0);
        });

        executor.submit(task);
    }

    /** Display search results in the list view. */
    private void displayResults(SearchResult<ItemSummary> result) {
        List<ItemSummary> items = result.getItems();
        totalResults = result.getTotal();
        currentPage = result.getPage();

        resultItems.setAll(items);
        updatePagination(totalResults);

        if (items.isEmpty() && !lastKeyword.isBlank()) {
            Label noResults = new Label(Messages.get(SearchKeys.NO_RESULTS));
            noResults.getStyleClass().add(StyleClasses.SEARCH_STATUS);
            resultList.setPlaceholder(noResults);
        }

        // Auto-select first item
        if (!items.isEmpty()) {
            resultList.getSelectionModel().selectFirst();
        } else {
            detailPanel.clear();
        }
    }

    /** Load item detail asynchronously. */
    private void loadItemDetail(int itemId) {
        detailPanel.showLoading();
        executor.submit(() -> {
            ItemDetail detail = searchService.getItemDetail(itemId);
            Platform.runLater(() -> {
                if (detail != null) {
                    detailPanel.display(detail);
                } else {
                    detailPanel.clear();
                }
            });
        });
    }

    /** Update pagination bar state. */
    private void updatePagination(int total) {
        if (total == 0) {
            paginationLabel.setText(Messages.fmt(SearchKeys.RESULT_COUNT, 0));
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            return;
        }
        int totalPages = (total + DEFAULT_PAGE_SIZE - 1) / DEFAULT_PAGE_SIZE;
        paginationLabel.setText(
            Messages.fmt(SearchKeys.RESULT_COUNT, total) + "  " +
            Messages.fmt(SearchKeys.PAGE_INFO, totalPages, currentPage)
        );
        prevBtn.setDisable(currentPage <= 1);
        nextBtn.setDisable(currentPage * DEFAULT_PAGE_SIZE >= total);
    }

    /** Create a placeholder label for empty state. */
    private Label createPlaceholderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(StyleClasses.SEARCH_STATUS);
        return label;
    }

    // ── Custom List Cell ──

    /** Custom cell factory for result items: name + subtitle + level. */
    private static class ItemResultCell extends ListCell<ItemSummary> {
        private final Label nameLabel = new Label();
        private final Label subtitleLabel = new Label();
        private final Label levelLabel = new Label();
        private final VBox content = new VBox(2, nameLabel, subtitleLabel);
        private final HBox row = new HBox(0);
        private final Region spacer = new Region();

        {
            nameLabel.getStyleClass().add(StyleClasses.RESULT_NAME);
            subtitleLabel.getStyleClass().add(StyleClasses.RESULT_SUBTITLE);
            levelLabel.getStyleClass().add(StyleClasses.RESULT_LEVEL);
            row.getStyleClass().add(StyleClasses.RESULT_CELL);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().setAll(content, spacer, levelLabel);
        }

        @Override
        protected void updateItem(ItemSummary item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            String displayName = item.getNameZh() != null && !item.getNameZh().isEmpty()
                ? item.getName() + "  " + item.getNameZh()
                : item.getName();
            nameLabel.setText(displayName);

            // Rarity color based on item class
            nameLabel.getStyleClass().removeAll(
                "result-name-normal", "result-name-magic", "result-name-rare",
                "result-name-unique", "result-name-gem", "result-name-currency"
            );
            nameLabel.getStyleClass().add(getRarityClass(item.getItemClass()));

            subtitleLabel.setText(item.getItemClass() != null ? item.getItemClass() : "");
            levelLabel.setText(item.getDropLevel() > 0
                ? Messages.get(SearchKeys.DETAIL_DROP_LEVEL) + ": " + item.getDropLevel()
                : "");

            setGraphic(row);
        }

        private static String getRarityClass(String itemClass) {
            if (itemClass == null) return "result-name-normal";
            String lower = itemClass.toLowerCase();
            if (lower.contains("unique") || lower.contains("传奇")) return "result-name-unique";
            if (lower.contains("currency") || lower.contains("通货") || lower.contains("card")) return "result-name-currency";
            if (lower.contains("gem") || lower.contains("宝石") || lower.contains("skill")) return "result-name-gem";
            if (lower.contains("magic") || lower.contains("魔法")) return "result-name-magic";
            if (lower.contains("rare") || lower.contains("稀有")) return "result-name-rare";
            return "result-name-normal";
        }
    }

    // ── Detail Panel ──

    /** Right-side panel showing item details. */
    private static class ItemDetailPanel extends VBox {

        private final Label titleLabel = new Label();
        private final Label classLabel = new Label();
        private final VBox requirementsBox = new VBox(4);
        private final Label requirementsTitle = new Label();
        private final VBox implicitsBox = new VBox(2);
        private final Label implicitsTitle = new Label();
        private final Label flavourLabel = new Label();
        private final ProgressIndicator loadingIndicator = new ProgressIndicator(24);

        ItemDetailPanel() {
            getStyleClass().add(StyleClasses.DETAIL_PANEL);
            setFillWidth(true);

            titleLabel.getStyleClass().add(StyleClasses.DETAIL_TITLE);
            classLabel.getStyleClass().add("detail-class");

            requirementsTitle.getStyleClass().add("detail-section-title");
            implicitsTitle.getStyleClass().add("detail-section-title");
            flavourLabel.getStyleClass().add("detail-flavour");
            flavourLabel.setWrapText(true);

            loadingIndicator.setVisible(false);
            loadingIndicator.getStyleClass().add(StyleClasses.SEARCH_LOADING);

            getChildren().setAll(titleLabel, classLabel, loadingIndicator,
                requirementsTitle, requirementsBox, implicitsTitle, implicitsBox, flavourLabel);
            setVisible(false);
        }

        void showLoading() {
            setVisible(true);
            loadingIndicator.setVisible(true);
            titleLabel.setText(Messages.get(SearchKeys.LOADING));
        }

        void display(ItemDetail detail) {
            setVisible(true);
            loadingIndicator.setVisible(false);

            ItemSummary summary = detail.getSummary();

            // Title with rarity color
            String name = summary.getNameZh() != null && !summary.getNameZh().isEmpty()
                ? summary.getName() + "  " + summary.getNameZh()
                : summary.getName();
            titleLabel.setText(name);
            titleLabel.getStyleClass().removeAll(
                "detail-title-normal", "detail-title-magic",
                "detail-title-rare", "detail-title-unique"
            );
            titleLabel.getStyleClass().add(getDetailRarityClass(summary.getItemClass()));

            // Class + drop level
            String classText = Messages.fmt(SearchKeys.DETAIL_CLASS, summary.getItemClass() != null ? summary.getItemClass() : "-");
            String dropText = Messages.fmt(SearchKeys.DETAIL_DROP_LEVEL, summary.getDropLevel());
            classLabel.setText(classText + "  |  " + dropText);

            // Requirements section
            Map<String, Integer> reqs = detail.getRequirements();
            if (!reqs.isEmpty()) {
                requirementsTitle.setText(Messages.get(SearchKeys.DETAIL_REQUIREMENTS));
                requirementsTitle.setVisible(true);
                requirementsBox.getChildren().clear();
                if (reqs.containsKey("level")) {
                    addReqRow(Messages.get(SearchKeys.DETAIL_LEVEL), reqs.get("level"));
                }
                if (reqs.containsKey("str")) {
                    addReqRow(Messages.get(SearchKeys.DETAIL_STR), reqs.get("str"));
                }
                if (reqs.containsKey("dex")) {
                    addReqRow(Messages.get(SearchKeys.DETAIL_DEX), reqs.get("dex"));
                }
                if (reqs.containsKey("int")) {
                    addReqRow(Messages.get(SearchKeys.DETAIL_INT), reqs.get("int"));
                }
                requirementsBox.setVisible(true);
            } else {
                requirementsBox.setVisible(false);
                requirementsTitle.setVisible(false);
            }

            // Implicits section
            List<String> implicits = detail.getImplicits();
            if (!implicits.isEmpty()) {
                implicitsTitle.setText(Messages.get(SearchKeys.DETAIL_IMPLICITS));
                implicitsTitle.setVisible(true);
                implicitsBox.getChildren().clear();
                for (String imp : implicits) {
                    Label impLabel = new Label(imp);
                    impLabel.getStyleClass().add("detail-implicit");
                    implicitsBox.getChildren().add(impLabel);
                }
                implicitsBox.setVisible(true);
            } else {
                implicitsBox.setVisible(false);
                implicitsTitle.setVisible(false);
            }

            // Flavour text
            String flavour = detail.getFlavourText();
            if (flavour != null && !flavour.isEmpty()) {
                flavourLabel.setText(flavour);
                flavourLabel.setVisible(true);
            } else {
                flavourLabel.setVisible(false);
            }
        }

        void clear() {
            setVisible(false);
            titleLabel.setText("");
            classLabel.setText("");
            requirementsBox.getChildren().clear();
            requirementsBox.setVisible(false);
            requirementsTitle.setVisible(false);
            implicitsBox.getChildren().clear();
            implicitsBox.setVisible(false);
            implicitsTitle.setVisible(false);
            flavourLabel.setVisible(false);
        }

        private void addReqRow(String label, Integer value) {
            HBox row = new HBox(16);
            Label keyLabel = new Label(label);
            keyLabel.getStyleClass().add("detail-req-label");
            Label valLabel = new Label(String.valueOf(value));
            valLabel.getStyleClass().add("detail-req-value");
            row.getChildren().addAll(keyLabel, valLabel);
            requirementsBox.getChildren().add(row);
        }

        private static String getDetailRarityClass(String itemClass) {
            if (itemClass == null) return "detail-title-normal";
            String lower = itemClass.toLowerCase();
            if (lower.contains("unique") || lower.contains("传奇")) return "detail-title-unique";
            if (lower.contains("magic") || lower.contains("魔法")) return "detail-title-magic";
            if (lower.contains("rare") || lower.contains("稀有")) return "detail-title-rare";
            return "detail-title-normal";
        }
    }
}

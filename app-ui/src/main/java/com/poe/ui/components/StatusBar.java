package com.poe.ui.components;

import com.poe.ui.constants.LayoutConstants;
import com.poe.ui.constants.StyleClasses;
import com.poe.ui.i18n.Messages;
import com.poe.ui.i18n.keys.StatusKeys;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * 底部状态栏。
 * 显示数据版本、同步时间、状态信息和同步进度条。
 */
public class StatusBar extends HBox {

    /** 数据版本标签 */
    private final Label dataVersion;
    /** 同步时间标签 */
    private final Label syncTime;
    /** 状态信息标签 */
    private final Label status;
    /** 同步进度条（仅在同步期间可见） */
    private final ProgressBar syncProgress;

    public StatusBar() {
        getStyleClass().add(StyleClasses.STATUS_BAR);
        setSpacing(LayoutConstants.STATUS_BAR_SPACING);

        dataVersion = new Label(Messages.get(StatusKeys.DATA_VERSION_PREFIX) + " —");
        dataVersion.getStyleClass().add(StyleClasses.STATUS_LABEL);

        syncTime = new Label(Messages.get(StatusKeys.SYNC_TIME_PREFIX) + " —");
        syncTime.getStyleClass().add(StyleClasses.STATUS_LABEL);

        status = new Label(Messages.get(StatusKeys.READY));
        status.getStyleClass().add(StyleClasses.STATUS_LABEL);

        syncProgress = new ProgressBar(LayoutConstants.STATUS_BAR_PROGRESS_INITIAL);
        syncProgress.setVisible(false);
        syncProgress.setPrefWidth(LayoutConstants.STATUS_BAR_PROGRESS_PREF_WIDTH);
        syncProgress.setMaxHeight(LayoutConstants.STATUS_BAR_PROGRESS_MAX_HEIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(dataVersion, syncTime, spacer, status, syncProgress);
    }

    /**
     * 设置数据版本标签文本。
     *
     * @param version 数据版本号字符串
     */
    public void setDataVersion(String version) {
        dataVersion.setText(Messages.get(StatusKeys.DATA_VERSION_PREFIX) + " " + version);
    }

    /**
     * 设置上次同步时间标签文本。
     *
     * @param time 同步时间字符串
     */
    public void setSyncTime(String time) {
        syncTime.setText(Messages.get(StatusKeys.SYNC_TIME_PREFIX) + " " + time);
    }

    /**
     * 设置当前状态文本。
     *
     * @param text 状态信息（如"就绪"、"同步中"等）
     */
    public void setStatus(String text) {
        status.setText(text);
    }

    /**
     * 更新同步进度条。
     * 进度为 0 或 1 时自动隐藏进度条，其余值显示。
     *
     * @param progress 进度值，范围 [0, 1]
     */
    public void setSyncProgress(double progress) {
        syncProgress.setProgress(progress);
        syncProgress.setVisible(progress > 0 && progress < 1);
    }
}

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

    public void setDataVersion(String version) {
        dataVersion.setText(Messages.get(StatusKeys.DATA_VERSION_PREFIX) + " " + version);
    }

    public void setSyncTime(String time) {
        syncTime.setText(Messages.get(StatusKeys.SYNC_TIME_PREFIX) + " " + time);
    }

    public void setStatus(String text) {
        status.setText(text);
    }

    public void setSyncProgress(double progress) {
        syncProgress.setProgress(progress);
        syncProgress.setVisible(progress > 0 && progress < 1);
    }
}

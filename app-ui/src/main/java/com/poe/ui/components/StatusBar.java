package com.poe.ui.components;

import com.poe.ui.i18n.Messages;
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

    private final Label dataVersion;
    private final Label syncTime;
    private final Label status;
    private final ProgressBar syncProgress;

    public StatusBar() {
        getStyleClass().add("status-bar");
        setSpacing(16);

        dataVersion = new Label(Messages.get("status.data-version.prefix") + " —");
        dataVersion.getStyleClass().add("status-label");

        syncTime = new Label(Messages.get("status.sync-time.prefix") + " —");
        syncTime.getStyleClass().add("status-label");

        status = new Label(Messages.get("status.ready"));
        status.getStyleClass().add("status-label");

        syncProgress = new ProgressBar(0);
        syncProgress.setVisible(false);
        syncProgress.setPrefWidth(120);
        syncProgress.setMaxHeight(12);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(dataVersion, syncTime, spacer, status, syncProgress);
    }

    public void setDataVersion(String version) {
        dataVersion.setText(Messages.get("status.data-version.prefix") + " " + version);
    }

    public void setSyncTime(String time) {
        syncTime.setText(Messages.get("status.sync-time.prefix") + " " + time);
    }

    public void setStatus(String text) {
        status.setText(text);
    }

    public void setSyncProgress(double progress) {
        syncProgress.setProgress(progress);
        syncProgress.setVisible(progress > 0 && progress < 1);
    }
}

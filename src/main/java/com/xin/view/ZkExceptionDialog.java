package com.xin.view;

import javafx.application.Platform;
import org.controlsfx.dialog.ExceptionDialog;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
public class ZkExceptionDialog extends ExceptionDialog {
    public ZkExceptionDialog(String title, String content, Throwable exception) {
        super(exception);
        setHeaderText(title);
        setHeaderText(content);
    }

    public ZkExceptionDialog(String content, Throwable exception) {
        this("出现异常", content, exception);
    }

    public void showUi() {
        Platform.runLater(this::showAndWait);
    }

}

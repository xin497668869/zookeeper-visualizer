package com.xin.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
public class AlertUtils {

    public static void showErrorAlert(String title, String contentText) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(contentText);
        errorAlert.showAndWait();
    }

    public static void showInfoAlert(String title, String contentText) {
        Alert errorAlert = new Alert(AlertType.INFORMATION);
        errorAlert.setTitle(title);
        errorAlert.setContentText(contentText);
        TextArea textArea = new TextArea(contentText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);
        errorAlert.getDialogPane()
                  .setContent(gridPane);
        errorAlert.showAndWait();
    }
}

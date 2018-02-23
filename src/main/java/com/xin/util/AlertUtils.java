package com.xin.util;

import javafx.scene.control.Alert;

/**
 * @author linxixin@cvte.com
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

}

package com.xin.view;

import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * @author jurnlee
 * @date 2018/11/23
 */
public class AlertTemplate {


    public static void showTipAlert(boolean success, String succTip, String failTip) {
        Alert alert;
        if (success) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(succTip);
        } else {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(failTip);
        }
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initStyle(StageStyle.UTILITY);
        alert.show();
    }
}

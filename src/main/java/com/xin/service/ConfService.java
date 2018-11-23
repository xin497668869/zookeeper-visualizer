package com.xin.service;


import com.xin.ConfUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author jurnlee
 */
public class ConfService {

    private static ConfService confService = new ConfService();

    private ConfService() {
    }

    public static ConfService getService() {
        if (confService == null) {
            confService = new ConfService();
        }
        return confService;
    }

    public void saveConf(ConfUtil.Conf selectedItem, ListView<ConfUtil.Conf> confListView) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/confSetting.fxml"));
            Stage stage = new Stage();
            if (selectedItem != null) {
                TextField nameTextField = (TextField) root.lookup("#nameTextField");
                nameTextField.setText(selectedItem.getName());
                TextField addressTextField = (TextField) root.lookup("#addressTextField");
                addressTextField.setText(selectedItem.getAddress());
                TextField idTextField = (TextField) root.lookup("#idTextField");
                idTextField.setText(selectedItem.getId());
                stage.setTitle("zk配置修改");
            } else {
                stage.setTitle("zk配置新增");
            }
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene value = new Scene(root);
            stage.setScene(value);
            stage.showAndWait();
            ConfUtil.Conf conf = (ConfUtil.Conf) value.getUserData();
            if (conf != null) {
                ConfUtil.addOrUpdateConf(conf, confListView);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

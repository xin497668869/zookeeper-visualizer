package com.xin.service;


import com.alibaba.fastjson.JSON;
import com.xin.ConfUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @author jurnlee
 */
@Slf4j
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

    /**
     * 导出到文件，默认为json，若encodeBase64=true则编码成base64
     */
    public void startExportToFile(String title, String defaultFileName, Object content, boolean encodeBase64, Function<Boolean, Void> resultHandler) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(defaultFileName);
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file == null) {
            return;
        }
        boolean res = true;
        try {
            FileWriter fw = new FileWriter(file);
            String text = JSON.toJSONString(content);
            if (encodeBase64) {
                text = new BASE64Encoder().encode(text.getBytes(StandardCharsets.UTF_8));
            }
            fw.write(text);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            log.warn("导出配置异常：", e);
            res = false;
        }
        if (resultHandler != null) {
            resultHandler.apply(res);
        }
    }
}

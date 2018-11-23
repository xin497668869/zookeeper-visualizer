package com.xin.view;

import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author jurnlee
 * @date 2018/11/22
 */
public class AboutDialog extends Dialog<Void> {


    public AboutDialog() {
        getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
            close();
        });
        try {
            this.setContentText(IOUtils.toString(this.getClass().getClassLoader().getResource("about.txt").toURI(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            this.setContentText("欢迎使用zookeeper-visualizer！");
        }
        this.setTitle("关于我们");
        this.setHeight(400d);
        this.initModality(Modality.WINDOW_MODAL);
        this.initStyle(StageStyle.UTILITY);
    }

}

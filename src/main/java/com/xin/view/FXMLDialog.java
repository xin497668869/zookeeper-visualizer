package com.xin.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
public class FXMLDialog<T> {

    @Getter
    private Stage stage;
    @Getter
    private Scene scene;
    @Getter
    private T controller;
    private String xmlLocation;

    public FXMLDialog(String xmlLocation) {
        this.xmlLocation = xmlLocation;
    }

    public void init() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource(xmlLocation));
            loader.load();
            controller = loader.getController();
            scene = new Scene(loader.getRoot());
            stage = new Stage();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void show() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void setTitle(String title) {
        stage.setTitle(title);
    }
}

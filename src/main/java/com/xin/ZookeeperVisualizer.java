package com.xin; /**
 * @author linxixin@cvte.com
 * @since 1.0
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ZookeeperVisualizer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("jute.maxbuffer", String.valueOf(5120 * 1024));
        try {
            BorderPane root = FXMLLoader.load(getClass().getResource("/fxml/root.fxml"));

            primaryStage.setTitle("zookeeper-visualizer");
            primaryStage.getIcons()
                        .add(new Image(ZookeeperVisualizer.class.getClassLoader()
                                                                .getResourceAsStream("icons/root.png")));
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

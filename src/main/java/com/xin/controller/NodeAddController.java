package com.xin.controller;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.zookeeper.CreateMode;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
public class NodeAddController implements Initializable {
    public Label parentPathLabel;
    public RadioButton persistentRadioButton;
    public RadioButton ephemeralRadioButton;
    public TextArea nodeValueTextArea;
    public TextField nodeNameTextArea;
    public Button okButton;
    public RadioButton persistentSequentialRadioButton;
    public RadioButton ephemeralSequentialRadioButton;

    private ToggleGroup toggleGroup = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nodeValueTextArea.setText("");

        persistentRadioButton.setToggleGroup(toggleGroup);
        ephemeralRadioButton.setToggleGroup(toggleGroup);
        persistentSequentialRadioButton.setToggleGroup(toggleGroup);
        ephemeralSequentialRadioButton.setToggleGroup(toggleGroup);
        persistentRadioButton.setSelected(true);

    }

    public CreateMode getZkNodeTypeEnum() {
        if (persistentRadioButton.isSelected()) {
            return CreateMode.PERSISTENT;
        } else if (ephemeralRadioButton.isSelected()) {
            return CreateMode.EPHEMERAL;
        } else if (persistentSequentialRadioButton.isSelected()) {
            return CreateMode.PERSISTENT_SEQUENTIAL;
        } else if (ephemeralSequentialRadioButton.isSelected()) {
            return CreateMode.EPHEMERAL_SEQUENTIAL;
        } else {
            return CreateMode.PERSISTENT;
        }
    }

    public void mouseClickedToCreateNode(MouseEvent mouseEvent) {
        if (getZkNodeTypeEnum() != null) {
            String parentPath = parentPathLabel.getText();
            okButton.getScene()
                    .setUserData(
                            new NodeAddConf(parentPath + nodeNameTextArea.getText(), nodeValueTextArea.getText(), getZkNodeTypeEnum()));
            Stage window = (Stage) okButton.getScene()
                                           .getWindow();
            window.close();
        }
    }

    @Data
    @AllArgsConstructor
    public static class NodeAddConf {

        private String path;
        private String value;
        private CreateMode zkNodeType;

    }
}

package com.xin.controller;

import com.xin.ConfUtil;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class ConfSettingController implements Initializable {

    public TextField  nameTextField;
    public TextField  addressTextField;
    public BorderPane root;
    public Button     createConfButton;
    public TextField  idTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onClickToCreateSetting(MouseEvent mouseEvent) {
        ConfUtil.Conf conf = new ConfUtil.Conf(idTextField.getText(), nameTextField.getText(), addressTextField.getText());
        createConfButton.getScene().setUserData(conf);
        Stage window = (Stage) createConfButton.getScene().getWindow();
        window.close();
    }
}

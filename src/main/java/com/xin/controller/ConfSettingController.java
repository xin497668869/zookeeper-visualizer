package com.xin.controller;

import com.xin.ConfUtil;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.I0Itec.zkclient.ZkClient;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class ConfSettingController implements Initializable {

    public TextField  idTextField;
    public TextField  nameTextField;
    public TextField  addressTextField;
    public BorderPane root;
    public Button     createConfButton;
    public Button     testConnectButton;
    public Text       testResultMsg;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onClickToCreateSetting(MouseEvent mouseEvent) {
        ConfUtil.Conf conf = new ConfUtil.Conf(idTextField.getText(), nameTextField.getText(), addressTextField.getText());
        createConfButton.getScene().setUserData(conf);
        Stage window = (Stage) createConfButton.getScene().getWindow();
        window.close();
    }

    public void onClickToTestConnectSetting(MouseEvent mouseEvent) {
        testConnectButton.setDisable(true);

        testResultMsg.setText("连接中...");
        ConfUtil.Conf conf = new ConfUtil.Conf(idTextField.getText(), nameTextField.getText(), addressTextField.getText());

        new Thread(() -> {
            boolean isSuccess = true;
            ZkClient zkClient = null;
            try {
                zkClient = new ZkClient(conf.getAddress(), 5000);
                zkClient.connection();
            } catch (Exception e) {
                isSuccess = false;
                testResultMsg.setText("连接失败！");
            }finally {
                if (zkClient != null){
                    zkClient.close();
                }
            }
            if (isSuccess) {
                testResultMsg.setText("连接成功！");
            }
        }).start();
    }

    public void onInputTextChanged(KeyEvent event){
        if(!testResultMsg.getText().isEmpty()){
            testResultMsg.setText("");
        }
        if (testConnectButton.isDisabled()){
            testConnectButton.setDisable(false);
        }
    }
}

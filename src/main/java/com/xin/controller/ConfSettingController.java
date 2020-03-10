package com.xin.controller;

import com.xin.ZkConfService;
import com.xin.ZkConfService.ZkConf;
import com.xin.view.conf.ZkConfListView;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ConfSettingController implements Initializable {

    public TextField nameTextField;
    public TextField addressTextField;
    public BorderPane root;
    public Button createConfButton;
    public Button testConnectButton;
    public Text testResultMsg;
    private ZkConf zkConf;
    private ZkConfListView zkConfListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onClickToCreateConf(MouseEvent mouseEvent) {
        zkConf.setAddress(addressTextField.getText());
        zkConf.setName(nameTextField.getText());
        ZkConfService.getService()
                     .saveZkConf(zkConf, zkConfListView);
        Stage window = (Stage) createConfButton.getScene()
                                               .getWindow();
        window.close();
    }

    public void onClickToTestConnect(MouseEvent mouseEvent) {
        testConnectButton.setDisable(true);

        testResultMsg.setText("连接中...");

        new Thread(() -> {
            ZkClient zkClient = null;
            try {
                zkClient = new ZkClient(addressTextField.getText(), 5000, 5000);
                Platform.runLater(() -> testResultMsg.setText("连接成功！"));
            } catch (Exception e) {
                log.warn("连接异常", e);
                Platform.runLater(() -> {
                    testResultMsg.setText("连接失败！" + e.toString());

                });
            } finally {
                if (zkClient != null) {
                    zkClient.close();
                }
            }
            if (testConnectButton.isDisabled()) {
                testConnectButton.setDisable(false);
            }
        }).start();
    }

    public void init(ZkConfListView zkConfListView) {
        this.zkConfListView = zkConfListView;
        this.zkConf = new ZkConf();
        this.zkConf.setAddress("localhost:2181");
        initComponent();
    }

    public void init(ZkConf zkConf, ZkConfListView zkConfListView) {
        this.zkConfListView = zkConfListView;
        this.zkConf = zkConf;
        initComponent();
    }

    private void initComponent() {
        this.nameTextField.setText(zkConf.getName());
        this.addressTextField.setText(zkConf.getAddress());

    }
}

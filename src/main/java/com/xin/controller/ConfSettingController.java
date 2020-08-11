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
import org.I0Itec.zkclient.MyZkClient;

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
    public TextField connectTimeout;
    public TextField sessionTimeout;

    public BorderPane root;
    public Button createConfButton;
    public Button testConnectButton;
    public Text testResultMsg;
    private ZkConfListView zkConfListView;
    private String currentZkConfId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public ZkConf getCurrentUiZkConf() {
        return new ZkConf(currentZkConfId,
                          nameTextField.getText(),
                          addressTextField.getText(),
                          Integer.valueOf(sessionTimeout.getText()),
                          Integer.valueOf(connectTimeout.getText()));
    }

    public void onClickToCreateConf(MouseEvent mouseEvent) {

        ZkConfService.getService()
                     .saveZkConf(getCurrentUiZkConf(), zkConfListView);
        Stage window = (Stage) createConfButton.getScene()
                                               .getWindow();
        window.close();
    }

    public void onClickToTestConnect(MouseEvent mouseEvent) {
        testConnectButton.setDisable(true);

        testResultMsg.setText("连接中...");

        new Thread(() -> {
            MyZkClient zkClient = null;
            try {
                ZkConf zkConf = getCurrentUiZkConf();
                zkClient = new MyZkClient(zkConf.getAddress(),
                                          zkConf.getSessionTimeout(),
                                          zkConf.getConnectTimeout());
                Platform.runLater(() -> testResultMsg.setText("连接成功！"));
            } catch (Exception e) {
                log.warn("连接异常", e);
                Platform.runLater(() -> {
                    testResultMsg.setText("连接失败！\n" + e.getMessage());

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
        currentZkConfId = null;
        initComponent(new ZkConf(null,
                                 "",
                                 "localhost:2181",
                                 5000,
                                 5000));
    }

    public void init(ZkConf zkConf, ZkConfListView zkConfListView) {
        this.zkConfListView = zkConfListView;
        currentZkConfId = zkConf.getId();
        initComponent(zkConf);
    }

    private void initComponent(ZkConf zkConf) {
        this.nameTextField.setText(zkConf.getName());
        this.connectTimeout.setText(String.valueOf(zkConf.getConnectTimeout()));
        this.sessionTimeout.setText(String.valueOf(zkConf.getSessionTimeout()));
        this.addressTextField.setText(zkConf.getAddress());
        this.connectTimeout.setText(String.valueOf(zkConf.getConnectTimeout()));
    }
}

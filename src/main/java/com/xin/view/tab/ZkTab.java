package com.xin.view.tab;

import com.xin.ConfUtil;
import com.xin.ZkClientWithUi;
import com.xin.ZkNode;
import com.xin.view.SearchTextField;
import com.xin.view.zktreeview.ZkTreeView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.controlsfx.control.MaskerPane;

import java.io.IOException;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkTab extends Tab {
    public static final String     SPLIT_PANE = "#splitPane";
    private final       MaskerPane maskerPane = new MaskerPane();
    private final ConfUtil.Conf  conf;
    @Getter
    private       ZkClientWithUi zkClientWithUi;

    public ZkTab(ConfUtil.Conf conf) {
        super(conf.toString());
        this.conf = conf;
    }

    public void init(ZkClient zkClient) {
        Parent parent = null;

        try {
            parent = FXMLLoader.load(getClass().getResource("/connectTab.fxml"));

            StackPane stackPane = new StackPane();
            stackPane.setPadding(new Insets(10, 0, 0, 0));
            stackPane.getChildren().add(parent);

            stackPane.getChildren().add(maskerPane);
            maskerPane.setVisible(false);
            maskerPane.setText(conf + " 连接异常, 重连中...");
            zkClientWithUi = new ZkClientWithUi(zkClient, maskerPane);
            setContent(stackPane);

            zkClient.subscribeStateChanges(new IZkStateListener() {
                @Override
                public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                    if (state == Watcher.Event.KeeperState.SyncConnected) {
                        //当我重新启动后start，监听触发
                        log.info("重连成功 " + conf);
                        maskerPane.setVisible(false);
                    } else if (state == Watcher.Event.KeeperState.Disconnected) {
                        maskerPane.setVisible(true);
                    }
                }

                @Override
                public void handleNewSession() throws Exception {
                    maskerPane.setVisible(false);
                }

                @Override
                public void handleSessionEstablishmentError(Throwable error) throws Exception {
                    log.error("zk异常" + error);
                }
            });
            ZkTreeView treeView = (ZkTreeView) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(0).lookup("#zkTreeView");
            SearchTextField searchZkNodeTextField = (SearchTextField) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(0).lookup("#searchZkNodeTextField");

            TextArea zkNodeDataTextArea = (TextArea) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(1).lookup("#zkNodeDataTextArea");
            TextField zkPathTextField = (TextField) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(1).lookup("#zkPathTextField");
            TextArea zkNodeStatTextArea = (TextArea) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(1).lookup("#zkNodeStatTextArea");
            Button reloadNodeValueButton = (Button) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(1).lookup("#reloadNodeValueButton");
            Button saveNodeValueButton = (Button) ((SplitPane) parent.lookup(SPLIT_PANE)).getItems().get(1).lookup("#saveNodeValueButton");

            reloadNodeValueButton.setOnMouseClicked(event -> {
                treeView.fireZkValueReload();
                saveNodeValueButton.setDisable(true);
            });


            saveNodeValueButton.setOnMouseClicked(event -> {
                log.info("点击保存准备保存zk数据 " + zkPathTextField.getText() + "  " + zkNodeDataTextArea.getText());
                String value = zkNodeDataTextArea.getText();
                zkClient.writeData(zkPathTextField.getText(), value);
                saveNodeValueButton.setDisable(true);
            });
            /**
             * 当焦点不在节点上的时候需要清空数据, 灰化按钮
             */
            treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    reloadNodeValueButton.setDisable(true);
                    zkNodeDataTextArea.setText("");
                    zkNodeDataTextArea.setDisable(true);
                    zkNodeStatTextArea.setText("");
                    zkPathTextField.setText("");
                } else {
                    zkNodeDataTextArea.setDisable(false);
                    reloadNodeValueButton.setDisable(false);
                }
                saveNodeValueButton.setDisable(true);
            });

            treeView.init(zkClientWithUi, searchZkNodeTextField, zkNodeDataTextArea, zkPathTextField, zkNodeStatTextArea);


            setOnCloseRequest(event -> zkClient.close());

            zkNodeDataTextArea.setOnKeyPressed(event -> {
                TreeItem<ZkNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    saveNodeValueButton.setDisable(false);
                }
            });

        } catch (IOException e) {
            log.error("加载ui资源失败 /connectTab.fxml", e);
            return;
        }
    }
}

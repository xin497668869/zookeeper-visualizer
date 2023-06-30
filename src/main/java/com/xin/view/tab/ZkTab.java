package com.xin.view.tab;

import com.xin.ZkClientWrap;
import com.xin.ZkConfService.ZkConf;
import com.xin.view.NodeInfoEditProxy;
import com.xin.view.zktreeview.SearchTextField;
import com.xin.view.zktreeview.ZkTreeView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher;
import org.controlsfx.control.MaskerPane;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
@Slf4j
@Getter
public class ZkTab extends Tab {
    public static final String SPLIT_PANE = "#splitPane";
    private final ZkConf zkConf;
    private MaskerPane loadingMaskerPane = new MaskerPane();
    @Getter
    private ZkClientWrap zkClientWrap;
    private ZkTreeView treeView;
    private SearchTextField searchZkNodeTextField;

    private NodeInfoEditProxy nodeInfoEditProxy;

    public ZkTab(ZkConf zkConf) {
        super(zkConf.toString());
        this.zkConf = zkConf;
    }

    public void initField(Node contentNode, ZkClientWrap zkClientWrap) {
        Node node = ((SplitPane) contentNode.lookup(SPLIT_PANE)).getItems()
                                                                .get(0);
        treeView = (ZkTreeView) node.lookup("#zkTreeView");
        searchZkNodeTextField = (SearchTextField) node.lookup("#searchZkNodeTextField");

        Node node1 = ((SplitPane) contentNode.lookup(SPLIT_PANE)).getItems()
                                                                 .get(1);
        TextArea zkNodeDataTextArea = (TextArea) node1.lookup("#zkNodeDataTextArea");

        TextField zkPathTextField = (TextField) node1.lookup("#zkPathTextField");
        TextArea zkNodeStatTextArea = (TextArea) node1.lookup("#zkNodeStatTextArea");
        Button reloadNodeValueButton = (Button) node1.lookup("#reloadNodeValueButton");
        Button saveNodeValueButton = (Button) node1.lookup("#saveNodeValueButton");

        Button commandStatButton = (Button) node1.lookup("#commandStat");
        Button commandRuokButton = (Button) node1.lookup("#commandRuok");
//        Button commandDumpButton = (Button) node1.lookup("#commandDump");
        Button commandSrvrButton = (Button) node1.lookup("#commandSrvr");
        Button commandConfButton = (Button) node1.lookup("#commandConf");
        Button commandEnviButton = (Button) node1.lookup("#commandEnvi");
        Button commandReqsButton = (Button) node1.lookup("#commandReqs");
        Button commandConsButton = (Button) node1.lookup("#commandCons");
        Button commandWchcButton = (Button) node1.lookup("#commandWchc");
        Button commandWchpButton = (Button) node1.lookup("#commandWchp");
        loadingMaskerPane.setVisible(false);
        loadingMaskerPane.setText(zkConf + " 连接异常, 重连中...");
        nodeInfoEditProxy = new NodeInfoEditProxy(zkClientWrap,
                                                  zkNodeDataTextArea,
                                                  zkPathTextField,
                                                  zkNodeStatTextArea,
                                                  reloadNodeValueButton,
                                                  saveNodeValueButton,
                                                  Arrays.asList(commandStatButton,
                                                                commandRuokButton,
//                                                                commandDumpButton,
                                                                commandSrvrButton,
                                                                commandConfButton,
                                                                commandEnviButton,
                                                                commandReqsButton,
                                                                commandConsButton,
                                                                commandWchcButton,
                                                                commandWchpButton));
        nodeInfoEditProxy.init();
    }

    public void init(ZkClientWrap zkClientWrap) {

        try {
            Parent parent = buildUi(zkClientWrap);

            initField(parent, zkClientWrap);

            installZkStateChange(zkClientWrap);

            setOnCloseRequest(event -> zkClientWrap.close());

            treeView.init(this.zkClientWrap, searchZkNodeTextField, nodeInfoEditProxy);

        } catch (IOException e) {
            log.error("加载ui资源失败 /connectTab.fxml", e);
            return;
        }
    }

    private Parent buildUi(ZkClientWrap zkClientWrap) throws IOException {
        Parent parent;
        parent = FXMLLoader.load(getClass().getResource("/fxml/connectTab.fxml"));

        StackPane stackPane = new StackPane();
        stackPane.setPadding(new Insets(10, 0, 0, 0));
        stackPane.getChildren()
                 .add(parent);
        stackPane.getChildren()
                 .add(loadingMaskerPane);

        this.zkClientWrap = zkClientWrap;
        setContent(stackPane);
        return parent;
    }

    private void installZkStateChange(ZkClientWrap zkClientWrap) {
        zkClientWrap.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                if (state == Watcher.Event.KeeperState.SyncConnected) {
                    //当我重新启动后start，监听触发
                    log.info("重连成功 " + zkConf);
                    loadingMaskerPane.setVisible(false);
                } else if (state == Watcher.Event.KeeperState.Disconnected) {
                    loadingMaskerPane.setVisible(true);
                }
            }

            @Override
            public void handleNewSession() throws Exception {
                loadingMaskerPane.setVisible(false);
            }

            @Override
            public void handleSessionEstablishmentError(Throwable error) throws Exception {
                log.error("zk异常" + error);
            }
        });
    }
}

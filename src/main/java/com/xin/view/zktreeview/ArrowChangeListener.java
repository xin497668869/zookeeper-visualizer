package com.xin.view.zktreeview;

import com.xin.ZkClientWithUi;
import com.xin.ZkNode;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;

import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ArrowChangeListener implements ChangeListener<Boolean>, IZkChildListener {

    private final ZkClientWithUi   zkClientWithUi;
    private final TreeItem<ZkNode> zkNodeTreeItem;
    private final ZkTreeView       zkTreeView;

    public ArrowChangeListener(ZkClientWithUi zkClientWithUi, TreeItem<ZkNode> zkNodeTreeItem, ZkTreeView zkTreeView) {
        this.zkClientWithUi = zkClientWithUi;
        this.zkNodeTreeItem = zkNodeTreeItem;
        this.zkTreeView = zkTreeView;
    }

    private void fire() {
        zkClientWithUi.subscribeChildChanges(zkNodeTreeItem.getValue().getPath(), this);
        List<String> currentChilds = zkClientWithUi.getChildren(zkNodeTreeItem.getValue().getPath());
        zkTreeView.refreshByParent(zkNodeTreeItem, currentChilds);
        zkTreeView.refresh();

    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            fire();
        } else {
            zkClientWithUi.unsubscribeChildChanges(zkNodeTreeItem.getValue().getPath(), this);
            zkTreeView.closeChildren(zkNodeTreeItem);
        }
    }


    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) {
        //这个没有子节点, 可能是删除自己了
        if(currentChilds== null) {
            return ;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                zkTreeView.refreshByParent(zkNodeTreeItem, currentChilds);
                zkTreeView.refresh();
            }
        });
    }
}

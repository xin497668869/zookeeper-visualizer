package com.xin.view.zktreeview;

import com.xin.ZkClientWrap;
import com.xin.view.ZkNodeTreeItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;

import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ArrowChangeListener implements ChangeListener<Boolean>, IZkChildListener {

    private final ZkClientWrap zkClientWrap;
    private final ZkNodeTreeItem zkNodeTreeItem;

    public ArrowChangeListener(ZkClientWrap zkClientWrap, ZkNodeTreeItem zkNodeTreeItem) {
        this.zkClientWrap = zkClientWrap;
        this.zkNodeTreeItem = zkNodeTreeItem;
    }

    private void fire() {
        zkClientWrap.subscribeChildChanges(zkNodeTreeItem.getValue()
                                                         .getPath(), this);
        List<String> currentChilds = zkClientWrap.getChildren(zkNodeTreeItem.getValue()
                                                                            .getPath());
        zkNodeTreeItem.refreshByParent(currentChilds);
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            fire();
        } else {
            zkClientWrap.unsubscribeChildChanges(zkNodeTreeItem.getValue()
                                                               .getPath(), this);
            zkNodeTreeItem.closeChildren();
        }
    }


    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) {
        //这个没有子节点, 可能是删除自己了
        if(currentChilds== null) {
            return ;
        }
        Platform.runLater(() -> zkNodeTreeItem.refreshByParent(currentChilds));
    }
}

package com.xin.view.zktreeview;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import com.xin.view.NodeInfoEditProxy;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import org.I0Itec.zkclient.IZkDataListener;

/**
 * 监听zk树节点的焦点变化, 如果选择了其他节点需要出发显示右边界面的情况
 *
 * @author 497668869@qq.com
 * @since 1.0
 */
public class ChangeSelectDataChangeListener implements ChangeListener<TreeItem<ZkNode>> {

    private final ZkClientWrap zkClientWrap;
    private final NodeInfoEditProxy nodeInfoEditProxy;

    private IZkDataListener listener = new IZkDataListener() {
        @Override
        public void handleDataChange(String dataPath, Object data) {
            nodeInfoEditProxy.updateDate(dataPath);
        }

        @Override
        public void handleDataDeleted(String dataPath) {
            nodeInfoEditProxy.selectNoNode();
        }
    };

    ChangeSelectDataChangeListener(ZkClientWrap zkClientWrap, NodeInfoEditProxy nodeInfoEditProxy) {
        this.zkClientWrap = zkClientWrap;
        this.nodeInfoEditProxy = nodeInfoEditProxy;
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<ZkNode>> observable, TreeItem<ZkNode> oldValue, TreeItem<ZkNode> newValue) {
        nodeInfoEditProxy.setCurrentTreeItem(newValue);
        if (oldValue != null) {
            zkClientWrap.unsubscribeDataChanges(oldValue.getValue()
                                                        .getPath(), listener);
        }
        nodeInfoEditProxy.disabledComponent(newValue == null);
        if (newValue != null) {
            zkClientWrap.subscribeDataChanges(newValue.getValue()
                                                      .getPath(), listener);

            nodeInfoEditProxy.updateDate(newValue.getValue()
                                                 .getPath());
        }

    }

}

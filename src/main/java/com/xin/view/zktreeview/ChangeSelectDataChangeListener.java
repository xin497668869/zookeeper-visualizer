package com.xin.view.zktreeview;

import com.xin.ZkClientWithUi;
import com.xin.ZkNode;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.zookeeper.data.Stat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 监听zk树节点的焦点变化, 如果选择了其他节点需要出发显示右边界面的情况
 *
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class ChangeSelectDataChangeListener implements ChangeListener<TreeItem<ZkNode>> {

    private final ZkClientWithUi zkClientWithUi;
    private final TextArea       zkNodeDataTextArea;
    private final TextField      nodePathTextField;
    private final TextArea       zkNodeStatTextArea;

    private IZkDataListener listener = new IZkDataListener() {
        @Override
        public void handleDataChange(String dataPath, Object data) {
            zkNodeDataTextArea.setText(String.valueOf(data));
            nodePathTextField.setText(dataPath);
            updateNodeValue(dataPath);
        }

        @Override
        public void handleDataDeleted(String dataPath) {
            nodePathTextField.setText(dataPath);
            zkNodeDataTextArea.setText("");
        }
    };

    ChangeSelectDataChangeListener(ZkClientWithUi zkClientWithUi, TextArea zkNodeDataTextArea, TextField nodePathTextField, TextArea zkNodeStatTextArea) {
        this.zkClientWithUi = zkClientWithUi;
        this.zkNodeDataTextArea = zkNodeDataTextArea;
        this.nodePathTextField = nodePathTextField;
        this.zkNodeStatTextArea = zkNodeStatTextArea;
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<ZkNode>> observable, TreeItem<ZkNode> oldValue, TreeItem<ZkNode> newValue) {
        if (oldValue != null) {
            zkClientWithUi.unsubscribeDataChanges(oldValue.getValue().getPath(), listener);
        }
        if (newValue != null) {
            zkClientWithUi.subscribeDataChanges(newValue.getValue().getPath(), listener);

            updateNodeValue(newValue.getValue().getPath());
        }


    }

    private void updateNodeValue(String nodePath) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                nodePathTextField.setText(nodePath);
                Stat stat = new Stat();
                String value = zkClientWithUi.readData(nodePath, stat);
                zkNodeDataTextArea.setText(value);
                zkNodeStatTextArea.setText(formatStat(stat));
            }
        });
    }

    public ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public String formatStat(Stat stat) {
        return String.format("cZxid = %X\n" +
                                     "ctime = %s\n" +
                                     "mZxid = %X\n" +
                                     "mtime = %s\n" +
                                     "pZxid = %X\n" +
                                     "cversion = %X\n" +
                                     "dataVersion = %X\n" +
                                     "aclVersion = %X\n" +
                                     "ephemeralOwner = %X\n" +
                                     "dataLength = %X\n" +
                                     "numChildren = %X",
                             stat.getCzxid(),
                             simpleDateFormat.get().format(new Date(stat.getCtime())),
                             stat.getMzxid(),
                             simpleDateFormat.get().format(new Date(stat.getMtime())),
                             stat.getPzxid(),
                             stat.getCversion(),
                             stat.getVersion(),
                             stat.getAversion(),
                             stat.getEphemeralOwner(),
                             stat.getDataLength(),
                             stat.getNumChildren()
        );
    }
}

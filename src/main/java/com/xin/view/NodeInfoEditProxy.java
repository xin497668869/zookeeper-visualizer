package com.xin.view;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.data.Stat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class NodeInfoEditProxy {
    private final TextArea zkNodeStatTextArea;
    private final TextArea zkNodeDataTextArea;
    private final TextField zkPathTextField;
    private final Button reloadNodeValueButton;
    private final Button saveNodeValueButton;
    private final ZkClientWrap zkClientWrap;
    public ThreadLocal<SimpleDateFormat> simpleDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public NodeInfoEditProxy(ZkClientWrap zkClientWrap,
                             TextArea zkNodeDataTextArea,
                             TextField zkPathTextField,
                             TextArea zkNodeStatTextArea,
                             Button reloadNodeValueButton,
                             Button saveNodeValueButton) {
        this.zkClientWrap = zkClientWrap;
        this.zkNodeDataTextArea = zkNodeDataTextArea;
        this.zkPathTextField = zkPathTextField;
        this.zkNodeStatTextArea = zkNodeStatTextArea;
        this.reloadNodeValueButton = reloadNodeValueButton;
        this.saveNodeValueButton = saveNodeValueButton;
    }

    public void init() {
        installSaveDataAction(zkClientWrap);
        installReloadDataAction();
    }

    public void updateDate(String nodePath) {
        zkPathTextField.setText(nodePath);
        Stat stat = new Stat();
        String value = zkClientWrap.readData(nodePath, stat);
        zkNodeDataTextArea.setText(String.valueOf(value));
        zkNodeStatTextArea.setText(formatStat(stat));
    }

    public void reloadInfo(ZkNode value) {
        if (value == null) {
            reloadNodeValueButton.setDisable(true);
            zkNodeDataTextArea.setText("");
            zkNodeDataTextArea.setDisable(true);
            zkNodeStatTextArea.setText("");
            zkPathTextField.setText("");
        } else {
            zkNodeDataTextArea.setDisable(false);
            reloadNodeValueButton.setDisable(false);
        }
    }

    private String formatStat(Stat stat) {
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
                             simpleDateFormat.get()
                                             .format(new Date(stat.getCtime())),
                             stat.getMzxid(),
                             simpleDateFormat.get()
                                             .format(new Date(stat.getMtime())),
                             stat.getPzxid(),
                             stat.getCversion(),
                             stat.getVersion(),
                             stat.getAversion(),
                             stat.getEphemeralOwner(),
                             stat.getDataLength(),
                             stat.getNumChildren()
        );
    }

    private void installReloadDataAction() {
        reloadNodeValueButton.setOnMouseClicked(event -> {
            saveNodeValueButton.setDisable(true);
        });
    }

    private void installSaveDataAction(ZkClientWrap zkClientWrap) {

        saveNodeValueButton.setOnMouseClicked(event -> {
            log.info("点击保存准备保存zk数据 " + zkPathTextField.getText() + "  " + zkNodeDataTextArea.getText());
            String value = zkNodeDataTextArea.getText();
            zkClientWrap.writeData(zkPathTextField.getText(), value);
            saveNodeValueButton.setDisable(true);
        });
    }
}

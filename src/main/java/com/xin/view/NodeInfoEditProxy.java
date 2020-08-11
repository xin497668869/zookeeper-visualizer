package com.xin.view;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import com.xin.util.AlertUtils;
import com.xin.util.CommandUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.data.Stat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    private final List<Button> commandButtons;
    public ThreadLocal<SimpleDateFormat> simpleDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    @Getter
    private TreeItem<ZkNode> currentTreeItem;

    public NodeInfoEditProxy(ZkClientWrap zkClientWrap,
                             TextArea zkNodeDataTextArea,
                             TextField zkPathTextField,
                             TextArea zkNodeStatTextArea,
                             Button reloadNodeValueButton,
                             Button saveNodeValueButton,
                             List<Button> commandButtons) {
        this.zkClientWrap = zkClientWrap;
        this.zkNodeDataTextArea = zkNodeDataTextArea;
        this.zkPathTextField = zkPathTextField;
        this.zkNodeStatTextArea = zkNodeStatTextArea;
        this.reloadNodeValueButton = reloadNodeValueButton;
        this.saveNodeValueButton = saveNodeValueButton;
        this.commandButtons = commandButtons;
//        this.zkPatDecodeTextField = zkPatDecodeTextField;
    }

    public void init() {
        installSaveDataAction(zkClientWrap);
        installReloadDataAction();
        for (Button commandButton : commandButtons) {
            commandButton.setOnAction(event -> {
                String command = commandButton.getText()
                                              .trim();
                try {
                    String result = CommandUtils.result(command, zkClientWrap.getZkConf());
                    AlertUtils.showInfoAlert(command + " 结果", result);
                } catch (Throwable e) {
                    log.error("请求命令异常", e);
                    new ZkExceptionDialog("请求命令异常", e).showUi();
                }

            });
        }
    }

    public void updateDate(String nodePath) {
        Stat stat = new Stat();
        String value = zkClientWrap.readData(nodePath, stat);
        Platform.runLater(() -> {
            zkPathTextField.setText(nodePath);
            zkNodeDataTextArea.setText(String.valueOf(value));
            zkNodeStatTextArea.setText(formatStat(stat));
        });
    }

    public void disabledComponent(boolean disabled) {
        Platform.runLater(() -> {
            if (disabled) {
                reloadNodeValueButton.setDisable(true);
                zkNodeDataTextArea.setDisable(true);
                zkNodeStatTextArea.setDisable(true);
            } else {
                zkNodeDataTextArea.setDisable(false);
                reloadNodeValueButton.setDisable(false);
                zkNodeStatTextArea.setDisable(false);
            }
        });
    }

    public void selectNoNode() {
        zkPathTextField.setText("");
        zkNodeDataTextArea.setText("");
        zkNodeStatTextArea.setText("");
    }

    public void setCurrentTreeItem(TreeItem<ZkNode> newValue) {
        this.currentTreeItem = newValue;
    }

    private String formatStat(Stat stat) {
        return String.format("cZxid = %X\n" +
                                     "\nctime = 0x%s\n" +
                                     "mZxid = 0x%X\n" +
                                     "mtime = 0x%s\n" +
                                     "pZxid = 0x%X\n" +
                                     "cversion = %d\n" +
                                     "dataVersion = %d\n" +
                                     "aclVersion = %d\n" +
                                     "ephemeralOwner = 0x%X\n" +
                                     "dataLength = %d\n" +
                                     "numChildren = %d",
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
        reloadNodeValueButton.setOnAction(event -> {
            TreeItem<ZkNode> currentTreeItem = getCurrentTreeItem();
            if (currentTreeItem != null) {
                updateDate(currentTreeItem.getValue()
                                          .getPath());
            }
        });
    }

    private void installSaveDataAction(ZkClientWrap zkClientWrap) {
        saveNodeValueButton.setOnAction(event -> {
            log.info("点击保存准备保存zk数据 " + zkPathTextField.getText() + "  " + zkNodeDataTextArea.getText());
            String value = zkNodeDataTextArea.getText();
            zkClientWrap.writeData(zkPathTextField.getText(), value);
        });
    }
}

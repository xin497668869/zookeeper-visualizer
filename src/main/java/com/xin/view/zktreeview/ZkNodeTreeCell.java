package com.xin.view.zktreeview;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import com.xin.controller.NodeAddController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkNodeTreeCell extends TreeCell<ZkNode> {
    private final ZkTreeView zkTreeView;
    private final ZkClientWrap zkClientWrap;
    EventHandler<MouseEvent> mouseEventEventHandler = event -> {
        getSelectionModel().clearSelection();
        event.consume();
    };
    private EventHandler<ActionEvent> deleteNodeAction = getDeleteNodeAction();
    private EventHandler<ActionEvent> addNodeAction = getAddNodeAction();
    private EventHandler<ActionEvent> expandNodeAction = getExpandNodeAction();
    private EventHandler<ActionEvent> unExpandNodeAction = getUnExpandNodeAction();

    public ZkNodeTreeCell(ZkTreeView zkTreeView, ZkClientWrap zkClientWrap) {
        this.zkTreeView = zkTreeView;
        this.zkClientWrap = zkClientWrap;
    }

    public boolean deleteZkNode(TreeItem<ZkNode> zkNodeTreeItem) {
        log.info("准备删除节点 " + zkNodeTreeItem.getValue()
                                           .getPath());
        try {
            return zkClientWrap.deleteRecursive(zkNodeTreeItem.getValue()
                                                              .getPath());
        } catch (Exception e) {
            log.error("删除节点失败 ", e);
        }

        return false;
    }

    @Override
    protected void updateItem(ZkNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || getIndex() < 0) {
            setText(null);
            setGraphic(null);
            addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
        } else {

            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setTextFill(Color.BLACK);
            setText(item.getName());
            setGraphic(null);

            installContextMenu();

            removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
        }

        treeItemDoubleClick();
    }

    private void treeItemDoubleClick() {
        setEventDispatcher((event, tail) -> {
            if (event instanceof MouseEvent) {
                if (((MouseEvent) event).getButton() == MouseButton.PRIMARY
                        && event.getEventType()
                                .equals(MOUSE_PRESSED)
                        && ((MouseEvent) event).getClickCount() == 2) {

                    TreeItem<ZkNode> selectedItem = getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        selectedItem.setExpanded(!selectedItem.isExpanded());
                    }
                    return event;
                }
            }
            return getEventDispatcher()
                    .dispatchEvent(event, tail);
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TreeCellSkin<>(this);
    }

    private SelectionModel<TreeItem<ZkNode>> getSelectionModel() {
        return zkTreeView.getSelectionModel();
    }

    private void installContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addNode = new MenuItem("新增节点");
        addNode.setOnAction(addNodeAction);
        contextMenu.getItems()
                   .add(addNode);

        MenuItem deleteNode = new MenuItem("删除节点");
        deleteNode.setOnAction(deleteNodeAction);
        contextMenu.getItems()
                   .add(deleteNode);

        MenuItem expandNode = new MenuItem("展开所有节点");
        expandNode.setOnAction(expandNodeAction);
        contextMenu.getItems()
                   .add(expandNode);

        MenuItem unExpandNode = new MenuItem("收缩所有节点");
        unExpandNode.setOnAction(unExpandNodeAction);
        contextMenu.getItems()
                   .add(unExpandNode);
        setContextMenu(contextMenu);

    }

    private EventHandler<ActionEvent> getUnExpandNodeAction() {
        return event -> {
            ZkNode value = getSelectionModel().getSelectedItem()
                                              .getValue();
            unExpandAllChildren(value);
        };
    }

    private EventHandler<ActionEvent> getExpandNodeAction() {
        return event -> {
            ZkNode value = getSelectionModel().getSelectedItem()
                                              .getValue();
            expandAllChildren(value);
        };

    }

    private void expandAllChildren(ZkNode zkNode) {
        zkNode.getTreeItem()
              .setExpanded(true);
        if (zkNode.getChildren() == null) {
            return;
        }
        for (ZkNode child : zkNode.getChildren()) {
            expandAllChildren(child);
        }
    }

    private void unExpandAllChildren(ZkNode zkNode) {
        zkNode.getTreeItem()
              .setExpanded(false);
        if (zkNode.getChildren() == null) {
            return;
        }
        for (ZkNode child : zkNode.getChildren()) {
            unExpandAllChildren(child);
        }
    }

    private EventHandler<ActionEvent> getDeleteNodeAction() {
        return event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("删除节点提示");
            alert.setHeaderText(null);
            alert.setContentText("准备删除节点 path: " + getSelectionModel().getSelectedItem()
                                                                      .getValue()
                                                                      .getPath());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (!deleteZkNode(getSelectionModel().getSelectedItem())) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("节点删除失败 path: " + getSelectionModel().getSelectedItem()
                                                                             .getValue()
                                                                             .getPath());
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("准备删除节点 ");
                    errorAlert.showAndWait();
                }
            }
        };
    }

    private EventHandler<ActionEvent> getAddNodeAction() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Parent parent = FXMLLoader.load(getClass().getResource("/nodeAdd.fxml"));
                    Label parentPathLabel = (Label) parent.lookup("#parentPathLabel");
                    TreeItem<ZkNode> selectedItem = getSelectionModel()
                            .getSelectedItem();
                    if ("/".equals(selectedItem.getValue()
                                               .getPath())) {
                        parentPathLabel.setText(selectedItem.getValue()
                                                            .getPath());
                    } else {
                        parentPathLabel.setText(selectedItem.getValue()
                                                            .getPath() + "/");
                    }
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("zk节点新增");
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.showAndWait();

                    NodeAddController.NodeAddConf nodeAddConf = (NodeAddController.NodeAddConf) scene.getUserData();
                    if (nodeAddConf != null) {
                        log.info("准备创建zk节点 " + nodeAddConf);
                        zkClientWrap.create(nodeAddConf.getPath(), nodeAddConf.getValue(), nodeAddConf.getZkNodeType());
                    }

                } catch (IOException e) {
                    log.error("生成nodeAdd控件失败", e);
                }
            }
        };
    }
}

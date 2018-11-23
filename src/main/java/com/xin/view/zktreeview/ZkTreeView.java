package com.xin.view.zktreeview;

import com.xin.ZkClientWithUi;
import com.xin.ZkNode;
import com.xin.ZkNodeInfo;
import com.xin.controller.NodeAddController;
import com.xin.service.ConfService;
import com.xin.view.AlertTemplate;
import com.xin.view.SearchTextField;
import com.xin.view.TreeCellSkin;
import javafx.event.ActionEvent;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.zookeeper.data.Stat;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;

import java.io.IOException;
import java.util.*;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkTreeView extends TreeView<ZkNode> {

    private ZkClientWithUi zkClientWithUi;
    private ZkNode                    root               = new ZkNode("/", "/");
    private TreeItem<ZkNode>          rootZkNodeTreeItem = new TreeItem<>(root);
    private EventHandler<ActionEvent> deleteNodeAction   = getDeleteNodeAction();
    private EventHandler<ActionEvent> addNodeAction      = getAddNodeAction();
    private EventHandler<ActionEvent> exportNodeAction = getExportNodeAction();
    private ChangeSelectDataChangeListener selectToDataChangeListener;
    private SearchTextField                searchZkNodeTextField;

    private void resetBySearch(String text) {
        resetBySearch(root, text);
        refresh();
    }

    public boolean isMatchSearch(ZkNode parent, String text) {
        if (parent.getChildren() == null) {
            return false;
        }
        for (ZkNode zkNode : parent.getChildren()) {
            if (text == null || text.isEmpty()) {
                return true;
            } else if (zkNode.getName().toLowerCase().contains(text.toLowerCase())) {
                return true;
            }
            resetBySearch(zkNode, text);
        }
        return false;
    }

    private void resetBySearch(ZkNode parent, String text) {
        if (parent.getChildren() == null) {
            return;
        }
        for (ZkNode zkNode : parent.getChildren()) {
            if (text == null || text.isEmpty()) {
                zkNode.setHighLight(false);
            } else if (zkNode.getName().toLowerCase().contains(text.toLowerCase())) {
                zkNode.setHighLight(true);
            } else {
                zkNode.setHighLight(false);
            }
            resetBySearch(zkNode, text);
        }
    }


    public void init(ZkClientWithUi zkClient, SearchTextField searchZkNodeTextField, TextArea nodeDataTextArea, TextField zkNodeDataTextArea, TextArea zkNodeStatTextArea) {
        this.zkClientWithUi = zkClient;
        this.searchZkNodeTextField = searchZkNodeTextField;
        initRootItem(rootZkNodeTreeItem);


        EventHandler<MouseEvent> mouseEventEventHandler = event -> {
            getSelectionModel().clearSelection();
            event.consume();
        };

        setCellFactory(new Callback<TreeView<ZkNode>, TreeCell<ZkNode>>() {
            @Override
            public TreeCell<ZkNode> call(TreeView<ZkNode> param) {
                return new TreeCell<ZkNode>() {

                    private TextFlow buildTextFlow(String text, String filter) {
                        int filterIndex = text.toLowerCase().indexOf(filter.toLowerCase());
                        Text textBefore = new Text(text.substring(0, filterIndex));
                        Text textAfter = new Text(text.substring(filterIndex + filter.length()));
                        Text textFilter = new Text(text.substring(filterIndex, filterIndex + filter.length()));
                        textFilter.setFill(Color.ORANGE);
                        textFilter.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
                        return new TextFlow(textBefore, textFilter, textAfter);
                    }

                    @Override
                    protected void updateItem(ZkNode item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || getIndex() < 0) {
                            setText(null);
                            setGraphic(null);
                            addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
                        } else {
                            if (item.isHighLight()) {
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                setGraphic(buildTextFlow(item.getName(), searchZkNodeTextField.getText()));
                                setText(null);
                            } else {
                                setContentDisplay(ContentDisplay.TEXT_ONLY);
                                setTextFill(Color.BLACK);
                                setText(item.getName());
                                setGraphic(null);
                            }
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem addNode = new MenuItem("新增节点");
                            addNode.setOnAction(addNodeAction);
                            contextMenu.getItems().add(addNode);

                            MenuItem deleteNode = new MenuItem("删除节点");
                            deleteNode.setOnAction(deleteNodeAction);
                            contextMenu.getItems().add(deleteNode);

                            MenuItem exportNode = new MenuItem("导出节点");
                            exportNode.setOnAction(exportNodeAction);
                            contextMenu.getItems().add(exportNode);


                            setContextMenu(contextMenu);
                            removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
                        }

                        EventDispatcher originalDispatcher = getEventDispatcher();
                        setEventDispatcher((event, tail) -> {
                            if (event instanceof MouseEvent) {
                                if (((MouseEvent) event).getButton() == MouseButton.PRIMARY
                                        && event.getEventType().equals(MOUSE_PRESSED)
                                        && ((MouseEvent) event).getClickCount() == 2) {

                                    TreeItem<ZkNode> selectedItem = getSelectionModel().getSelectedItem();
                                    boolean expanded = selectedItem.isExpanded();
                                    selectedItem.setExpanded(!expanded);
                                    return event;
                                }
                            }
                            return originalDispatcher.dispatchEvent(event, tail);
                        });
                    }

                    @Override
                    protected Skin<?> createDefaultSkin() {
                        return new TreeCellSkin<>(this);
                    }
                };
            }
        });

        selectToDataChangeListener = new ChangeSelectDataChangeListener(zkClient, nodeDataTextArea, zkNodeDataTextArea, zkNodeStatTextArea);

        getSelectionModel().selectedItemProperty().addListener(selectToDataChangeListener);

        searchZkNodeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            resetBySearch(newValue);
            refresh();
        });

        EventHandler<KeyEvent> keyEventHandler = event -> {
            searchZkNodeTextField.selectEnd();
            searchZkNodeTextField.fireEvent(new KeyEvent(searchZkNodeTextField, searchZkNodeTextField, event.getEventType(), event.getCharacter(), event.getText(), event.getCode(), event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown()));

        };
        setOnKeyTyped(keyEventHandler);

        setOnKeyPressed(keyEventHandler);
        setOnKeyReleased(keyEventHandler);

        searchZkNodeTextField.validate((Control c, String newValue) ->
                                               ValidationResult.fromMessageIf(c, "没搜索到", Severity.ERROR, !isMatchSearch(root, newValue)));
        refresh();
    }

    public void fireZkValueReload() {
        if (selectToDataChangeListener != null) {
            selectToDataChangeListener.changed(null, getSelectionModel().getSelectedItem(), getSelectionModel().getSelectedItem());
        }
    }

    private EventHandler<ActionEvent> getDeleteNodeAction() {
        return event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("删除节点提示");
            alert.setHeaderText(null);
            alert.setContentText("准备删除节点 path: " + getSelectionModel().getSelectedItem().getValue().getPath());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (!deleteZkNode(getSelectionModel().getSelectedItem())) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("节点删除失败 path: " + getSelectionModel().getSelectedItem().getValue().getPath());
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
                    TreeItem<ZkNode> selectedItem = ZkTreeView.super.getSelectionModel().getSelectedItem();
                    if (rootZkNodeTreeItem.equals(selectedItem)) {
                        parentPathLabel.setText(selectedItem.getValue().getPath());
                    } else {
                        parentPathLabel.setText(selectedItem.getValue().getPath() + "/");
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
                        zkClientWithUi.create(nodeAddConf.getPath(), nodeAddConf.getValue(), nodeAddConf.getZkNodeType());
                    }

                } catch (IOException e) {
                    log.error("生成nodeAdd控件失败", e);
                }
            }
        };
    }


    public boolean deleteZkNode(TreeItem<ZkNode> zkNodeTreeItem) {
        log.info("准备删除节点 " + zkNodeTreeItem.getValue().getPath());
        try {
            return zkClientWithUi.deleteRecursive(zkNodeTreeItem.getValue().getPath());
        } catch (Exception e) {
            log.error("删除节点失败 ", e);
        }

        return false;
    }

    private EventHandler<ActionEvent> getExportNodeAction() {
        ZkNodeInfo zkNodeInfo = new ZkNodeInfo();

        return event -> {
            ZkNode currNode = getSelectionModel().getSelectedItem().getValue();
            if (currNode == null) {
                return;
            }
            zkNodeInfo.setName(currNode.getName());
            zkNodeInfo.setPath(currNode.getPath());

            List<String> subs = zkClientWithUi.getChildren(currNode.getPath());
            getSubTreeNodes(currNode.getPath(), subs, zkNodeInfo);

            ConfService.getService().startExportToFile("Save Resource File", currNode.getName() + ".json",
                    zkNodeInfo, false, (res) -> {
                        AlertTemplate.showTipAlert(res, "导出成功！", "导出失败！");
                        return null;
                    });
            event.consume();
        };
    }

    private void getSubTreeNodes(String currPath, List<String> subs, ZkNodeInfo zkNodeInfo) {
        List<ZkNodeInfo> subList = new ArrayList<>();
        subs.forEach(node -> {
            String nodePath = currPath + "/" + node;
            try {
                ZkNodeInfo info = new ZkNodeInfo().setName(node).setPath(nodePath).setData(zkClientWithUi.readData(nodePath, new Stat()));
                subList.add(info);
                zkNodeInfo.setChildren(subList);
                getSubTreeNodes(nodePath, zkClientWithUi.getChildren(nodePath), info);
            } catch (Exception e) {
                log.info("获取节点[{}]的子节点失败，可能已经发生了变更", nodePath);
            }
        });
    }

    /**
     * 跟节点初始化, 添加箭头监听, 展开第二层
     *
     * @param rootZkNodeTreeItem
     */
    private void initRootItem(TreeItem<ZkNode> rootZkNodeTreeItem) {
        setRoot(rootZkNodeTreeItem);
        ArrowChangeListener listener = new ArrowChangeListener(zkClientWithUi, rootZkNodeTreeItem, this);
        rootZkNodeTreeItem.expandedProperty().addListener(listener);
        rootZkNodeTreeItem.setExpanded(true);
    }


    /**
     * 根据父节点刷新子节点信息
     *
     * @param zkNodeTreeItem
     * @param children
     */
    synchronized void refreshByParent(TreeItem<ZkNode> zkNodeTreeItem, List<String> children) {
        log.info(zkNodeTreeItem.getValue().getPath() + " 这个节点有变化");

        List<String> newChildren = new ArrayList<>(children);

        //判断哪些是需要删除的节点, 对需要删除的节点进行删除
        ZkNode zkNode = zkNodeTreeItem.getValue();
        if (zkNode.getChildren() != null) {
            Iterator<ZkNode> iterator = zkNode.getChildren().iterator();
            while (iterator.hasNext()) {
                ZkNode childZkNode = iterator.next();
                if (!children.contains(childZkNode.getName())) {
                    iterator.remove();
                    removeTreeItem(zkNodeTreeItem, childZkNode.getName());
                }
                newChildren.remove(childZkNode.getName());
            }
        }
        //对新增的zk节点进行添加
        for (String childName : newChildren) {
            String path;
            if (root.equals(zkNode)) {
                path = "/" + childName;
            } else {
                path = zkNode.getPath() + "/" + childName;
            }
            ZkNode childNode = new ZkNode(path, childName);
            zkNode.addChild(childNode);

            TreeItem<ZkNode> treeItem = new TreeItem<>(childNode);
            treeItem.expandedProperty().addListener(new ArrowChangeListener(zkClientWithUi, treeItem, this));

            zkNodeTreeItem.getChildren().add(treeItem);
        }
        /**
         * 保证和zk里面的排序是一样的
         */
        zkNodeTreeItem.getChildren().sort(Comparator.comparing(zkNodeTreeItem1 -> children.indexOf(zkNodeTreeItem1.getValue().getName())));
    }

    private void removeTreeItem(TreeItem<ZkNode> zkNodeTreeItem, String nodeName) {
        Iterator<TreeItem<ZkNode>> iterator = zkNodeTreeItem.getChildren().iterator();
        while (iterator.hasNext()) {
            TreeItem<ZkNode> treeItem = iterator.next();
            if (treeItem.getValue().getName().equals(nodeName)) {
                closeChildren(treeItem);
                iterator.remove();
            }
        }
    }


    /**
     * 删除所有节点信息, 主要是要撤销监听器
     *
     * @param zkNodeTreeItem
     */
    public void closeChildren(TreeItem<ZkNode> zkNodeTreeItem) {
        if (zkNodeTreeItem.getChildren() == null) {
            return;
        }
        for (TreeItem<ZkNode> nodeTreeItem : zkNodeTreeItem.getChildren()) {

            Map<String, Set<IZkChildListener>> childListener = zkClientWithUi.getZkClient().getChildListener();
            String path = nodeTreeItem.getValue().getPath();
            if(childListener.containsKey(path)) {
                childListener.get(path).clear();
            }

            Map<String, Set<IZkDataListener>> dataListener = zkClientWithUi.getZkClient().getDataListener();
            if(dataListener.containsKey(path)) {
                dataListener.get(path).clear();
            }

            closeChildren(nodeTreeItem);
        }
        zkNodeTreeItem.getChildren().clear();
        zkNodeTreeItem.getValue().setChildren(null);
    }

}

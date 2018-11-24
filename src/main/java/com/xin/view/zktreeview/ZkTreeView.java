package com.xin.view.zktreeview;

import com.xin.ZkClientWithUi;
import com.xin.ZkNode;
import com.xin.controller.NodeAddController;
import com.xin.util.match.FList;
import com.xin.util.match.MinusculeMatcher;
import com.xin.util.match.TextRange;
import com.xin.view.FilterableTreeItem;
import com.xin.view.SearchTextField;
import com.xin.view.TreeCellSkin;
import javafx.event.ActionEvent;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkTreeView extends TreeView<ZkNode> {

    private ZkClientWithUi            zkClientWithUi;
    private ZkNode                    root               = new ZkNode("/", "/");
    private FilterableTreeItem        rootZkNodeTreeItem = new FilterableTreeItem(root);
    private EventHandler<ActionEvent> deleteNodeAction   = getDeleteNodeAction();
    private EventHandler<ActionEvent> addNodeAction      = getAddNodeAction();
    private EventHandler<ActionEvent> expandNodeAction   = getExpandNodeAction();
    private EventHandler<ActionEvent> unExpandNodeAction = getUnExpandNodeAction();

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

    public void isMatch(ZkNode zkNode, ZkNode parent, String text) {
        zkNode.setParent(parent);
        if (text == null || text.isEmpty()) {
            zkNode.setHighLight(true);
            zkNode.setMatchSegments(FList.emptyList());
        } else {
            MinusculeMatcher minusculeMatcher = new MinusculeMatcher(text, MinusculeMatcher.MatchingCaseSensitivity.NONE, "");
            FList<TextRange> textRanges = minusculeMatcher.matchingFragments(zkNode.getName());
            if (textRanges != null && !textRanges.isEmpty()) {
                zkNode.setMatchSegments(textRanges);
                zkNode.setHighLight(true);
                ZkNode parentZkNode = zkNode.getParent();
                while (parentZkNode != null) {
                    parentZkNode.setHighLight(true);
                    parentZkNode = parentZkNode.getParent();
                }
            } else {
                zkNode.setHighLight(false);
                zkNode.setMatchSegments(FList.emptyList());
            }
        }
    }

    private void resetBySearch(ZkNode parent, String text) {
        rootZkNodeTreeItem.refilter();
        if (parent.getChildren() == null) {
            return;
        }

        for (ZkNode zkNode : parent.getChildren()) {
            isMatch(zkNode, parent, text);
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

                    private TextFlow buildTextFlow(FList<TextRange> matchSegments, String text) {
                        List<Text> texts = new ArrayList<>();
                        int start = 0;
                        for (TextRange matchSegment : matchSegments) {
                            if (start < matchSegment.getStartOffset()) {
                                texts.add(new Text(text.substring(start, matchSegment.getStartOffset())));
                            }
                            Text hightLight = new Text(text.substring(matchSegment.getStartOffset(), matchSegment.getEndOffset()));
                            hightLight.setFill(Color.ORANGE);
                            hightLight.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
                            texts.add(hightLight);
                            start = matchSegment.getEndOffset();
                        }
                        if (start < text.length()) {
                            texts.add(new Text(text.substring(start)));
                        }
                        return new TextFlow(texts.toArray(new Node[texts.size()]));
                    }

                    @Override
                    protected void updateItem(ZkNode item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || getIndex() < 0) {
                            setText(null);
                            setGraphic(null);
                            addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
                        } else {
                            if (item.isHighLight() && !item.getMatchSegments().isEmpty()) {
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                setGraphic(buildTextFlow(item.getMatchSegments(), item.getName()));
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


                            MenuItem expandNode = new MenuItem("展开所有节点");
                            expandNode.setOnAction(expandNodeAction);
                            contextMenu.getItems().add(expandNode);


                            MenuItem unExpandNode = new MenuItem("收缩所有节点");
                            unExpandNode.setOnAction(unExpandNodeAction);
                            contextMenu.getItems().add(unExpandNode);
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

    private void expandAllChildren(ZkNode zkNode) {
        zkNode.getTreeItem().setExpanded(true);
        if (zkNode.getChildren() == null) {
            return;
        }
        for (ZkNode child : zkNode.getChildren()) {
            expandAllChildren(child);
        }
    }

    private void unExpandAllChildren(ZkNode zkNode) {
        zkNode.getTreeItem().setExpanded(false);
        if (zkNode.getChildren() == null) {
            return;
        }
        for (ZkNode child : zkNode.getChildren()) {
            unExpandAllChildren(child);
        }
    }

    private EventHandler<ActionEvent> getUnExpandNodeAction() {
        return event -> {
            ZkNode value = getSelectionModel().getSelectedItem().getValue();
            unExpandAllChildren(value);
        };
    }

    private EventHandler<ActionEvent> getExpandNodeAction() {
        return event -> {
            ZkNode value = getSelectionModel().getSelectedItem().getValue();
            expandAllChildren(value);
        };

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

            zkClientWithUi.unsubscribeChildChanges(zkNodeTreeItem.getValue().getPath());
            return zkClientWithUi.deleteRecursive(zkNodeTreeItem.getValue().getPath());
        } catch (Exception e) {
            log.error("删除节点失败 ", e);
        }

        return false;
    }

    /**
     * 跟节点初始化, 添加箭头监听, 展开第二层
     *
     * @param rootZkNodeTreeItem
     */
    private void initRootItem(FilterableTreeItem rootZkNodeTreeItem) {
        setRoot(rootZkNodeTreeItem);
        root.setTreeItem(rootZkNodeTreeItem);
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
    synchronized void refreshByParent(FilterableTreeItem zkNodeTreeItem, List<String> children) {
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
            isMatch(childNode, zkNode, searchZkNodeTextField.getText());

            FilterableTreeItem treeItem = new FilterableTreeItem(childNode);
            childNode.setTreeItem(treeItem);
            treeItem.expandedProperty().addListener(new ArrowChangeListener(zkClientWithUi, treeItem, this));

            zkNodeTreeItem.getInternalChildren().add(treeItem);
        }
        /**
         * 保证和zk里面的排序是一样的
         */
        zkNodeTreeItem.getInternalChildren().sort(Comparator.comparing(zkNodeTreeItem1 -> children.indexOf(zkNodeTreeItem1.getValue().getName())));
    }

    private void removeTreeItem(TreeItem<ZkNode> zkNodeTreeItem, String nodeName) {
        Iterator<TreeItem<ZkNode>> iterator = zkNodeTreeItem.getChildren().iterator();
        while (iterator.hasNext()) {
            TreeItem<ZkNode> treeItem = iterator.next();
            if (treeItem.getValue().getName().equals(nodeName)) {
                closeChildren(treeItem);
            }
        }
        closeChildren(zkNodeTreeItem);
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
            if (childListener.containsKey(path)) {
                childListener.get(path).clear();
            }

            Map<String, Set<IZkDataListener>> dataListener = zkClientWithUi.getZkClient().getDataListener();
            if (dataListener.containsKey(path)) {
                dataListener.get(path).clear();
            }

            closeChildren(nodeTreeItem);
        }
        zkNodeTreeItem.getChildren().clear();
        zkNodeTreeItem.getValue().setChildren(null);
    }

}

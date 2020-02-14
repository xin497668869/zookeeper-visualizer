package com.xin.view.zktreeview;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import com.xin.util.FuzzyMatchUtils;
import com.xin.util.match.StringUtil;
import com.xin.view.NodeInfoEditProxy;
import com.xin.view.ZkNodeTreeItem;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkTreeView extends TreeView<ZkNode> {

    private ZkClientWrap zkClientWrap;
    private ChangeSelectDataChangeListener selectToDataChangeListener;
    private SearchTextField searchZkNodeTextField;

    public void init(ZkClientWrap zkClient, SearchTextField searchZkNodeTextField, NodeInfoEditProxy nodeInfoEditProxy) {
        this.zkClientWrap = zkClient;
        this.searchZkNodeTextField = searchZkNodeTextField;
        ZkNodeTreeItem rootZkNodeTreeItem = initRootItem();

        setCellFactory(param -> new ZkNodeTreeCell(this, zkClientWrap));

        selectToDataChangeListener = new ChangeSelectDataChangeListener(zkClient, nodeInfoEditProxy);

        getSelectionModel().selectedItemProperty()
                           .addListener(selectToDataChangeListener);

        searchZkNodeTextField.textProperty()
                             .addListener((observable, oldValue, newValue) -> {
                                 filterBySearchValue(rootZkNodeTreeItem, newValue);
                                 updateShowTreeItems(rootZkNodeTreeItem);

                             });

        pressKeyToSearchInputText(searchZkNodeTextField);

        refresh();
    }

    private void updateShowTreeItems(ZkNodeTreeItem rootZkNodeTreeItem) {
        rootZkNodeTreeItem.updateShowTreeItems();
        for (TreeItem<ZkNode> source : rootZkNodeTreeItem.getSources()) {
            ((ZkNodeTreeItem) source).updateShowTreeItems();
        }
    }

    private boolean filterBySearchValue(ZkNodeTreeItem rootZkNodeTreeItem,
                                        String newValue) {
        if (StringUtil.isEmpty(newValue)) {
            rootZkNodeTreeItem.setShow(true);
        } else {
            rootZkNodeTreeItem.setShow(FuzzyMatchUtils.match(rootZkNodeTreeItem.getValue()
                                                                               .getName(), newValue));
        }

        for (TreeItem<ZkNode> child : rootZkNodeTreeItem.getSources()) {
            ZkNodeTreeItem zkNodeTreeItem = (ZkNodeTreeItem) child;
            if (filterBySearchValue(zkNodeTreeItem, newValue)) {
                rootZkNodeTreeItem.setShow(true);
            }
        }
        return rootZkNodeTreeItem.isShow();

    }

    /**
     * 随意在TreeView输入都可以在搜索框识别得到
     */
    private void pressKeyToSearchInputText(SearchTextField searchZkNodeTextField) {
        EventHandler<KeyEvent> keyEventHandler = event -> {
            searchZkNodeTextField.selectEnd();
            searchZkNodeTextField.fireEvent(
                    new KeyEvent(searchZkNodeTextField, searchZkNodeTextField, event.getEventType(), event.getCharacter(), event.getText(),
                                 event.getCode(), event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown()));

        };
        setOnKeyTyped(keyEventHandler);

        setOnKeyPressed(keyEventHandler);
        setOnKeyReleased(keyEventHandler);
    }

//    private void getSubTreeNodes(String currPath, List<String> subs, ZkNodeInfo zkNodeInfo) {
//        List<ZkNodeInfo> subList = new ArrayList<>();
//        subs.forEach(node -> {
//            String nodePath = currPath + "/" + node;
//            try {
//                ZkNodeInfo info = new ZkNodeInfo().setName(node)
//                                                  .setPath(nodePath)
//                                                  .setData(zkClientWrap.readData(nodePath, new Stat()));
//                subList.add(info);
//                zkNodeInfo.setChildren(subList);
//                getSubTreeNodes(nodePath, zkClientWrap.getChildren(nodePath), info);
//            } catch (Exception e) {
//                log.info("获取节点[{}]的子节点失败，可能已经发生了变更", nodePath);
//            }
//        });
//    }

    /**
     * 跟节点初始化, 添加箭头监听, 展开第二层
     */
    private ZkNodeTreeItem initRootItem() {
        ZkNode root = new ZkNode("/", "/");
        ZkNodeTreeItem rootZkNodeTreeItem = new ZkNodeTreeItem(zkClientWrap, root);
        setRoot(rootZkNodeTreeItem);
        root.setTreeItem(rootZkNodeTreeItem);
        rootZkNodeTreeItem.setExpanded(true);
        return rootZkNodeTreeItem;
    }

}

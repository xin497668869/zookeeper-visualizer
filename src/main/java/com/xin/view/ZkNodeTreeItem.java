package com.xin.view;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import com.xin.view.zktreeview.ArrowChangeListener;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkNodeTreeItem extends TreeItem<ZkNode> {

    private final ZkClientWrap zkClientWrap;

    @Getter
    @Setter
    private boolean show = true;
    @Getter
    private List<TreeItem<ZkNode>> sources = new ArrayList<>();

    public ZkNodeTreeItem(ZkClientWrap zkClientWrap, ZkNode value) {
        super(value);
        ArrowChangeListener listener = new ArrowChangeListener(zkClientWrap, this);
        this.zkClientWrap = zkClientWrap;
        this.expandedProperty()
            .addListener(listener);
    }

    public void updateShowTreeItems() {
        getChildren().clear();
        for (TreeItem<ZkNode> source : sources) {
            if (((ZkNodeTreeItem) source).isShow()) {
                getChildren().add(source);
            }
        }
    }

    /**
     * 删除所有节点信息, 主要是要撤销监听器
     */
    public void closeChildren() {
        if (sources == null) {
            return;
        }
        for (TreeItem<ZkNode> nodeTreeItem : sources) {

            String path = nodeTreeItem.getValue()
                                      .getPath();
            zkClientWrap.unsubscribeChildChanges(path);

            zkClientWrap.unsubscribeDataChanges(path);

            ((ZkNodeTreeItem) nodeTreeItem).closeChildren();
        }
        if (sources != null) {
            sources.clear();
        }
        getValue().setChildren(null);
    }

    /**
     * 根据父节点刷新子节点信息
     */
    public synchronized void refreshByParent(List<String> children) {
        log.info(getValue().getPath() + " 这个节点有变化");

        List<String> newChildren = new ArrayList<>(children);

        //判断哪些是需要删除的节点, 对需要删除的节点进行删除
        ZkNode zkNode = getValue();
        if (zkNode.getChildren() != null) {
            Iterator<ZkNode> iterator = zkNode.getChildren()
                                              .iterator();
            while (iterator.hasNext()) {
                ZkNode childZkNode = iterator.next();
                if (!children.contains(childZkNode.getName())) {
                    iterator.remove();
                    removeTreeItem(childZkNode.getName());
                }
                newChildren.remove(childZkNode.getName());
            }
        }
        //对新增的zk节点进行添加
        for (String childName : newChildren) {
            String path;
            if ("/".equals(zkNode.getPath())) {
                path = "/" + childName;
            } else {
                path = zkNode.getPath() + "/" + childName;
            }
            ZkNode childNode = new ZkNode(path, childName);
            zkNode.addChild(childNode);
            ZkNodeTreeItem treeItem = new ZkNodeTreeItem(zkClientWrap, childNode);
            childNode.setTreeItem(treeItem);

            addChildren(treeItem);
        }
        updateShowTreeItems();
        sources.sort(Comparator.comparing(zkNodeTreeItem1 -> children.indexOf(zkNodeTreeItem1.getValue()
                                                                                             .getName())));
    }

    private void addChildren(ZkNodeTreeItem treeItem) {
        sources.add(treeItem);

    }

    private void removeTreeItem(String nodeName) {
        Iterator<TreeItem<ZkNode>> iterator = sources.iterator();
        TreeItem<ZkNode> findItem = null;
        while (iterator.hasNext()) {
            TreeItem<ZkNode> treeItem = iterator.next();
            if (treeItem.getValue()
                        .getName()
                        .equals(nodeName)) {
                ((ZkNodeTreeItem) treeItem).closeChildren();
                findItem = treeItem;
            }
        }
        if (findItem != null) {

            String path = findItem.getValue()
                                  .getPath();

            zkClientWrap.unsubscribeDataChanges(path);
            zkClientWrap.unsubscribeChildChanges(path);

            sources.remove(findItem);
        }
    }

}
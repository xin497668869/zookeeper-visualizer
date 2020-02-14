package com.xin.view;

import com.xin.ZkClientWrap;
import com.xin.ZkNode;
import com.xin.view.zktreeview.ArrowChangeListener;
import javafx.scene.control.TreeItem;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkNodeTreeItem extends TreeItem<ZkNode> {

    private final ZkClientWrap zkClientWrap;

    public ZkNodeTreeItem(ZkClientWrap zkClientWrap, ZkNode value) {
        super(value);
        ArrowChangeListener listener = new ArrowChangeListener(zkClientWrap, this);
        this.zkClientWrap = zkClientWrap;
        this.expandedProperty()
            .addListener(listener);
//        this.sourceList = FXCollections.observableArrayList();
//        this.filteredList = new SearchFilterObservalbeList<>(this.sourceList, new Predicate<TreeItem<ZkNode>>() {
//            @Override
//            public boolean test(TreeItem<ZkNode> zkNodeTreeItem) {
////                return zkNodeTreeItem.getValue().isHighLight();
//                return true;
//            }
//        });
        ;
//        setHiddenFieldChildren(this.filteredList);
    }

    public void addChildren(ZkNodeTreeItem treeItem) {
        getChildren()
                .add(treeItem);
    }

    /**
     * 删除所有节点信息, 主要是要撤销监听器
     */
    public void closeChildren() {
        if (getChildren() == null) {
            return;
        }
        for (TreeItem<ZkNode> nodeTreeItem : getChildren()) {

            Map<String, Set<IZkChildListener>> childListener = zkClientWrap.getZkClient()
                                                                           .getChildListener();
            String path = nodeTreeItem.getValue()
                                      .getPath();
            if (childListener.containsKey(path)) {
                childListener.get(path)
                             .clear();
            }

            Map<String, Set<IZkDataListener>> dataListener = zkClientWrap.getZkClient()
                                                                         .getDataListener();
            if (dataListener.containsKey(path)) {
                dataListener.get(path)
                            .clear();
            }

            ((ZkNodeTreeItem) nodeTreeItem).closeChildren();
        }
        if (getChildren() != null) {
            getChildren()
                    .clear();
        }
        getValue()
                .setChildren(null);
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

        getChildren()
                .sort(Comparator.comparing(zkNodeTreeItem1 -> children.indexOf(zkNodeTreeItem1.getValue()
                                                                                              .getName())));
    }

    private void removeTreeItem(String nodeName) {
        Iterator<TreeItem<ZkNode>> iterator = getChildren().iterator();
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
            Map<String, Set<IZkChildListener>> childListener = zkClientWrap.getZkClient()
                                                                           .getChildListener();
            String path = findItem.getValue()
                                  .getPath();
            if (childListener.containsKey(path)) {
                childListener.get(path)
                             .clear();
            }

            Map<String, Set<IZkDataListener>> dataListener = zkClientWrap.getZkClient()
                                                                         .getDataListener();
            if (dataListener.containsKey(path)) {
                dataListener.get(path)
                            .clear();
            }
            getChildren()
                    .remove(findItem);
        }
    }

//    public void setPredicate(Predicate predicate) {
//        this.predicate.set(predicate);
//    }

//    protected void setHiddenFieldChildren(ObservableList<TreeItem<ZkNode>> list) {
//        try {
//            Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
//            childrenField.setAccessible(true);
//            childrenField.set(this, list);
//
//            Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
//            declaredField.setAccessible(true);
//            list.addListener((ListChangeListener<? super TreeItem<ZkNode>>) declaredField.get(this));
//        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//            throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
//        }
//    }

//    public ObservableList<TreeItem<ZkNode>> getInternalChildren() {
//        return this.sourceList;
//    }

}
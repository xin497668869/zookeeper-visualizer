package com.xin;

import com.xin.view.ZkNodeTreeItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class ZkNode {
    @Getter
    private String             path;
    private String             name;
    private boolean            childExpand;
    private List<ZkNode>       children;
    private ZkNode parent;
    private ZkNodeTreeItem treeItem;

    public ZkNode(String path, String name) {
        this.path = path;
        this.name = name;
        childExpand = false;
    }

    public ZkNode getParent() {
        return parent;
    }

    public void setParent(ZkNode parent) {
        this.parent = parent;
    }



    public void addChild(ZkNode zkNode) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(zkNode);
    }

    @Override
    public String toString() {
        return name;
    }

    public ZkNodeTreeItem getTreeItem() {
        return treeItem;
    }

    public void setTreeItem(ZkNodeTreeItem treeItem) {
        this.treeItem = treeItem;
    }

}

package com.xin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class ZkNode {

    private String       path;
    private String       name;
    private boolean      isHighLight;
    private List<ZkNode> children;

    public ZkNode(String path, String name) {
        this.path = path;
        this.name = name;
        isHighLight = false;
    }

    public void setHighLight(boolean highLight) {
        isHighLight = highLight;
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
}

package com.xin.view;

import com.xin.ZkNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class FilterableTreeItem extends TreeItem<ZkNode> {
    final private ObservableList<TreeItem<ZkNode>>       sourceList;
    private       TreeItemFilteredList<TreeItem<ZkNode>> filteredList;
    private       ObjectProperty<Predicate<ZkNode>>      predicate = new SimpleObjectProperty<>();

    public void refilter() {
        filteredList.refilter();
    }

    public FilterableTreeItem(ZkNode value) {
        super(value);
        this.sourceList = FXCollections.observableArrayList();
        this.filteredList = new TreeItemFilteredList<>(this.sourceList, new Predicate<TreeItem<ZkNode>>() {
            @Override
            public boolean test(TreeItem<ZkNode> zkNodeTreeItem) {
                return zkNodeTreeItem.getValue().isHighLight();
            }
        });
        predicate.set(new Predicate<ZkNode>() {
            @Override
            public boolean test(ZkNode zkNode) {
                return zkNode.isHighLight();
            }
        });
        setHiddenFieldChildren(this.filteredList);
    }

    public void setPredicate(Predicate predicate) {
        this.predicate.set(predicate);
    }

    protected void setHiddenFieldChildren(ObservableList<TreeItem<ZkNode>> list) {
        try {
            Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
            childrenField.setAccessible(true);
            childrenField.set(this, list);

            Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
            declaredField.setAccessible(true);
            list.addListener((ListChangeListener<? super TreeItem<ZkNode>>) declaredField.get(this));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
        }
    }

    public ObservableList<TreeItem<ZkNode>> getInternalChildren() {
        return this.sourceList;
    }


}
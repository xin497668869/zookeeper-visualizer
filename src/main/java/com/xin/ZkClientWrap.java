package com.xin;

import com.xin.util.AlertUtils;
import com.xin.view.zktreeview.ArrowChangeListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkClientWrap {
    @Getter
    private final ZkClient zkClient;

    public ZkClientWrap(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public synchronized void unsubscribeDataChanges(String path, IZkDataListener listener) {
        try {
            zkClient.unsubscribeDataChanges(path, listener);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("取消监听节点数据变化信息异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
    }

    public synchronized void subscribeDataChanges(String path, IZkDataListener listener) {
        try {
            zkClient.subscribeDataChanges(path, listener);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("新增监听节点数据变化信息异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
    }

    public synchronized String readData(String nodePath, Stat stat) {
        try {
            return zkClient.readData(nodePath, stat);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("读取节点数据异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
        return "";
    }

    public synchronized void create(String path, String value, CreateMode zkNodeType) {
        try {
            zkClient.create(path, value, zkNodeType);
        } catch (ZkNodeExistsException e) {
            AlertUtils.showErrorAlert("创建节点异常, 节点已存在", e.getMessage());
            log.error("创建节点异常, 节点已存在", e);
        } catch (IllegalArgumentException e) {
            AlertUtils.showErrorAlert("创建节点异常", e.getMessage());
            log.error("创建节点异常", e);
        } catch (ZkInterruptedException e) {
            AlertUtils.showErrorAlert("创建节点异常, 操作被打断", e.getMessage());
            log.error("创建节点异常, 操作被打断", e);
        } catch (Exception e) {
            if (e.getCause() instanceof KeeperException.NoChildrenForEphemeralsException) {
                AlertUtils.showErrorAlert("创建节点异常, 临时节点不能有子节点", e.getMessage());
            } else {
                AlertUtils.showErrorAlert("创建节点异常", e.getMessage());
            }

            log.error("zkClient执行异常", e);
        }
    }

    public synchronized boolean deleteRecursive(String path) {
        try {
            return zkClient.deleteRecursive(path);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("删除节点异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
        return false;
    }

    public synchronized void subscribeChildChanges(String path, ArrowChangeListener arrowChangeListener) {
        try {
            zkClient.subscribeChildChanges(path, arrowChangeListener);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("新增监听节点值变化异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
    }

    public synchronized List<String> getChildren(String path) {
        try {
            return zkClient.getChildren(path);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("获取子节点数据异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
        return new ArrayList<>();
    }

    public synchronized void unsubscribeChildChanges(String path, ArrowChangeListener arrowChangeListener) {
        try {
            zkClient.unsubscribeChildChanges(path, arrowChangeListener);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("取消监听节点值变化异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
    }

    public synchronized void unsubscribeChildChanges(String path) {
        try {
            zkClient.unsubscribeChildChanges(path);
        } catch (Exception e) {
            AlertUtils.showErrorAlert("取消监听节点值变化异常", e.getMessage());
            log.error("zkClient执行异常", e);
        }
    }

    public void subscribeStateChanges(IZkStateListener iZkStateListener) {
        zkClient.subscribeStateChanges(iZkStateListener);

    }

    public void writeData(String path, String value) {
        zkClient.writeData(path, value);
    }

    public void close() {
        zkClient.close();
    }
}

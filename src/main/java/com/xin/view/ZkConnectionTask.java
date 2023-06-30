package com.xin.view;

import com.xin.ZkConfService.ZkConf;
import com.xin.util.ZkUtils;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.MyZkClient;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
@Slf4j
public class ZkConnectionTask extends Task<MyZkClient> {
    @Getter
    private ZkConf zkConf;
    @Getter
    private MyZkClient zkClient;

    public ZkConnectionTask(ZkConf zkConf) {
        this.zkConf = zkConf;
    }

    @Override
    protected MyZkClient call() throws Exception {
        zkClient = ZkUtils.getConnection(zkConf);

        return zkClient;
    }
}

package com.xin.view;

import com.xin.ConfUtil;
import com.xin.util.ZkUtils;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkConnectionTask extends Task<ZkClient> {
    @Getter
    private ConfUtil.Conf conf;
    @Getter
    private ZkClient      zkClient;

    public ZkConnectionTask(ConfUtil.Conf conf) {
        this.conf = conf;
    }

    @Override
    protected ZkClient call() throws Exception {
        zkClient = ZkUtils.getConnection(conf);
        try {
            zkClient.connection();
        }catch (Exception e){
            e.printStackTrace();
            zkClient.close();
            throw e;
        }
        return zkClient;
    }
}

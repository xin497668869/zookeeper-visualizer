package com.xin.util;

import com.xin.ZkConfService.ZkConf;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.StandardCharsets;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class ZkUtils {
    public static void init() {
    }

    public static ZkClient getConnection(ZkConf zkConf) {
        ZkClient zkClient = new ZkClient(zkConf.getAddress(), 5000, 5000, new ZkSerializer() {
            @Override
            public byte[] serialize(Object data) throws ZkMarshallingError {
                if (data == null) {
                    return new byte[]{};
                } else if (data instanceof String) {
                    return ((String) data).getBytes();
                } else {
                    throw new RuntimeException("不支持的序列化类型 " + data.getClass());
                }
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }, 1000);
        return zkClient;
    }
}

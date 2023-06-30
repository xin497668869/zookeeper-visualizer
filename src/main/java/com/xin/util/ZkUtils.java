package com.xin.util;

import com.xin.ZkConfService.ZkConf;
import org.I0Itec.zkclient.MyZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.StandardCharsets;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
public class ZkUtils {
    public static void init() {
    }

    public static MyZkClient getConnection(ZkConf zkConf) {
        return new MyZkClient(zkConf.getAddress(),
                              zkConf.getSessionTimeout(),
                              zkConf.getConnectTimeout(),
                              new ZkSerializer() {
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
    }
}

package com.xin;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author jurnlee
 * @date 2018/11/23
 */
@Data
@Accessors(chain = true)
public class ZkNodeInfo implements Serializable {

    private String path;

    private String name;

    private String data;

    private List<ZkNodeInfo> children;


}

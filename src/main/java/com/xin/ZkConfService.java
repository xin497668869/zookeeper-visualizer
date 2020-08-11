package com.xin;

import com.alibaba.fastjson.JSON;
import com.xin.controller.ConfSettingController;
import com.xin.view.FXMLDialog;
import com.xin.view.conf.ZkConfListView;
import javafx.scene.control.ListView;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class ZkConfService {
    public static final String CONF_FILE_NAME = "zkSetting.txt";
    public static ZkConfService zkConfService = new ZkConfService();
    private static List<ZkConf> zkConfs;

    public static ZkConfService getService() {
        return zkConfService;
    }

    public static void createSaveUi(ZkConf zkConf, ZkConfListView zkConfListView) {
        FXMLDialog<ConfSettingController> fxmlDialog = new FXMLDialog<>("/fxml/confSetting.fxml");
        fxmlDialog.init();
        ConfSettingController controller = fxmlDialog.getController();
        if (zkConf == null) {
            fxmlDialog.setTitle("创建连接");
            controller.init(zkConfListView);
        } else {
            controller.init(zkConf, zkConfListView);
            fxmlDialog.setTitle("修改连接");
        }
        fxmlDialog.show();

    }

    public int saveZkConf(ZkConf zkConf, ListView<ZkConf> confListView) {
        for (ZkConf zkConf1 : zkConfs) {
            if (zkConf1.id.equalsIgnoreCase(zkConf.getId())) {
                zkConf1.updateConf(zkConf);
                saveInFile();
                confListView.getItems()
                            .stream()
                            .filter(conf2 -> conf2.getId()
                                                  .equals(zkConf.getId()))
                            .forEach(c -> c.updateConf(zkConf));
                confListView.refresh();
                return 0;
            }
        }
        zkConfs.add(zkConf);
        saveInFile();
        confListView.getItems()
                    .add(zkConf);
        confListView.refresh();
        return 1;
    }

    public void removeZkConf(ZkConf zkConf, ListView<ZkConf> confListView) {
        zkConfs.remove(zkConf);
        saveInFile();
        confListView.getItems()
                    .removeIf(conf2 -> {
                        return conf2.getId()
                                    .equals(zkConf.getId());
                    });
        confListView.refresh();
    }

    public List<ZkConf> getZkConf() {
        try {
            File file = new File(CONF_FILE_NAME);
            if (file.exists()) {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                zkConfs = JSON.parseArray(content, ZkConf.class);
                for (ZkConf zkConf : zkConfs) {
                    if (zkConf.getConnectTimeout() == null) {
                        zkConf.setConnectTimeout(5000);
                    }
                    if (zkConf.getSessionTimeout() == null) {
                        zkConf.setSessionTimeout(5000);
                    }
                }
            } else {
                zkConfs = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return zkConfs;
    }

    private void saveInFile() {
        try {
            FileUtils.writeByteArrayToFile(new File(CONF_FILE_NAME), JSON.toJSONBytes(zkConfs), false);
        } catch (IOException e) {
            log.error("保存配置异常", e);
        }
    }

    @Data
    public static class ZkConf implements Serializable {
        private String id;
        private String name;
        private Integer connectTimeout;
        private Integer sessionTimeout;
        private String address;

        public ZkConf() {
        }

        public ZkConf copy() {
            return new ZkConf(id, name, address, connectTimeout, sessionTimeout);
        }

        public ZkConf(String id,
                      String name,
                      String address,
                      Integer connectTimeout,
                      Integer sessionTimeout) {
            if (id == null || id.isEmpty()) {
                this.id = UUID.randomUUID()
                              .toString()
                              .replace("-", "");
            } else {
                this.id = id;
            }
            this.name = name;
            this.address = address;
            this.connectTimeout = connectTimeout;
            this.sessionTimeout = sessionTimeout;
        }

        public void updateConf(ZkConf zkConf) {
            name = zkConf.name;
            address = zkConf.address;
            this.connectTimeout = zkConf.connectTimeout;
            this.sessionTimeout = zkConf.sessionTimeout;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            ZkConf zkConf = (ZkConf) o;
            return Objects.equals(id, zkConf.id);
        }

        @Override
        public String toString() {
            return name + "(" + address + ")";
        }
    }
}

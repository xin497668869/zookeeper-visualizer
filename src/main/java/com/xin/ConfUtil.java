package com.xin;


import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javafx.scene.control.ListView;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class ConfUtil {

    @Data
    @NoArgsConstructor
    public static class Conf {
        private String id;
        private String name;
        private String address;

        public void updateConf(Conf conf) {
            name = conf.name;
            address = conf.address;
        }

        @Override
        public String toString() {
            return name + "(" + address + ")";
        }

        public Conf(String id, String name, String address) {
            if (id == null || id.isEmpty()) {
                this.id = UUID.randomUUID().toString().replace("-", "");
            } else {
                this.id = id;
            }
            this.name = name;
            this.address = address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Conf conf = (Conf) o;
            return Objects.equals(id, conf.id);
        }

        @Override
        public int hashCode() {

            return Objects.hash(super.hashCode(), id);
        }
    }

    private static List<Conf> confs;
    private static JSONSerializer               serializer   = new JSONSerializer();
    private static JSONDeserializer<List<Conf>> deserializer = new JSONDeserializer<>();

    public static int addOrUpdateConf(Conf conf, ListView<Conf> confListView) {
        for (Conf conf1 : confs) {
            if (conf1.id.equalsIgnoreCase(conf.getId())) {
                conf1.updateConf(conf);
                save();
                confListView.getItems().stream().filter(conf2 -> conf2.getId().equals(conf.getId())).forEach(c -> c.updateConf(conf));
                confListView.refresh();
                return 0;
            }
        }
        confs.add(conf);
        save();
        confListView.getItems().add(conf);
        confListView.refresh();
        return 1;
    }

    public static void removeConf(Conf conf, ListView<Conf> confListView) {
        confs.remove(conf);
        save();
        confListView.getItems().removeIf(conf2 -> conf2.getId().equals(conf.getId()));
        confListView.refresh();
    }

    public static void save() {
        try (FileWriter fileWriter = new FileWriter("conf", false)) {
            serializer.deepSerialize(confs, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Conf> load() {
        try {
            String confFileName = "conf";
            if (new File(confFileName).exists()) {
                try (FileReader fileReader = new FileReader(confFileName)) {
                    confs = deserializer.deserialize(fileReader);
                }
            } else {
                confs = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return confs;
    }
}

package com.xin.controller;

import com.alibaba.fastjson.JSON;
import com.xin.ConfUtil;
import com.xin.service.ConfService;
import com.xin.view.AboutDialog;
import com.xin.view.AlertTemplate;
import com.xin.view.ProgressDialog;
import com.xin.view.tab.ZkTab;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.HyperlinkLabel;
import sun.misc.BASE64Decoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class RootController implements Initializable {

    public MenuItem importBtn;
    public MenuItem exportBtn;
    public MenuItem exitBtn;
    public MenuItem newConnBtn;
    public MenuItem expandAllNodeBtn;
    public MenuItem closeAllNodeBtn;
    public MenuItem aboutBtn;

    private AboutDialog aboutDialog = new AboutDialog();

    /**
     * 配置列表
     */
    public ListView<ConfUtil.Conf> confListView;
    public TextField filterTextField;
    public TabPane connectTabPane;
    public HyperlinkLabel welcomeInfo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            welcomeInfo.setText(IOUtils.toString(RootController.class.getClassLoader().getResource("welcomeInfo.txt").toURI(), StandardCharsets.UTF_8));
            welcomeInfo.setOnAction(event -> {
                Hyperlink link = (Hyperlink) event.getSource();
                final String str = link.getText();
                try {
                    java.awt.Desktop.getDesktop().browse(new URI(str));
                } catch (Exception e) {
                    log.error("打开github网页失败", e);
                }
            });

            ContextMenu cm = getContextMenu();

            confListView.setItems(FXCollections.observableArrayList(ConfUtil.load()));
            confListView.setOnMouseClicked(mouseEvent -> {
                System.out.println(" confListView.mouseEvent " + mouseEvent);

                if (mouseEvent.getEventType().getName().equalsIgnoreCase(MouseEvent.MOUSE_CLICKED.getName())) {
                    expandAllNodeBtn.setDisable(false);
                    closeAllNodeBtn.setDisable(false);
                }

                if (mouseEvent.getClickCount() == 2) {
                    ConfUtil.Conf selectedItem = confListView.getSelectionModel().getSelectedItem();

                    ProgressDialog progressDialog = new ProgressDialog(selectedItem.toString() + "  连接中...", selectedItem, new Consumer<ZkClient>() {
                        @Override
                        public void accept(ZkClient zkClient) {
                            connectTabPane.getScene().getStylesheets().add(getClass().getResource("/style.css")
                                    .toExternalForm());
                            ZkTab tab = new ZkTab(selectedItem);
                            tab.init(zkClient);
                            tab.setClosable(true);
                            connectTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
                            connectTabPane.getTabs().add(tab);
                            connectTabPane.getSelectionModel().select(tab);
                        }
                    });
                    progressDialog.showAndWait();
                }
            });


            confListView.setCellFactory(new Callback<ListView<ConfUtil.Conf>, ListCell<ConfUtil.Conf>>() {
                @Override
                public ListCell<ConfUtil.Conf> call(ListView<ConfUtil.Conf> param) {

                    EventHandler<MouseEvent> mouseEventEventHandler = event -> {
                        confListView.getSelectionModel().clearSelection();
                        event.consume();
                    };

                    return new ListCell<ConfUtil.Conf>() {
                        @Override
                        protected void updateItem(ConfUtil.Conf item, boolean empty) {
                            super.updateItem(item, empty);
                            if (!empty && item != null) {
                                setContextMenu(cm);
                                setText(item.toString());
                                removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
                            } else {
                                setText("");
                                addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
                            }

                        }
                    };
                }
            });

            filterTextField.textProperty().addListener(observable -> {
                ObservableList<ConfUtil.Conf> confs = FXCollections.observableArrayList(ConfUtil.load());
                Predicate filter = o -> o.toString().contains(filterTextField.getCharacters());
                confListView.setItems(new FilteredList<ConfUtil.Conf>(confs, filter));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        Label menuLabel = new Label("关于");
        menuLabel.setOnMouseClicked(event -> {
            aboutBtnAction();
        });
        aboutBtn.setGraphic(menuLabel);
    }

    private ContextMenu getContextMenu() {
        MenuItem menuItem1 = new MenuItem("修改");
        menuItem1.setOnAction(event -> {
            ConfUtil.Conf selectedItem = confListView.getSelectionModel().getSelectedItem();
            ConfService.getService().saveConf(selectedItem, confListView);
        });
        MenuItem menuItem2 = new MenuItem("删除");
        menuItem2.setOnAction(event -> {
            ConfUtil.Conf selectedItem = confListView.getSelectionModel().getSelectedItem();
            ConfUtil.removeConf(selectedItem, confListView);
        });
        ContextMenu cm = new ContextMenu();
        cm.getItems().add(menuItem1);
        cm.getItems().add(menuItem2);
        return cm;
    }

    public void importBtnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
//        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            FileReader fr = new FileReader(file);
            new BufferedReader(fr).lines().filter(l -> l != null && !l.isEmpty()).forEachOrdered(sb::append);
            String str = new String(new BASE64Decoder().decodeBuffer(sb.toString()));
            List<ConfUtil.Conf> list = JSON.parseArray(str, ConfUtil.Conf.class);
            confListView.setItems(FXCollections.observableArrayList(list));
            ConfUtil.reloadList(list);
        } catch (Exception e) {
            log.warn("导入配置异常：", e);
        }
        System.out.println(file);
    }

    public void exportBtnAction() {
        ConfService.getService().startExportToFile("Save Resource File", "connections.conf",
                ConfUtil.load(), true, (res) -> {
                    AlertTemplate.showTipAlert(res, "导出成功！", "导出失败！");
                    return null;
                });
    }

    public void exitBtnAction() {
        Platform.exit();
    }

    public void newConnBtnAction() {
        ConfService.getService().saveConf(null, confListView);
    }

    public void expandAllNodeBtnAction() {
        System.out.println("expandAllNodeBtnAction...");
    }

    public void closeAllNodeBtnAction() {
        System.out.println("closeAllNodeBtnAction...");
    }

    public void aboutBtnAction() {
        if (aboutDialog.isShowing()) {
            return;
        }
        aboutDialog.show();
    }


}

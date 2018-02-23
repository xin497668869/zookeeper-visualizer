package com.xin.controller;

import com.xin.ConfUtil;
import com.xin.view.ProgressDialog;
import com.xin.view.tab.ZkTab;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.I0Itec.zkclient.ZkClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class RootController implements Initializable {

    /**
     * 配置列表
     */
    public ListView<ConfUtil.Conf> confListView;
    public TextField               filterTextField;
    public TabPane                 connectTabPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            confListView.setItems(FXCollections.observableArrayList(ConfUtil.load()));
            ContextMenu cm = getContextMenu();
            confListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) {
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

    }

    private ContextMenu getContextMenu() {
        MenuItem menuItem1 = new MenuItem("修改");
        menuItem1.setOnAction(event -> {
            ConfUtil.Conf selectedItem = confListView.getSelectionModel().getSelectedItem();
            saveConf(selectedItem);
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

    public void onClickToCreateConf(MouseEvent mouseEvent) {

        saveConf(null);

    }

    private void saveConf(ConfUtil.Conf selectedItem) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/confSetting.fxml"));
            Stage stage = new Stage();
            if (selectedItem != null) {
                TextField nameTextField = (TextField) root.lookup("#nameTextField");
                nameTextField.setText(selectedItem.getName());
                TextField addressTextField = (TextField) root.lookup("#addressTextField");
                addressTextField.setText(selectedItem.getAddress());
                TextField idTextField = (TextField) root.lookup("#idTextField");
                idTextField.setText(selectedItem.getId());
                stage.setTitle("zk配置修改");
            } else{
                stage.setTitle("zk配置新增");
            }
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene value = new Scene(root);
            stage.setScene(value);
            stage.showAndWait();
            ConfUtil.Conf conf = (ConfUtil.Conf) value.getUserData();
            if (conf != null) {
                ConfUtil.addOrUpdateConf(conf, confListView);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

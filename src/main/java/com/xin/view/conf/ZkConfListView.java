package com.xin.view.conf;

import com.xin.ZkClientWrap;
import com.xin.ZkConfService;
import com.xin.ZkConfService.ZkConf;
import com.xin.view.tab.ZkTab;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.I0Itec.zkclient.MyZkClient;

import java.util.function.Consumer;

import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * test
 *
 * @author 497668869@qq.com
 * @since 1.0
 */
public class ZkConfListView extends ListView<ZkConf> {

    private Runnable connectTrigger;

    public ZkConfListView() {

        setCellFactory(new Callback<ListView<ZkConf>, ListCell<ZkConf>>() {
            @Override
            public ListCell<ZkConf> call(ListView<ZkConf> param) {

                EventHandler<MouseEvent> mouseEventEventHandler = event -> {
                    getSelectionModel()
                            .clearSelection();
                    event.consume();
                };

                return new ListCell<ZkConf>() {
                    @Override
                    protected void updateItem(ZkConf item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            setContextMenu(getRightContextMenu());
                            setText(item.toString());
                            removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventEventHandler);
                        } else {
                            setText("");
                            setContextMenu(getCreateContextMenu());
                        }
                    }
                };
            }
        });
    }

    public void installConnectTrigger(TabPane connectTabPane) {
        this.connectTrigger = () -> {
            final ZkConf selectedItem = getSelectionModel().getSelectedItem();
            ProgressDialog progressDialog = new ProgressDialog(selectedItem + "  连接中...", selectedItem,
                                                               new Consumer<MyZkClient>() {
                                                                   @Override
                                                                   public void accept(MyZkClient zkClient) {
                                                                       ZkTab tab = new ZkTab(selectedItem);
                                                                       connectTabPane.getScene()
                                                                                     .getStylesheets()
                                                                                     .add(getClass().getResource("/css/style.css")
                                                                                                    .toExternalForm());
                                                                       tab.init(new ZkClientWrap(zkClient, selectedItem.copy()));
                                                                       tab.setClosable(true);
                                                                       connectTabPane.setTabClosingPolicy(
                                                                               TabPane.TabClosingPolicy.SELECTED_TAB);
                                                                       connectTabPane.getTabs()
                                                                                     .add(tab);
                                                                       connectTabPane.getSelectionModel()
                                                                                     .select(tab);
                                                                   }
                                                               });
            progressDialog.showAndWait();
        };

        setOnMouseClicked(mouseEvent -> {
            if (!PRIMARY.equals(mouseEvent.getButton()) || mouseEvent.getClickCount() != 2) {
                return;
            }
            if (getSelectionModel().getSelectedItem() == null) {
                return;
            }
            connectTrigger.run();
        });
    }

    private ContextMenu getCreateContextMenu() {
        MenuItem menuItem1 = new MenuItem("新增连接");
        menuItem1.setOnAction(event -> {
            ZkConfService.createSaveUi(null, this);
        });

        ContextMenu cm = new ContextMenu();
        cm.getItems()
          .add(menuItem1);
        return cm;
    }

    private ContextMenu getRightContextMenu() {
        MenuItem menuItem1 = new MenuItem("修改");
        menuItem1.setOnAction(event -> {
            ZkConf selectedItem = getSelectionModel()
                    .getSelectedItem();
            ZkConfService.createSaveUi(selectedItem, this);
        });

        MenuItem menuItem2 = new MenuItem("删除");
        menuItem2.setOnAction(event -> {
            ZkConf selectedItem = getSelectionModel()
                    .getSelectedItem();
            ZkConfService.getService()
                         .removeZkConf(selectedItem, this);
        });
        ContextMenu cm = new ContextMenu();
        cm.getItems()
          .add(menuItem1);
        cm.getItems()
          .add(menuItem2);
        return cm;
    }
}

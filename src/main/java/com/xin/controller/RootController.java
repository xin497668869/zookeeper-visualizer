package com.xin.controller;

import com.xin.ZkConfService;
import com.xin.util.FuzzyMatchUtils;
import com.xin.util.StringUtil;
import com.xin.view.conf.SearchFilterObservalbeList;
import com.xin.view.conf.ZkConfListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.HyperlinkLabel;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Slf4j
public class RootController implements Initializable {

    public MenuItem exitBtn;
    public MenuItem newConnBtn;
    /**
     * 配置列表
     */
    public ZkConfListView zkConfListView;
    public TextField filterTextField;
    public TabPane connectTabPane;
    public HyperlinkLabel welcomeInfo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            welcomeInfo.setText(IOUtils.toString(RootController.class.getClassLoader()
                                                                     .getResource("welcomeInfo.txt")
                                                                     .toURI(), StandardCharsets.UTF_8));
            welcomeInfo.setOnAction(event -> {
                Hyperlink link = (Hyperlink) event.getSource();
                final String str = link.getText();
                try {
                    Desktop.getDesktop()
                           .browse(new URI(str));
                } catch (Exception e) {
                    log.error("打开github网页失败", e);
                }
            });

            zkConfListView.installConnectTrigger(connectTabPane);

            installConfSearchFilter();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void exitBtnAction() {
        Platform.exit();
    }

    public void newConnBtnAction() {
        ZkConfService.createSaveUi(null, zkConfListView);
    }

    private void installConfSearchFilter() {
        zkConfListView.setItems(new SearchFilterObservalbeList<>(FXCollections.observableArrayList(ZkConfService.getService()
                                                                                                                .getZkConf()), zkConf -> {
            if (!StringUtil.isEmpty(filterTextField.getText())) {
                return FuzzyMatchUtils.match(zkConf.toString(),
                                             filterTextField.getText());
            } else {
                return true;
            }
        }));
        filterTextField.textProperty()
                       .addListener(observable -> {
                           ((SearchFilterObservalbeList) zkConfListView.getItems()).refilter();
                       });
    }

}

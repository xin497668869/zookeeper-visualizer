/**
 * Copyright (c) 2014, 2015 ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.xin.view.conf;

import com.xin.DialogUtils;
import com.xin.ZkConfService.ZkConf;
import com.xin.view.AlertTemplate;
import com.xin.view.ZkConnectionTask;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import lombok.Getter;
import org.I0Itec.zkclient.ZkClient;

import java.util.function.Consumer;

import static com.xin.DialogUtils.forcefullyHideDialog;

public class ProgressDialog extends Dialog<Void> {

    @Getter
    private final ZkConf zkConf;
    @Getter
    private Task<ZkClient> worker;

    public ProgressDialog(String text, ZkConf zkConf, Consumer<ZkClient> successRun) {
        this.zkConf = zkConf;
        final DialogPane dialogPane = getDialogPane();

        final Label space = new Label("");
        final Label progressMessag2e = new Label(text);
        space.setPadding(new Insets(0, 0, 1, 0));

        final WorkerProgressPane content = new WorkerProgressPane(this, successRun);
        worker = new ZkConnectionTask(zkConf);
        Thread th = new Thread(worker);
        th.start();

        content.setWorker(worker);

        VBox vbox = new VBox(10, progressMessag2e, space, content);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setPrefSize(300, 80);
        /**
         * The content Text cannot be set before the constructor and since we
         * set a Content Node, the contentText will not be shown. If we want to
         * let the user display a content text, we must recreate it.
         */
        Button close = new Button("close");
        close.setPrefWidth(150);
        close.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                forcefullyHideDialog(ProgressDialog.this);
                worker.cancel();
            }
        });
        vbox.setAlignment(Pos.BASELINE_CENTER);
        vbox.getChildren().add(close);
        dialogPane.setContent(vbox);
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                forcefullyHideDialog(ProgressDialog.this);
                worker.cancel();
            }
        });
    }


    /**************************************************************************
     *
     * Support classes
     *
     **************************************************************************/

    /**
     * The WorkerProgressPane takes a {@link Dialog} and a {@link Worker}
     * and links them together so the dialog is shown or hidden depending
     * on the state of the worker.  The WorkerProgressPane also includes
     * a progress bar that is automatically bound to the progress property
     * of the worker.  The way in which the WorkerProgressPane shows and
     * hides its worker's dialog is consistent with the expected behavior
     * for {@link #showWorkerProgress(Worker)}.
     */
    private static class WorkerProgressPane extends Region {
        private final Consumer<ZkClient> successRun;
        private       Worker<?>          worker;


        private ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State old, Worker.State value) {
                ZkConnectionTask zkConnectionTask = (ZkConnectionTask) ((SimpleObjectProperty) observable).getBean();

                switch (value) {
                    case CANCELLED:
                        end();
                        return;
                    case FAILED:
                        end();
                        AlertTemplate.showTipAlert(false, "", "连接失败！\n " + zkConnectionTask.getException().getMessage());
                        return;
                    case SUCCEEDED:
                        end();
                        successRun.accept(zkConnectionTask.getZkClient());
                        break;
                    case SCHEDULED:
                        break;
                    default: //no-op
                }
            }
        };

        public final void setWorker(final Worker<?> newWorker) {
            if (newWorker != worker) {
                if (worker != null) {
                    worker.stateProperty().removeListener(stateListener);
                    end();
                }

                worker = newWorker;

                if (newWorker != null) {
                    newWorker.stateProperty().addListener(stateListener);
                    if (newWorker.getState() == Worker.State.RUNNING || newWorker.getState() == Worker.State.SCHEDULED) {
                        // It is already running
                    }
                }
            }
        }

        // If the progress indicator changes, then we need to re-initialize
        // If the worker changes, we need to re-initialize

        private final ProgressDialog dialog;
        private final ProgressBar    progressBar;

        public WorkerProgressPane(ProgressDialog dialog, Consumer<ZkClient> successRun) {
            this.successRun = successRun;
            this.dialog = dialog;

            this.progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(progressBar);

            if (worker != null) {
                progressBar.progressProperty().bind(worker.progressProperty());
            }
        }


        private void end() {
            progressBar.progressProperty().unbind();
            DialogUtils.forcefullyHideDialog(dialog);
        }

        @Override
        protected void layoutChildren() {
            if (progressBar != null) {
                Insets insets = getInsets();
                double w = getWidth() - insets.getLeft() - insets.getRight();
                double h = getHeight() - insets.getTop() - insets.getBottom();

                double prefH = progressBar.prefHeight(-1);
                double x = insets.getLeft() + (w - w) / 2.0;
                double y = insets.getTop() + (h - prefH) / 2.0;

                progressBar.resizeRelocate(x, y, w, prefH);
            }
        }
    }
}

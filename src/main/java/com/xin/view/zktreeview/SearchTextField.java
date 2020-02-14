package com.xin.view.zktreeview;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class SearchTextField extends CustomTextField {

    ValidationSupport validationSupport = new ValidationSupport();

    {
        validationSupport.setValidationDecorator(new StyleClassValidationDecoration());
    }

    public SearchTextField() {
        setupClearButtonField(this, this.rightProperty());
        setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                setText("");
            }
        });
    }

    public void validate(Validator<String> stringValidator) {
        validationSupport.registerValidator(this, stringValidator);
    }

    private static void setupClearButtonField(final TextField inputField, ObjectProperty<Node> rightProperty) {
        inputField.getStyleClass().add("clearable-field");
        Region clearButton = new Region();
        clearButton.getStyleClass().addAll(new String[]{"graphic"});
        StackPane clearButtonPane = new StackPane(new Node[]{clearButton});
        clearButtonPane.getStyleClass().addAll(new String[]{"clear-button"});
        clearButtonPane.setOpacity(0.0D);
        clearButtonPane.setCursor(Cursor.DEFAULT);
        clearButtonPane.setOnMouseReleased((e) -> {
            inputField.clear();
        });
        clearButtonPane.managedProperty().bind(inputField.editableProperty());
        clearButtonPane.visibleProperty().bind(inputField.editableProperty());
        rightProperty.set(clearButtonPane);
        final FadeTransition fader = new FadeTransition(Duration.millis(200), clearButtonPane);
        fader.setCycleCount(1);
        inputField.textProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable arg0) {
                String text = inputField.getText();
                boolean isTextEmpty = text == null || text.isEmpty();
                boolean isButtonVisible = fader.getNode().getOpacity() > 0.0D;
                if (isTextEmpty && isButtonVisible) {
                    this.setButtonVisible(false);
                } else if (!isTextEmpty && !isButtonVisible) {
                    this.setButtonVisible(true);
                }

            }

            private void setButtonVisible(boolean visible) {
                fader.setFromValue(visible ? 0.0D : 1.0D);
                fader.setToValue(visible ? 1.0D : 0.0D);
                fader.play();
            }
        });
    }
}

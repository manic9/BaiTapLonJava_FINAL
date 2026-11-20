package com.esports.manager.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ToastManager {

    private static VBox toastContainer;

    private ToastManager() {}

    public static void init(VBox container) {
        toastContainer = container;
    }

    public static void showSuccess(String message) {
        show(message, "toast-success", "fas-check-circle", "#10B981");
    }

    public static void showError(String message) {
        show(message, "toast-error", "fas-exclamation-circle", "#EF4444");
    }

    public static void showInfo(String message) {
        show(message, "toast-info", "fas-info-circle", "#00D4FF");
    }

    private static void show(String message, String styleClass, String iconLiteral, String iconHex) {
        if (toastContainer == null) return;

        HBox toast = new HBox(10);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast-box", styleClass);
        toast.setMaxWidth(380);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(16);
        icon.setStyle("-fx-icon-color: " + iconHex + ";");

        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: 600;");

        toast.getChildren().addAll(icon, lbl);
        toast.setOpacity(0);
        toast.setTranslateY(15);

        toastContainer.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(220), toast);
        slideUp.setFromY(15);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        });
        delay.play();
    }
}
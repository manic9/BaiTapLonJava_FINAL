package com.esports.manager.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public final class ViewNavigator {

    private static StackPane viewport;
    private static Label titleLabel;
    private static Label subtitleLabel;

    private ViewNavigator() {}

    public static void init(StackPane contentViewport, Label lblTitle, Label lblSubtitle) {
        viewport = contentViewport;
        titleLabel = lblTitle;
        subtitleLabel = lblSubtitle;
    }

    public static void navigate(String fxmlFileName, String title, String subtitle) {
        navigate(fxmlFileName, title, subtitle, null);
    }

    public static <T> void navigate(String fxmlFileName, String title, String subtitle, Consumer<T> controllerInit) {
        try {
            URL resource = ViewNavigator.class.getResource("/com/esports/manager/fxml/" + fxmlFileName);
            if (resource == null) {
                ToastManager.showError("Không tìm thấy giao diện: " + fxmlFileName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();

            if (controllerInit != null) {
                controllerInit.accept(loader.getController());
            }

            if (titleLabel != null) titleLabel.setText(title);
            if (subtitleLabel != null) subtitleLabel.setText(subtitle);

            view.setOpacity(0);
            viewport.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(180), view);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        } catch (IOException e) {
            e.printStackTrace();
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            ToastManager.showError("Lỗi tải giao diện: " + cause.getMessage());
        }
    }
}
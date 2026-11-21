package com.esports.manager;

import com.esports.manager.config.AppContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class EsportsManagerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Khởi tạo Repository, Service & nạp dữ liệu mẫu vào bộ nhớ
        AppContext.getInstance().initialize();

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/com/esports/manager/fxml/MainWindow.fxml"))
        );
        Scene scene = new Scene(loader.load(), 1380, 820);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/esports/manager/css/dark-theme.css")).toExternalForm()
        );

        primaryStage.setTitle("Nexus Arena — Esports Tournament Manager");
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
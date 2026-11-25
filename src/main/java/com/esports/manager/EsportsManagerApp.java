package com.esports.manager;

import com.esports.manager.config.AppContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.esports.manager.controller.LoginController;

import java.util.Objects;

public class EsportsManagerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppContext appContext = AppContext.getInstance();
        try {
            appContext.initialize();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Không thể khởi động Nexus Arena");
            alert.setHeaderText("Ứng dụng yêu cầu SQL Server để lưu dữ liệu");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        primaryStage.setTitle("QL TOURNAMENT| Đăng nhập");
        primaryStage.setMinWidth(390);
        primaryStage.setMinHeight(550);
        showLogin(primaryStage);
        primaryStage.show();
    }

    public static void showLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    EsportsManagerApp.class.getResource("/com/esports/manager/fxml/LoginView.fxml")));
            Scene scene = new Scene(loader.load(), 430, 600);
            applyTheme(scene);
            LoginController controller = loader.getController();
            controller.setOnAuthenticated(() -> showAdmin(stage));
            stage.setTitle("QL TOURNAMENT | Đăng nhập");
            stage.setMinWidth(390);
            stage.setMinHeight(550);
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setWidth(430);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể mở màn hình đăng nhập", e);
        }
    }

    public static void showAdmin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    EsportsManagerApp.class.getResource("/com/esports/manager/fxml/MainWindow.fxml")));
            Scene scene = new Scene(loader.load(), 1380, 820);
            applyTheme(scene);
            stage.setTitle("QL TOURNAMENT | Quản trị giải đấu");
            stage.setMinWidth(1180);
            stage.setMinHeight(720);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể mở trang quản trị", e);
        }
    }

    private static void applyTheme(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(
                EsportsManagerApp.class.getResource("/com/esports/manager/css/dark-theme.css")).toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

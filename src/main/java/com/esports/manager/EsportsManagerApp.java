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
        // Khởi tạo Repository, Service & nạp dữ liệu mẫu vào bộ nhớ
        AppContext.getInstance().initialize();

        primaryStage.setTitle("Nexus Arena | Đăng nhập");
        primaryStage.setMinWidth(390);
        primaryStage.setMinHeight(550);
        showLogin(primaryStage);
        primaryStage.show();
        AppContext appContext = AppContext.getInstance();
        if (!appContext.getInitialDbStatus().connected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Không kết nối được SQL Server");
            alert.setHeaderText("Ứng dụng đang chạy ở chế độ dữ liệu tạm thời");
            alert.setContentText(appContext.getInitialDbStatus().message()
                    + "\n\nCác thay đổi trong phiên này sẽ không được lưu vào SQL Server.");
            alert.showAndWait();
        }
    }

    public static void showLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    EsportsManagerApp.class.getResource("/com/esports/manager/fxml/LoginView.fxml")));
            Scene scene = new Scene(loader.load(), 430, 600);
            applyTheme(scene);
            LoginController controller = loader.getController();
            controller.setOnAuthenticated(() -> showAdmin(stage));
            stage.setTitle("Nexus Arena | Đăng nhập");
            stage.setMinWidth(390);
            stage.setMinHeight(550);
            stage.setScene(scene);
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
            stage.setTitle("Nexus Arena | Quản trị giải đấu");
            stage.setMinWidth(1180);
            stage.setMinHeight(720);
            stage.setScene(scene);
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

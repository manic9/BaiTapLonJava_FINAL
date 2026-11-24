package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.util.I18n;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Label lblLanguage;
    @FXML private Label lblTheme;
    @FXML private Label lblAccessStatus;
    @FXML private CheckBox chkRemember;
    @FXML private ComboBox<I18n.Language> cbLanguage;
    @FXML private ComboBox<String> cbTheme;
    @FXML private StackPane rootPane;
    @FXML private Canvas backgroundCanvas;
    @FXML private Button btnLogin;
    @FXML private Label libHelp;
    @FXML private Label libTournament;
    @FXML private Label libTermsofService;

    private Runnable onAuthenticated;
    private final Preferences preferences = Preferences.userNodeForPackage(LoginController.class);

    public void setOnAuthenticated(Runnable callback) {
        onAuthenticated = callback;
    }

    @FXML
    private void initialize() {
        lblError.setManaged(false);
        lblError.setVisible(false);
        cbLanguage.setItems(FXCollections.observableArrayList(I18n.Language.values()));
        I18n.Language initialLanguage = I18n.Language.fromCode(AppContext.getInstance().getSettings().getLanguage());
        cbLanguage.setValue(initialLanguage);
        I18n.setLanguage(initialLanguage);
        cbTheme.setItems(FXCollections.observableArrayList("QL TOURNAMENT Dark"));
        cbTheme.setValue("Ql Tournament Dark");
        String savedUsername = preferences.get("login.username", "");
        if (!savedUsername.isBlank()) {
            txtUsername.setText(savedUsername);
            chkRemember.setSelected(true);
            txtPassword.requestFocus();
        }
        cbLanguage.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                I18n.setLanguage(newValue);
                AppContext.getInstance().getSettings().setLanguage(newValue.getCode());
                updateCopy();
            }
        });
        backgroundCanvas.widthProperty().bind(rootPane.widthProperty());
        backgroundCanvas.heightProperty().bind(rootPane.heightProperty());
        backgroundCanvas.widthProperty().addListener((obs, oldValue, newValue) -> drawTexture());
        backgroundCanvas.heightProperty().addListener((obs, oldValue, newValue) -> drawTexture());
        drawTexture();
        updateCopy();
    }

    private void updateCopy() {
        boolean english = I18n.getCurrentLanguage() == I18n.Language.ENGLISH;
        txtUsername.setPromptText(english ? "Username" : "Tên đăng nhập");
        txtPassword.setPromptText(english ? "Password" : "Mật khẩu");
        lblLanguage.setText(english ? "Language" : "Ngôn ngữ");
        lblTheme.setText(english ? "Theme" : "Giao diện");
        chkRemember.setText(english ? "Remember" : "Ghi nhớ");
        lblAccessStatus.setText(english ? "Secure administrator access" : "Đăng nhập quản trị bảo mật");
        btnLogin.setText(english ? "Login" : "Đăng nhập");
        libHelp.setText(english ? "Help" : "Hỗ Trợ");
        libTournament.setText(english ? "Tournament" : "Giải đấu");
        libTermsofService.setText(english ? "Terms Of Service" : "Điều Khoản");
    }

    private void drawTexture() {
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();
        if (width <= 0 || height <= 0) return;
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(24, 22, 34));
        gc.fillRect(0, 0, width, height);
        gc.setStroke(Color.rgb(53, 47, 74, 0.62));
        gc.setLineWidth(1);
        for (double x = -height; x < width; x += 5) {
            gc.strokeLine(x, height, x + height, 0);
        }
        gc.setFill(Color.web("#8B70FF"));
        gc.fillOval(width * 0.77, height * 0.42, 5, 5);
        gc.fillOval(width * 0.25, height * 0.34, 3, 3);
        gc.setFill(Color.web("#00D4FF"));
        gc.fillOval(width * 0.14, height * 0.41, 2, 2);
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        char[] password = txtPassword.getText() == null ? new char[0] : txtPassword.getText().toCharArray();
        try {
            if (AppContext.getInstance().getAuthenticationService().authenticate(username, password)) {
                if (chkRemember.isSelected()) preferences.put("login.username", username);
                else preferences.remove("login.username");
                if (onAuthenticated != null) onAuthenticated.run();
                return;
            }
            showError(I18n.getCurrentLanguage() == I18n.Language.ENGLISH
                    ? "Invalid username or password."
                    : "Tên đăng nhập hoặc mật khẩu không chính xác.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
            txtPassword.clear();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setManaged(true);
        lblError.setVisible(true);
        txtPassword.requestFocus();
    }
}

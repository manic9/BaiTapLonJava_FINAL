package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.I18n;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

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
    @FXML private Hyperlink linkRegister;
    @FXML private Label lblNoAccount;
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
        lblNoAccount.setText(english ? "Don't have an account?" : "Chưa có tài khoản?");
        linkRegister.setText(english ? "Register" : "Đăng ký");
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
            var authenticatedUser = AppContext.getInstance().getAuthenticationService()
                    .authenticateUser(username, password);
            if (authenticatedUser.isPresent()) {
                AppContext.getInstance().setCurrentUser(authenticatedUser.get());
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

    @FXML
    private void handleRegister() {
        boolean english = I18n.getCurrentLanguage() == I18n.Language.ENGLISH;
        Dialog<RegistrationInput> dialog = new Dialog<>();
        dialog.setTitle(english ? "Create account" : "Đăng ký tài khoản");
        dialog.setHeaderText(english ? "Create an organizer account" : "Tạo tài khoản ban tổ chức");
        DialogHelper.applyDarkTheme(dialog.getDialogPane());
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType(english ? "Register" : "Đăng ký", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(450);

        TextField fullName = new TextField();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        PasswordField confirmPassword = new PasswordField();
        Label registrationError = new Label();
        registrationError.setWrapText(true);
        registrationError.setMaxWidth(400);
        registrationError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px;");
        fullName.setPromptText(english ? "Full name" : "Họ và tên");
        username.setPromptText(english ? "Username (3-50 characters)" : "Tên đăng nhập (3-50 ký tự)");
        password.setPromptText(english ? "At least 8 characters" : "Ít nhất 8 ký tự");
        confirmPassword.setPromptText(english ? "Confirm password" : "Nhập lại mật khẩu");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(14));
        form.add(new Label(english ? "Full name" : "Họ và tên"), 0, 0);
        form.add(fullName, 1, 0);
        form.add(new Label(english ? "Username" : "Tên đăng nhập"), 0, 1);
        form.add(username, 1, 1);
        form.add(new Label(english ? "Password" : "Mật khẩu"), 0, 2);
        form.add(password, 1, 2);
        form.add(new Label(english ? "Confirm" : "Xác nhận"), 0, 3);
        form.add(confirmPassword, 1, 3);
        form.add(registrationError, 0, 4, 2, 1);
        dialog.getDialogPane().setContent(form);

        ButtonType registerButtonType = dialog.getDialogPane().getButtonTypes().get(0);
        Button registerButton = (Button) dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.addEventFilter(ActionEvent.ACTION, event -> {
            String normalizedUsername = username.getText() == null ? "" : username.getText().trim();
            String validationMessage = null;
            if (fullName.getText() == null || fullName.getText().isBlank()) {
                validationMessage = english ? "Enter your full name." : "Vui lòng nhập họ và tên.";
            } else if (fullName.getText().trim().length() > 100) {
                validationMessage = english ? "Full name must be at most 100 characters." : "Họ tên không được vượt quá 100 ký tự.";
            } else if (!normalizedUsername.matches("[A-Za-z0-9._-]{3,50}")) {
                validationMessage = english
                        ? "Username must be 3-50 characters and use only letters, digits, dot, underscore, or hyphen."
                        : "Tên đăng nhập phải dài 3-50 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang.";
            } else if (password.getText().length() < 8) {
                validationMessage = english ? "Password must contain at least 8 characters." : "Mật khẩu cần có ít nhất 8 ký tự.";
            } else if (password.getText().length() > 128) {
                validationMessage = english ? "Password must not exceed 128 characters." : "Mật khẩu không được vượt quá 128 ký tự.";
            } else if (!password.getText().equals(confirmPassword.getText())) {
                validationMessage = english ? "The passwords do not match." : "Hai mật khẩu chưa khớp nhau.";
            }

            if (validationMessage != null) {
                registrationError.setText(validationMessage);
                event.consume();
            } else {
                registrationError.setText("");
            }
        });

        dialog.setResultConverter(button -> button == registerButtonType
                ? new RegistrationInput(fullName.getText().trim(), username.getText().trim(), password.getText().toCharArray())
                : null);

        dialog.showAndWait().ifPresent(input -> {
            try {
                AppContext.getInstance().getAuthenticationService()
                        .register(input.username(), input.fullName(), input.password());
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                DialogHelper.applyDarkTheme(success.getDialogPane());
                success.setTitle(english ? "Account created" : "Đăng ký thành công");
                success.setHeaderText(null);
                success.setContentText(english
                        ? "Your organizer account is ready. You can sign in now."
                        : "Tài khoản ban tổ chức đã được tạo. Bạn có thể đăng nhập ngay.");
                success.showAndWait();
                txtUsername.setText(input.username());
                txtPassword.requestFocus();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            } finally {
                java.util.Arrays.fill(input.password(), '\0');
            }
        });
    }

    private record RegistrationInput(String fullName, String username, char[] password) {}

    private void showError(String message) {
        lblError.setText(message);
        lblError.setManaged(true);
        lblError.setVisible(true);
        txtPassword.requestFocus();
    }
}

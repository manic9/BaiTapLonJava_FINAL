package com.esports.manager.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Objects;
import java.util.Optional;

public final class DialogHelper {

    private static final String CSS_PATH = "/com/esports/manager/css/dark-theme.css";

    private DialogHelper() {}

    public static boolean confirmDelete(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Xác nhận thao tác xóa");
        alert.setContentText(content);

        ButtonType btnDelete = new ButtonType("Xóa dữ liệu", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnDelete, btnCancel);

        applyDarkTheme(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnDelete;
    }

    public static void applyDarkTheme(DialogPane pane) {
        String css = Objects.requireNonNull(DialogHelper.class.getResource(CSS_PATH)).toExternalForm();
        if (!pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }
    }
}
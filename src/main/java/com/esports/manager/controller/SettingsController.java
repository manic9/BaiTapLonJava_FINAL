package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.AppSettings;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.Optional;

public class SettingsController {

    @FXML private TextField txtOrgName;
    @FXML private ComboBox<String> cbCurrency;
    @FXML private ComboBox<Integer> cbDefaultBo;
    @FXML private CheckBox chkAutoAdvance;
    @FXML private CheckBox chkConfirmDelete;

    private final AppContext ctx = AppContext.getInstance();

    @FXML
    public void initialize() {
        cbCurrency.setItems(FXCollections.observableArrayList("$", "₫", "€", "¥"));
        cbDefaultBo.setItems(FXCollections.observableArrayList(1, 3, 5));

        loadCurrentSettings();
    }

    private void loadCurrentSettings() {
        AppSettings settings = ctx.getSettings();
        txtOrgName.setText(settings.getOrganizationName());
        cbCurrency.setValue(settings.getCurrencySymbol());
        cbDefaultBo.setValue(settings.getDefaultBestOf());
        chkAutoAdvance.setSelected(settings.isAutoAdvanceBracket());
        chkConfirmDelete.setSelected(settings.isConfirmBeforeDelete());
    }

    @FXML
    public void handleSaveSettings() {
        String orgName = txtOrgName.getText().trim();
        if (orgName.isEmpty()) {
            ToastManager.showError("Tên đơn vị tổ chức không được để trống!");
            return;
        }

        AppSettings settings = ctx.getSettings();
        settings.setOrganizationName(orgName);
        settings.setCurrencySymbol(cbCurrency.getValue() != null ? cbCurrency.getValue() : "$");
        settings.setDefaultBestOf(cbDefaultBo.getValue() != null ? cbDefaultBo.getValue() : 3);
        settings.setAutoAdvanceBracket(chkAutoAdvance.isSelected());
        settings.setConfirmBeforeDelete(chkConfirmDelete.isSelected());

        ToastManager.showSuccess("Đã lưu cấu hình hệ thống thành công!");
    }

    @FXML
    public void handleResetMockData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Khôi phục dữ liệu mẫu");
        alert.setHeaderText("Đặt lại toàn bộ dữ liệu trong bộ nhớ?");
        alert.setContentText("Mọi thay đổi bạn đã thêm/sửa/xóa trong phiên làm việc này sẽ bị xóa và thay thế bằng bộ dữ liệu mẫu gốc.");

        ButtonType btnReset = new ButtonType("Khôi phục ngay", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnReset, btnCancel);
        DialogHelper.applyDarkTheme(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnReset) {
            ctx.resetMockData();
            ToastManager.showSuccess("Đã khôi phục toàn bộ dữ liệu mẫu về trạng thái mặc định!");
        }
    }
}
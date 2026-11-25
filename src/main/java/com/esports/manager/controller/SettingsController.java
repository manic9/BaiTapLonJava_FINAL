package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.AppSettings;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.I18n;
import com.esports.manager.util.ToastManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.Optional;

public class SettingsController {

    @FXML private TextField txtOrgName;
    @FXML private StackPane langTogglePane;
    @FXML private ImageView ivLangFlag;
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
        updateLangButton();
    }

    private void updateLangButton() {
        I18n.Language current = I18n.getCurrentLanguage();
        String flagPath;
        if (current == I18n.Language.ENGLISH) {
            flagPath = "/com/esports/manager/images/vietnam.png";
        } else {
            flagPath = "/com/esports/manager/images/united-kingdom.png";
        }
        InputStream is = getClass().getResourceAsStream(flagPath);
        if (is != null) {
            ivLangFlag.setImage(new Image(is));
        }
        String tooltipKey = (current == I18n.Language.ENGLISH) ? "lang.tooltip.vi" : "lang.tooltip.en";
        Tooltip.install(langTogglePane, new Tooltip(I18n.get(tooltipKey)));
    }

    @FXML
    public void handleToggleLanguage() {
        I18n.Language current = I18n.getCurrentLanguage();
        I18n.Language next = (current == I18n.Language.ENGLISH) ? I18n.Language.VIETNAMESE : I18n.Language.ENGLISH;
        I18n.setLanguage(next);
        updateLangButton();
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

        try {
            ctx.saveSettings();
            ToastManager.showSuccess("Đã lưu cấu hình hệ thống vào SQL Server!");
        } catch (RuntimeException ex) {
            ToastManager.showOperationError("Không thể lưu cấu hình", ex);
        }
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

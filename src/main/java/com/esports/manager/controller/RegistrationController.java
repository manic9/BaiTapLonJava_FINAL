package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Registration;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.ToastManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistrationController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Object> cbTournamentFilter;
    @FXML private ComboBox<Object> cbStatusFilter;
    @FXML private Label lblResultCount;
    @FXML private TableView<Registration> registrationTable;

    @FXML private TableColumn<Registration, String> colId;
    @FXML private TableColumn<Registration, String> colTournament;
    @FXML private TableColumn<Registration, String> colTeam;
    @FXML private TableColumn<Registration, String> colSeed;
    @FXML private TableColumn<Registration, String> colRegisteredAt;
    @FXML private TableColumn<Registration, String> colNotes;
    @FXML private TableColumn<Registration, RegistrationStatus> colStatus;
    @FXML private TableColumn<Registration, Void> colActions;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Registration> filteredData;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        bindData();
    }

    private void setupFilters() {
        List<Object> trnOptions = new ArrayList<>();
        trnOptions.add("Tất cả giải đấu");
        trnOptions.addAll(ctx.getTournamentService().getAll());
        cbTournamentFilter.setItems(FXCollections.observableArrayList(trnOptions));
        cbTournamentFilter.getSelectionModel().selectFirst();

        cbStatusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả trạng thái",
                RegistrationStatus.PENDING,
                RegistrationStatus.APPROVED,
                RegistrationStatus.REJECTED
        ));
        cbStatusFilter.getSelectionModel().selectFirst();

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbTournamentFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbStatusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));

        colTournament.setCellValueFactory(d -> new SimpleStringProperty(
                ctx.getTournamentService().findById(d.getValue().getTournamentId())
                        .map(Tournament::getName).orElse(d.getValue().getTournamentId())
        ));

        colTeam.setCellValueFactory(d -> new SimpleStringProperty(
                ctx.getTeamService().findById(d.getValue().getTeamId())
                        .map(Team::toString).orElse(d.getValue().getTeamId())
        ));
        colTeam.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
                }
            }
        });

        colSeed.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSeedNumber() > 0 ? "#" + d.getValue().getSeedNumber() : "-"
        ));
        colSeed.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String seed, boolean empty) {
                super.updateItem(seed, empty);
                if (empty || seed == null) {
                    setText(null);
                } else {
                    setText(seed);
                    setStyle("-fx-text-fill: #00D4FF; -fx-font-weight: bold;");
                }
            }
        });

        colRegisteredAt.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRegisteredAt() != null ? d.getValue().getRegisteredAt().format(TIME_FMT) : "-"
        ));

        colNotes.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));

        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RegistrationStatus st, boolean empty) {
                super.updateItem(st, empty);
                if (empty || st == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(st.getDisplayName());
                    badge.getStyleClass().addAll("badge", st.getBadgeStyle());
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnApprove = new Button();
            private final Button btnReject = new Button();
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox box = new HBox(6, btnApprove, btnReject, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);

                btnApprove.setText("✓");
                btnApprove.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
                btnApprove.getStyleClass().add("btn-icon");
                btnApprove.setTooltip(new Tooltip("Duyệt tham gia giải"));

                btnReject.setText("✗");
                btnReject.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 12px;");
                btnReject.getStyleClass().add("btn-icon");
                btnReject.setTooltip(new Tooltip("Từ chối hồ sơ"));

                btnEdit.setText("✎");
                btnEdit.setStyle("-fx-text-fill: #00D4FF; -fx-font-size: 12px;");
                btnEdit.getStyleClass().add("btn-icon");
                btnEdit.setTooltip(new Tooltip("Sửa Seed & Ghi chú"));

                btnDelete.setText("🗑");
                btnDelete.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px;");
                btnDelete.getStyleClass().add("btn-icon");
                btnDelete.setTooltip(new Tooltip("Xóa đơn đăng ký"));

                btnApprove.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    updateRegStatus(reg, RegistrationStatus.APPROVED);
                });

                btnReject.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    updateRegStatus(reg, RegistrationStatus.REJECTED);
                });

                btnEdit.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    showEditSeedNotesDialog(reg);
                });

                btnDelete.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    handleDeleteRegistration(reg);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Registration reg = getTableView().getItems().get(getIndex());
                    btnApprove.setDisable(reg.getStatus() == RegistrationStatus.APPROVED);
                    btnReject.setDisable(reg.getStatus() == RegistrationStatus.REJECTED);
                    setGraphic(box);
                }
            }
        });
    }

    private void bindData() {
        filteredData = new FilteredList<>(ctx.getRegistrationService().getAll(), p -> true);
        SortedList<Registration> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(registrationTable.comparatorProperty());
        registrationTable.setItems(sortedData);
        updateCountLabel();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        Object selectedTrn = cbTournamentFilter.getValue();
        Object selectedStatus = cbStatusFilter.getValue();

        filteredData.setPredicate(r -> {
            String teamName = ctx.getTeamService().getTeamNameOrTbd(r.getTeamId()).toLowerCase();
            String trnName = ctx.getTournamentService().findById(r.getTournamentId())
                    .map(t -> t.getName().toLowerCase()).orElse("");

            boolean matchKeyword = keyword.isEmpty()
                    || r.getId().toLowerCase().contains(keyword)
                    || teamName.contains(keyword)
                    || trnName.contains(keyword)
                    || (r.getNotes() != null && r.getNotes().toLowerCase().contains(keyword));

            boolean matchTrn = !(selectedTrn instanceof Tournament t)
                    || r.getTournamentId().equals(t.getId());

            boolean matchStatus = !(selectedStatus instanceof RegistrationStatus st)
                    || r.getStatus() == st;

            return matchKeyword && matchTrn && matchStatus;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText("Hiển thị " + filteredData.size() + " đơn đăng ký");
    }

    private void updateRegStatus(Registration reg, RegistrationStatus status) {
        try {
            ctx.getRegistrationService().updateStatus(reg.getId(), status);
            registrationTable.refresh();
            ToastManager.showSuccess("Đã chuyển trạng thái hồ sơ sang: " + status.getDisplayName());
        } catch (IllegalStateException ex) {
            ToastManager.showError(ex.getMessage());
        }
    }

    private void handleDeleteRegistration(Registration reg) {
        String teamName = ctx.getTeamService().getTeamNameOrTbd(reg.getTeamId());
        if (!ctx.getSettings().isConfirmBeforeDelete() || DialogHelper.confirmDelete(
                "Xóa đơn đăng ký",
                "Bạn có chắc chắn muốn xóa hồ sơ đăng ký của đội \"" + teamName + "\" (" + reg.getId() + ")?")) {
            if (ctx.getRegistrationService().delete(reg.getId())) {
                registrationTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess("Đã xóa đơn đăng ký: " + reg.getId());
            }
        }
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cbTournamentFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleCreateRegistration() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký đội tuyển vào giải đấu");
        dialog.setHeaderText("Tạo hồ sơ tham dự giải đấu mới");
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSubmit = new ButtonType("Gửi đăng ký", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        ComboBox<Tournament> cbTrn = new ComboBox<>(ctx.getTournamentService().getAll());
        cbTrn.setPrefWidth(290);
        if (!cbTrn.getItems().isEmpty()) cbTrn.getSelectionModel().selectFirst();

        ComboBox<Team> cbTeam = new ComboBox<>(ctx.getTeamService().getAll());
        cbTeam.setPrefWidth(290);
        if (!cbTeam.getItems().isEmpty()) cbTeam.getSelectionModel().selectFirst();

        Spinner<Integer> spSeed = new Spinner<>(0, 64, 0);
        spSeed.setEditable(true);
        spSeed.setMaxWidth(Double.MAX_VALUE);

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Ghi chú kiểm tra roster, hạt giống...");

        CheckBox chkAutoApprove = new CheckBox("Duyệt tham gia ngay sau khi tạo");
        chkAutoApprove.setSelected(true);

        grid.add(new Label("Giải đấu *:"), 0, 0);
        grid.add(cbTrn, 1, 0);
        grid.add(new Label("Đội tuyển *:"), 0, 1);
        grid.add(cbTeam, 1, 1);
        grid.add(new Label("Số hạt giống (0 = tự động):"), 0, 2);
        grid.add(spSeed, 1, 2);
        grid.add(new Label("Ghi chú:"), 0, 3);
        grid.add(txtNotes, 1, 3);
        grid.add(chkAutoApprove, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button submitBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cbTrn.getValue() == null || cbTeam.getValue() == null) {
                ToastManager.showError("Vui lòng chọn đầy đủ Giải đấu và Đội tuyển!");
                ev.consume();
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSubmit) {
            try {
                Registration reg = new Registration();
                reg.setTournamentId(cbTrn.getValue().getId());
                reg.setTeamId(cbTeam.getValue().getId());
                reg.setSeedNumber(spSeed.getValue());
                reg.setNotes(txtNotes.getText().trim().isEmpty() ? "Đăng ký trực tiếp qua hệ thống" : txtNotes.getText().trim());

                ctx.getRegistrationService().register(reg);
                if (chkAutoApprove.isSelected()) {
                    ctx.getRegistrationService().updateStatus(reg.getId(), RegistrationStatus.APPROVED);
                }
                registrationTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess("Đã đăng ký đội " + cbTeam.getValue().getName() + " vào giải đấu!");
            } catch (IllegalStateException ex) {
                ToastManager.showError(ex.getMessage());
            }
        }
    }

    private void showEditSeedNotesDialog(Registration reg) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật Seed & Ghi chú");
        dialog.setHeaderText("Hồ sơ đội: " + ctx.getTeamService().getTeamNameOrTbd(reg.getTeamId()));
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType("Lưu cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        Spinner<Integer> spSeed = new Spinner<>(0, 64, reg.getSeedNumber());
        spSeed.setEditable(true);
        spSeed.setPrefWidth(250);

        TextField txtNotes = new TextField(reg.getNotes() != null ? reg.getNotes() : "");
        txtNotes.setPrefWidth(250);

        grid.add(new Label("Số hạt giống (Seed):"), 0, 0);
        grid.add(spSeed, 1, 0);
        grid.add(new Label("Ghi chú hồ sơ:"), 0, 1);
        grid.add(txtNotes, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == btnSave) {
                ctx.getRegistrationService().updateSeedAndNotes(reg.getId(), spSeed.getValue(), txtNotes.getText().trim());
                registrationTable.refresh();
                ToastManager.showSuccess("Đã cập nhật thông tin Seed & Ghi chú!");
            }
        });
    }
}
package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Player;
import com.esports.manager.model.Team;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.ToastManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PlayerController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Object> cbTeamFilter;
    @FXML private ComboBox<String> cbRoleFilter;
    @FXML private Label lblResultCount;
    @FXML private TableView<Player> playerTable;

    @FXML private TableColumn<Player, String> colId;
    @FXML private TableColumn<Player, String> colIgn;
    @FXML private TableColumn<Player, String> colFullName;
    @FXML private TableColumn<Player, String> colTeam;
    @FXML private TableColumn<Player, String> colRole;
    @FXML private TableColumn<Player, String> colNationality;
    @FXML private TableColumn<Player, String> colAge;
    @FXML private TableColumn<Player, String> colKda;
    @FXML private TableColumn<Player, Void> colActions;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Player> filteredData;

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        bindData();
    }

    private void setupFilters() {
        List<Object> teamOptions = new ArrayList<>();
        teamOptions.add("Tất cả đội tuyển");
        teamOptions.addAll(ctx.getTeamService().getAll());
        cbTeamFilter.setItems(FXCollections.observableArrayList(teamOptions));
        cbTeamFilter.getSelectionModel().selectFirst();

        cbRoleFilter.setItems(FXCollections.observableArrayList(
                "Tất cả vị trí", "Top Lane", "Jungler", "Mid Lane", "AD Carry", "Support",
                "Duelist", "Initiator", "Controller", "Sentinel", "AWPer", "Rifler"
        ));
        cbRoleFilter.getSelectionModel().selectFirst();

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbTeamFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbRoleFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    public void filterByTeam(String teamId) {
        if (teamId == null) return;
        for (Object item : cbTeamFilter.getItems()) {
            if (item instanceof Team t && t.getId().equals(teamId)) {
                cbTeamFilter.getSelectionModel().select(t);
                applyFilters();
                return;
            }
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));

        colIgn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInGameName()));
        colIgn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String ign, boolean empty) {
                super.updateItem(ign, empty);
                if (empty || ign == null) {
                    setGraphic(null);
                } else {
                    Player p = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label lblIgn = new Label(ign);
                    lblIgn.setStyle("-fx-font-weight: bold; -fx-text-fill: #F8FAFC; -fx-font-size: 13px;");
                    box.getChildren().add(lblIgn);

                    if (p.isCaptain()) {
                        Label capBadge = new Label("CAPTAIN");
                        capBadge.setStyle("-fx-background-color: rgba(245, 158, 11, 0.18); -fx-text-fill: #FBBF24; " +
                                "-fx-border-color: rgba(245, 158, 11, 0.45); -fx-border-radius: 5px; " +
                                "-fx-background-radius: 5px; -fx-font-size: 9.5px; -fx-font-weight: bold; -fx-padding: 1 5;");
                        box.getChildren().add(capBadge);
                    }
                    setGraphic(box);
                }
            }
        });

        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));

        colTeam.setCellValueFactory(d -> {
            String teamId = d.getValue().getTeamId();
            return new SimpleStringProperty(
                    ctx.getTeamService().findById(teamId).map(Team::toString).orElse("Tự do (Free Agent)")
            );
        });

        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label pill = new Label(role);
                    pill.setStyle("-fx-background-color: rgba(124, 92, 252, 0.16); -fx-text-fill: #A78BFA; " +
                            "-fx-padding: 3 9; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(pill);
                }
            }
        });

        colNationality.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNationality()));
        colAge.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getAge())));

        colKda.setCellValueFactory(d -> new SimpleStringProperty(String.format(Locale.US, "%.2f", d.getValue().getKdaRatio())));
        colKda.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String kda, boolean empty) {
                super.updateItem(kda, empty);
                if (empty || kda == null) {
                    setText(null);
                } else {
                    setText(kda);
                    setStyle("-fx-text-fill: #00D4FF; -fx-font-weight: bold;");
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEdit.setText("✎");
                btnEdit.setStyle("-fx-text-fill: #00D4FF; -fx-font-size: 13px;");
                btnEdit.getStyleClass().add("btn-icon");
                btnEdit.setTooltip(new Tooltip("Chỉnh sửa tuyển thủ"));

                btnDelete.setText("🗑");
                btnDelete.setStyle("-fx-text-fill: #F87171; -fx-font-size: 13px;");
                btnDelete.getStyleClass().add("btn-icon");
                btnDelete.setTooltip(new Tooltip("Xóa tuyển thủ"));

                btnEdit.setOnAction(e -> {
                    Player p = getTableView().getItems().get(getIndex());
                    showPlayerFormDialog(p);
                });

                btnDelete.setOnAction(e -> {
                    Player p = getTableView().getItems().get(getIndex());
                    handleDeletePlayer(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void bindData() {
        filteredData = new FilteredList<>(ctx.getPlayerService().getAll(), p -> true);
        SortedList<Player> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(playerTable.comparatorProperty());
        playerTable.setItems(sortedData);
        updateCountLabel();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        Object selectedTeam = cbTeamFilter.getValue();
        String selectedRole = cbRoleFilter.getValue();

        filteredData.setPredicate(p -> {
            boolean matchKeyword = keyword.isEmpty()
                    || p.getInGameName().toLowerCase().contains(keyword)
                    || p.getFullName().toLowerCase().contains(keyword)
                    || p.getId().toLowerCase().contains(keyword)
                    || (p.getNationality() != null && p.getNationality().toLowerCase().contains(keyword));

            boolean matchTeam = !(selectedTeam instanceof Team t)
                    || (p.getTeamId() != null && p.getTeamId().equals(t.getId()));

            boolean matchRole = selectedRole == null
                    || "Tất cả vị trí".equals(selectedRole)
                    || (p.getRole() != null && p.getRole().equalsIgnoreCase(selectedRole));

            return matchKeyword && matchTeam && matchRole;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText("Hiển thị " + filteredData.size() + " tuyển thủ");
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cbTeamFilter.getSelectionModel().selectFirst();
        cbRoleFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleCreatePlayer() {
        showPlayerFormDialog(null);
    }

    private void handleDeletePlayer(Player p) {
        if (!ctx.getSettings().isConfirmBeforeDelete() || DialogHelper.confirmDelete(
                "Xóa tuyển thủ",
                "Bạn có chắc chắn muốn xóa tuyển thủ \"" + p.getInGameName() + "\" (" + p.getFullName() + ")?")) {
            if (ctx.getPlayerService().delete(p.getId())) {
                playerTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess("Đã xóa tuyển thủ: " + p.getInGameName());
            } else {
                ToastManager.showError("Không thể xóa tuyển thủ này.");
            }
        }
    }

    private void showPlayerFormDialog(Player existing) {
        boolean isEdit = existing != null;
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chỉnh sửa tuyển thủ" : "Thêm tuyển thủ mới");
        dialog.setHeaderText(isEdit ? "Cập nhật hồ sơ: " + existing.getInGameName() : "Đăng ký tuyển thủ mới vào hệ thống");
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(isEdit ? "Lưu thay đổi" : "Thêm tuyển thủ", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        ComboBox<Team> cbTeam = new ComboBox<>(ctx.getTeamService().getAll());
        cbTeam.setPrefWidth(285);
        if (isEdit && existing.getTeamId() != null) {
            ctx.getTeamService().findById(existing.getTeamId()).ifPresent(cbTeam::setValue);
        } else if (!cbTeam.getItems().isEmpty()) {
            cbTeam.getSelectionModel().selectFirst();
        }

        TextField txtIgn = new TextField(isEdit ? existing.getInGameName() : "");
        txtIgn.setPromptText("VD: Faker, Levi, TenZ...");

        TextField txtFullName = new TextField(isEdit ? existing.getFullName() : "");
        txtFullName.setPromptText("Họ và tên đầy đủ");

        ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList(
                "Top Lane", "Jungler", "Mid Lane", "AD Carry", "Support",
                "Duelist", "Initiator", "Controller", "Sentinel", "AWPer", "Rifler"
        ));
        cbRole.setEditable(true);
        cbRole.setValue(isEdit ? existing.getRole() : "Mid Lane");
        cbRole.setMaxWidth(Double.MAX_VALUE);

        TextField txtNationality = new TextField(isEdit ? existing.getNationality() : "Vietnam");
        Spinner<Integer> spAge = new Spinner<>(15, 45, isEdit ? existing.getAge() : 21);
        spAge.setEditable(true);
        spAge.setMaxWidth(Double.MAX_VALUE);

        TextField txtKda = new TextField(isEdit ? String.valueOf(existing.getKdaRatio()) : "3.50");
        CheckBox chkCaptain = new CheckBox("Đội trưởng (Team Captain)");
        chkCaptain.setSelected(isEdit && existing.isCaptain());

        grid.add(new Label("Đội tuyển *:"), 0, 0);
        grid.add(cbTeam, 1, 0);
        grid.add(new Label("In-Game Name *:"), 0, 1);
        grid.add(txtIgn, 1, 1);
        grid.add(new Label("Họ và tên *:"), 0, 2);
        grid.add(txtFullName, 1, 2);
        grid.add(new Label("Vị trí thi đấu:"), 0, 3);
        grid.add(cbRole, 1, 3);
        grid.add(new Label("Quốc tịch:"), 0, 4);
        grid.add(txtNationality, 1, 4);
        grid.add(new Label("Tuổi:"), 0, 5);
        grid.add(spAge, 1, 5);
        grid.add(new Label("Chỉ số KDA:"), 0, 6);
        grid.add(txtKda, 1, 6);
        grid.add(chkCaptain, 1, 7);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cbTeam.getValue() == null || txtIgn.getText().trim().isEmpty() || txtFullName.getText().trim().isEmpty()) {
                ToastManager.showError("Vui lòng nhập đầy đủ Đội tuyển, IGN và Họ tên!");
                ev.consume();
                return;
            }
            try {
                double kda = Double.parseDouble(txtKda.getText().trim());
                if (kda < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ToastManager.showError("Chỉ số KDA phải là số không âm (VD: 4.25)!");
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                Player target = isEdit ? existing : new Player();
                target.setTeamId(cbTeam.getValue().getId());
                target.setInGameName(txtIgn.getText().trim());
                target.setFullName(txtFullName.getText().trim());
                target.setRole(cbRole.getValue() != null ? cbRole.getValue().trim() : "Flex");
                target.setNationality(txtNationality.getText().trim());
                target.setAge(spAge.getValue());
                target.setKdaRatio(Double.parseDouble(txtKda.getText().trim()));
                target.setCaptain(chkCaptain.isSelected());
                return target;
            }
            return null;
        });

        Optional<Player> res = dialog.showAndWait();
        res.ifPresent(player -> {
            ctx.getPlayerService().save(player, !isEdit);
            playerTable.refresh();
            updateCountLabel();
            ToastManager.showSuccess((isEdit ? "Đã cập nhật tuyển thủ: " : "Đã thêm tuyển thủ: ") + player.getInGameName());
        });
    }
}
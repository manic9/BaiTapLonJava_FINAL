package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Player;
import com.esports.manager.model.Team;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.I18n;
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

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Player> filteredData;

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        setupRowContextMenu();
        bindData();
    }

    private void setupFilters() {
        List<Object> teamOptions = new ArrayList<>();
        teamOptions.add(com.esports.manager.util.I18n.get("filter.all_teams"));
        teamOptions.addAll(ctx.getTeamService().getAll());
        cbTeamFilter.setItems(FXCollections.observableArrayList(teamOptions));
        cbTeamFilter.getSelectionModel().selectFirst();

        cbRoleFilter.setItems(FXCollections.observableArrayList(
                com.esports.manager.util.I18n.get("filter.all_roles"), "Top Lane", "Jungler", "Mid Lane", "AD Carry", "Support",
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
                        Label capBadge = new Label(I18n.get("player.captain"));
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
                    ctx.getTeamService().findById(teamId).map(Team::toString).orElse(I18n.get("player.free_agent"))
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

    }

    private void setupRowContextMenu() {
        MenuItem miEdit = new MenuItem(I18n.get("player.tooltip.edit"));
        MenuItem miDelete = new MenuItem(I18n.get("player.tooltip.delete"));

        ContextMenu rowMenu = new ContextMenu(miEdit, miDelete);
        rowMenu.getStyleClass().add("context-menu-dark");

        miEdit.setOnAction(e -> {
            Player p = playerTable.getSelectionModel().getSelectedItem();
            if (p != null) showPlayerFormDialog(p);
        });

        miDelete.setOnAction(e -> {
            Player p = playerTable.getSelectionModel().getSelectedItem();
            if (p != null) handleDeletePlayer(p);
        });

        playerTable.setRowFactory(tv -> {
            TableRow<Player> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    rowMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            row.setOnMouseClicked(event -> {
                if (event.isPrimaryButtonDown() && !event.isShiftDown() && !event.isControlDown()) {
                    rowMenu.hide();
                }
            });
            return row;
        });

        playerTable.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                rowMenu.hide();
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
                    || I18n.get("filter.all_roles").equals(selectedRole)
                    || (p.getRole() != null && p.getRole().equalsIgnoreCase(selectedRole));

            return matchKeyword && matchTeam && matchRole;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText(com.esports.manager.util.I18n.get("player.count_format", filteredData.size()));
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
                I18n.get("player.delete.title"),
                String.format(I18n.get("player.delete.message"), p.getInGameName(), p.getFullName()))) {
            if (ctx.getPlayerService().delete(p.getId())) {
                playerTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess(String.format(I18n.get("player.toast.deleted"), p.getInGameName()));
            } else {
                ToastManager.showError(I18n.get("player.toast.delete_error"));
            }
        }
    }

    private void showPlayerFormDialog(Player existing) {
        boolean isEdit = existing != null;
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? I18n.get("player.dialog.edit_title") : I18n.get("player.dialog.create_title"));
        dialog.setHeaderText(isEdit ? I18n.get("player.dialog.edit_header") + existing.getInGameName() : I18n.get("player.dialog.create_header"));
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(isEdit ? I18n.get("player.dialog.save_edit") : I18n.get("player.dialog.save_create"), ButtonBar.ButtonData.OK_DONE);
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
        txtIgn.setPromptText(I18n.get("player.dialog.ign_prompt"));

        TextField txtFullName = new TextField(isEdit ? existing.getFullName() : "");
        txtFullName.setPromptText(I18n.get("player.dialog.fullname_prompt"));

        ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList(
                "Top Lane", "Jungler", "Mid Lane", "AD Carry", "Support",
                "Duelist", "Initiator", "Controller", "Sentinel", "AWPer", "Rifler"
        ));
        cbRole.setEditable(true);
        cbRole.setValue(isEdit ? existing.getRole() : "Mid Lane");
        cbRole.setMaxWidth(Double.MAX_VALUE);

        TextField txtNationality = new TextField(isEdit ? existing.getNationality() : "Vietnam");
        Spinner<Integer> spAge = new Spinner<>(15, 45, isEdit ? existing.getAge() : 18);
        spAge.setEditable(true);
        spAge.setMaxWidth(Double.MAX_VALUE);

        TextField txtKda = new TextField(isEdit ? String.valueOf(existing.getKdaRatio()) : "3.50");
        CheckBox chkCaptain = new CheckBox(I18n.get("player.captain_checkbox"));
        chkCaptain.setSelected(isEdit && existing.isCaptain());

        grid.add(new Label(I18n.get("player.dialog.lbl_team")), 0, 0);
        grid.add(cbTeam, 1, 0);
        grid.add(new Label(I18n.get("player.dialog.lbl_ign")), 0, 1);
        grid.add(txtIgn, 1, 1);
        grid.add(new Label(I18n.get("player.dialog.lbl_fullname")), 0, 2);
        grid.add(txtFullName, 1, 2);
        grid.add(new Label(I18n.get("player.dialog.lbl_role")), 0, 3);
        grid.add(cbRole, 1, 3);
        grid.add(new Label(I18n.get("player.dialog.lbl_nationality")), 0, 4);
        grid.add(txtNationality, 1, 4);
        grid.add(new Label(I18n.get("player.dialog.lbl_age")), 0, 5);
        grid.add(spAge, 1, 5);
        grid.add(new Label(I18n.get("player.dialog.lbl_kda")), 0, 6);
        grid.add(txtKda, 1, 6);
        grid.add(chkCaptain, 1, 7);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cbTeam.getValue() == null || txtIgn.getText().trim().isEmpty() || txtFullName.getText().trim().isEmpty()) {
                ToastManager.showError(I18n.get("player.dialog.validation_error"));
                ev.consume();
                return;
            }
            try {
                double kda = Double.parseDouble(txtKda.getText().trim());
                if (kda < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ToastManager.showError(I18n.get("player.dialog.kda_error"));
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
            String msgKey = isEdit ? "player.toast.updated" : "player.toast.created";
            ToastManager.showSuccess(String.format(I18n.get(msgKey), player.getInGameName()));
        });
    }
}
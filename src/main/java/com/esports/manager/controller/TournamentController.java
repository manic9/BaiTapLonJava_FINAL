package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.BracketFormat;
import com.esports.manager.model.enums.TournamentStatus;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.I18n;
import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
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

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TournamentController {

    // TournamentView.fxml
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbGameFilter;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private Label lblResultCount;
    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, String> colId;
    @FXML private TableColumn<Tournament, String> colName;
    @FXML private TableColumn<Tournament, String> colGame;
    @FXML private TableColumn<Tournament, String> colFormat;
    @FXML private TableColumn<Tournament, String> colTeams;
    @FXML private TableColumn<Tournament, String> colPrize;
    @FXML private TableColumn<Tournament, String> colDates;
    @FXML private TableColumn<Tournament, String> colStatus;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Tournament> filteredData;
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        setupRowContextMenu();
        bindData();
    }

    private void setupFilters() {
        // Thu thập danh sách tựa game từ dữ liệu mẫu
        Set<String> games = new LinkedHashSet<>(List.of(I18n.get("filter.all_games"), "League of Legends", "VALORANT", "CS:GO", "Dota 2", "FC Online"));
        for (Tournament t : ctx.getTournamentService().getAll()) {
            if (t.getGameTitle() != null && !t.getGameTitle().isBlank()) {
                games.add(t.getGameTitle());
            }
        }
        cbGameFilter.setItems(FXCollections.observableArrayList(games));
        cbGameFilter.getSelectionModel().selectFirst();

        List<String> statuses = new ArrayList<>();
        statuses.add(I18n.get("filter.all_statuses"));
        for (TournamentStatus s : TournamentStatus.values()) {
            statuses.add(s.getDisplayName());
        }
        cbStatusFilter.setItems(FXCollections.observableArrayList(statuses));
        cbStatusFilter.getSelectionModel().selectFirst();

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbGameFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbStatusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    setText(id);
                    setStyle("-fx-text-fill: #64748B; -fx-font-weight: bold;");
                }
            }
        });

        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                } else {
                    setText(name);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
                }
            }
        });

        colGame.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGameTitle()));
        colGame.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String game, boolean empty) {
                super.updateItem(game, empty);
                if (empty || game == null) {
                    setGraphic(null);
                } else {
                    Label pill = new Label(game);
                    pill.setStyle("-fx-background-color: rgba(124, 92, 252, 0.18); -fx-text-fill: #A78BFA; " +
                            "-fx-padding: 3 8; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(pill);
                }
            }
        });

        colFormat.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFormat() != null ? d.getValue().getFormat().getDisplayName() : "-"
        ));

        colTeams.setCellValueFactory(d -> {
            int approved = ctx.getRegistrationService().getApprovedByTournament(d.getValue().getId()).size();
            return new SimpleStringProperty(approved + " / " + d.getValue().getMaxTeams());
        });
        colTeams.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String teams, boolean empty) {
                super.updateItem(teams, empty);
                if (empty || teams == null) {
                    setText(null);
                } else {
                    setText(teams);
                    setStyle("-fx-text-fill: #00D4FF; -fx-font-weight: 600;");
                }
            }
        });

        colPrize.setCellValueFactory(d -> {
            String symbol = ctx.getSettings().getCurrencySymbol();
            return new SimpleStringProperty(symbol + currencyFmt.format(d.getValue().getPrizePool()));
        });
        colPrize.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String prize, boolean empty) {
                super.updateItem(prize, empty);
                if (empty || prize == null) {
                    setText(null);
                } else {
                    setText(prize);
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                }
            }
        });

        colDates.setCellValueFactory(d -> {
            Tournament t = d.getValue();
            String start = t.getStartDate() != null ? t.getStartDate().format(dateFmt) : "?";
            String end = t.getEndDate() != null ? t.getEndDate().format(dateFmt) : "?";
            return new SimpleStringProperty(start + " - " + end);
        });

        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().getDisplayName() : ""
        ));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Tournament t = getTableView().getItems().get(getIndex());
                    Label badge = new Label(status);
                    badge.getStyleClass().addAll("badge", t.getStatus().getBadgeStyle());
                    setGraphic(badge);
                }
            }
        });

    }

    private void setupRowContextMenu() {
        MenuItem miDetail = new MenuItem(I18n.get("btn.details"));
        MenuItem miBracket = new MenuItem(I18n.get("btn.bracket"));
        MenuItem miEdit = new MenuItem(I18n.get("tournament.tooltip.edit"));
        MenuItem miDelete = new MenuItem(I18n.get("tournament.tooltip.delete"));

        ContextMenu rowMenu = new ContextMenu(miDetail, miBracket, new SeparatorMenuItem(), miEdit, miDelete);
        rowMenu.getStyleClass().add("context-menu-dark");

        miDetail.setOnAction(e -> {
            Tournament t = tournamentTable.getSelectionModel().getSelectedItem();
            if (t != null) {
                ViewNavigator.<TournamentDetailController>navigate(
                        "TournamentDetailView.fxml", t.getName(),
                        ctrl -> ctrl.setTournament(t)
                );
            }
        });

        miBracket.setOnAction(e -> {
            Tournament t = tournamentTable.getSelectionModel().getSelectedItem();
            if (t != null) {
                ViewNavigator.<BracketController>navigate(
                        "BracketView.fxml", I18n.get("page.bracket.title"),
                        ctrl -> ctrl.selectTournament(t.getId())
                );
            }
        });

        miEdit.setOnAction(e -> {
            Tournament t = tournamentTable.getSelectionModel().getSelectedItem();
            if (t != null) showTournamentFormDialog(t);
        });

        miDelete.setOnAction(e -> {
            Tournament t = tournamentTable.getSelectionModel().getSelectedItem();
            if (t != null) handleDeleteTournament(t);
        });

        tournamentTable.setRowFactory(tv -> {
            TableRow<Tournament> row = new TableRow<>();
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

        tournamentTable.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                rowMenu.hide();
            }
        });
    }

    private void bindData() {
        filteredData = new FilteredList<>(ctx.getTournamentService().getAll(), t -> true);
        SortedList<Tournament> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tournamentTable.comparatorProperty());
        tournamentTable.setItems(sortedData);
        updateCountLabel();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String selectedGame = cbGameFilter.getValue();
        String selectedStatus = cbStatusFilter.getValue();

        filteredData.setPredicate(t -> {
            boolean matchKeyword = keyword.isEmpty()
                    || (t.getName() != null && t.getName().toLowerCase().contains(keyword))
                    || (t.getGameTitle() != null && t.getGameTitle().toLowerCase().contains(keyword))
                    || (t.getOrganizer() != null && t.getOrganizer().toLowerCase().contains(keyword))
                    || (t.getId() != null && t.getId().toLowerCase().contains(keyword));

            boolean matchGame = selectedGame == null
                    || I18n.get("filter.all_games").equals(selectedGame)
                    || (t.getGameTitle() != null && t.getGameTitle().equalsIgnoreCase(selectedGame));

            boolean matchStatus = selectedStatus == null
                    || I18n.get("filter.all_statuses").equals(selectedStatus)
                    || (t.getStatus() != null && t.getStatus().getDisplayName().equalsIgnoreCase(selectedStatus));

            return matchKeyword && matchGame && matchStatus;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText(I18n.get("tournament.count_format", filteredData.size()));
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cbGameFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void openCreateModal() {
        showTournamentFormDialog(null);
    }

    public void applyExternalSearch(String keyword) {
        if (txtSearch != null && keyword != null) {
            txtSearch.setText(keyword);
        }
    }

    private void handleDeleteTournament(Tournament t) {
        if (!ctx.getSettings().isConfirmBeforeDelete() || DialogHelper.confirmDelete(
                I18n.get("tournament.delete.title"),
                String.format(I18n.get("tournament.delete.message"), t.getName(), t.getId()))) {
            if (ctx.getTournamentService().delete(t.getId())) {
                tournamentTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess(String.format(I18n.get("tournament.toast.deleted"), t.getName()));
            } else {
                ToastManager.showError(I18n.get("tournament.toast.delete_error"));
            }
        }
    }

    private void showTournamentFormDialog(Tournament existing) {
        boolean isEdit = existing != null;
        Dialog<Tournament> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? I18n.get("tournament.dialog.edit_title") : I18n.get("tournament.dialog.create_title"));
        dialog.setHeaderText(isEdit ? I18n.get("tournament.dialog.edit_header") + existing.getName() : I18n.get("tournament.dialog.create_header"));
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(isEdit ? I18n.get("tournament.dialog.save_edit") : I18n.get("tournament.dialog.save_create"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        TextField txtName = new TextField(isEdit ? existing.getName() : "");
        txtName.setPromptText(I18n.get("tournament.dialog.name_prompt"));
        txtName.setPrefWidth(300);

        ComboBox<String> cbGame = new ComboBox<>(FXCollections.observableArrayList(
                "League of Legends", "VALORANT", "CS:GO", "Dota 2", "FC Online", "PUBG"
        ));
        cbGame.setEditable(true);
        cbGame.setValue(isEdit ? existing.getGameTitle() : "League of Legends");
        cbGame.setMaxWidth(Double.MAX_VALUE);

        TextField txtOrganizer = new TextField(isEdit ? existing.getOrganizer() : "");
        txtOrganizer.setPromptText(I18n.get("tournament.dialog.organizer_prompt"));

        ComboBox<BracketFormat> cbFormat = new ComboBox<>(FXCollections.observableArrayList(BracketFormat.values()));
        cbFormat.setValue(isEdit ? existing.getFormat() : BracketFormat.SINGLE_ELIMINATION);
        cbFormat.setMaxWidth(Double.MAX_VALUE);

        ComboBox<TournamentStatus> cbStatus = new ComboBox<>(FXCollections.observableArrayList(TournamentStatus.values()));
        cbStatus.setValue(isEdit ? existing.getStatus() : TournamentStatus.UPCOMING);
        cbStatus.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> spMaxTeams = new Spinner<>(2, 64, isEdit ? existing.getMaxTeams() : 8, 2);
        spMaxTeams.setEditable(true);
        spMaxTeams.setMaxWidth(Double.MAX_VALUE);

        TextField txtPrize = new TextField(isEdit ? String.valueOf((long) existing.getPrizePool()) : "25000");
        txtPrize.setPromptText("VD: 25000");

        DatePicker dpStart = new DatePicker(isEdit ? existing.getStartDate() : LocalDate.now().plusDays(7));
        DatePicker dpEnd = new DatePicker(isEdit ? existing.getEndDate() : LocalDate.now().plusDays(17));
        dpStart.setMaxWidth(Double.MAX_VALUE);
        dpEnd.setMaxWidth(Double.MAX_VALUE);

        TextField txtLocation = new TextField(isEdit ? existing.getLocation() : I18n.get("tournament.dialog.location_default"));
        txtLocation.setPromptText(I18n.get("tournament.dialog.location_prompt"));

        TextArea txtDesc = new TextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        txtDesc.setPromptText(I18n.get("tournament.dialog.desc_prompt"));
        txtDesc.setPrefRowCount(3);

        grid.add(new Label(I18n.get("tournament.dialog.lbl_name")), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_game")), 0, 1);
        grid.add(cbGame, 1, 1);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_organizer")), 0, 2);
        grid.add(txtOrganizer, 1, 2);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_format")), 0, 3);
        grid.add(cbFormat, 1, 3);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_status")), 0, 4);
        grid.add(cbStatus, 1, 4);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_teams")), 0, 5);
        grid.add(spMaxTeams, 1, 5);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_prize")), 0, 6);
        grid.add(txtPrize, 1, 6);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_start")), 0, 7);
        grid.add(dpStart, 1, 7);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_end")), 0, 8);
        grid.add(dpEnd, 1, 8);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_location")), 0, 9);
        grid.add(txtLocation, 1, 9);
        grid.add(new Label(I18n.get("tournament.dialog.lbl_desc")), 0, 10);
        grid.add(txtDesc, 1, 10);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (txtName.getText().trim().isEmpty() || txtOrganizer.getText().trim().isEmpty()) {
                ToastManager.showError(I18n.get("tournament.dialog.validation_error"));
                ev.consume();
                return;
            }
            double prize;
            try {
                prize = Double.parseDouble(txtPrize.getText().trim().replace(",", ""));
            } catch (NumberFormatException e) {
                ToastManager.showError(I18n.get("tournament.dialog.prize_error"));
                ev.consume();
                return;
            }

            if (isEdit) {
                existing.setName(txtName.getText().trim());
                existing.setGameTitle(cbGame.getValue());
                existing.setOrganizer(txtOrganizer.getText().trim());
                existing.setFormat(cbFormat.getValue());
                existing.setStatus(cbStatus.getValue());
                existing.setMaxTeams(spMaxTeams.getValue());
                existing.setPrizePool(prize);
                existing.setStartDate(dpStart.getValue());
                existing.setEndDate(dpEnd.getValue());
                existing.setLocation(txtLocation.getText().trim());
                existing.setDescription(txtDesc.getText().trim());

                ctx.getTournamentService().update(existing);
                ToastManager.showSuccess(String.format(I18n.get("tournament.toast.updated"), existing.getName()));
            } else {
                Tournament newTournament = new Tournament();
                newTournament.setName(txtName.getText().trim());
                newTournament.setGameTitle(cbGame.getValue());
                newTournament.setOrganizer(txtOrganizer.getText().trim());
                newTournament.setFormat(cbFormat.getValue() != null ? cbFormat.getValue() : BracketFormat.SINGLE_ELIMINATION);
                newTournament.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : TournamentStatus.UPCOMING);
                newTournament.setMaxTeams(spMaxTeams.getValue());
                newTournament.setPrizePool(prize);
                newTournament.setStartDate(dpStart.getValue());
                newTournament.setEndDate(dpEnd.getValue());
                newTournament.setLocation(txtLocation.getText().trim());
                newTournament.setDescription(txtDesc.getText().trim());

                ctx.getTournamentService().create(newTournament);
                ToastManager.showSuccess(String.format(I18n.get("tournament.toast.created"), newTournament.getName()));
            }

            tournamentTable.refresh();
            updateCountLabel();
        });

        dialog.showAndWait();
    }
}

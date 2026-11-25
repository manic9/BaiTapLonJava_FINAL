package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Match;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.I18n;
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
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Object> cbTournamentFilter;
    @FXML private ComboBox<Object> cbStatusFilter;
    @FXML private Label lblResultCount;
    @FXML private TableView<Match> matchTable;

    @FXML private TableColumn<Match, String> colId;
    @FXML private TableColumn<Match, String> colTournament;
    @FXML private TableColumn<Match, String> colRound;
    @FXML private TableColumn<Match, String> colTeamA;
    @FXML private TableColumn<Match, String> colScore;
    @FXML private TableColumn<Match, String> colTeamB;
    @FXML private TableColumn<Match, String> colBestOf;
    @FXML private TableColumn<Match, String> colTime;
    @FXML private TableColumn<Match, MatchStatus> colStatus;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Match> filteredData;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        setupRowContextMenu();
        bindData();
    }

    private void setupFilters() {
        List<Object> trnOptions = new ArrayList<>();
        trnOptions.add(com.esports.manager.util.I18n.get("filter.all_tournaments"));
        trnOptions.addAll(ctx.getTournamentService().getAll());
        cbTournamentFilter.setItems(FXCollections.observableArrayList(trnOptions));
        cbTournamentFilter.getSelectionModel().selectFirst();

        cbStatusFilter.setItems(FXCollections.observableArrayList(
                com.esports.manager.util.I18n.get("filter.all_statuses"),
                MatchStatus.SCHEDULED,
                MatchStatus.LIVE,
                MatchStatus.COMPLETED
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
        colRound.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoundName()));

        colTeamA.setCellValueFactory(d -> new SimpleStringProperty(ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamAId())));
        colTeamA.setCellFactory(col -> createTeamCell(true));

        colScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getScoreA() + "  :  " + d.getValue().getScoreB()));
        colScore.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-color: #0B0E14; -fx-text-fill: #00D4FF; -fx-font-weight: bold; " +
                            "-fx-padding: 4 10; -fx-background-radius: 7px; -fx-border-color: #1E293B; -fx-border-radius: 7px;");
                    setGraphic(badge);
                }
            }
        });

        colTeamB.setCellValueFactory(d -> new SimpleStringProperty(ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamBId())));
        colTeamB.setCellFactory(col -> createTeamCell(false));

        colBestOf.setCellValueFactory(d -> new SimpleStringProperty("BO" + d.getValue().getBestOf()));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getScheduledTime() != null ? d.getValue().getScheduledTime().format(TIME_FMT) : "-"
        ));

        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(MatchStatus st, boolean empty) {
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

    }

    private void setupRowContextMenu() {
        MenuItem miScore = new MenuItem(I18n.get("match.dialog.title"));
        MenuItem miEdit = new MenuItem("Sửa thông tin trận đấu");
        MenuItem miDelete = new MenuItem("Xóa trận đấu");

        ContextMenu rowMenu = new ContextMenu(miScore, new SeparatorMenuItem(), miEdit, miDelete);
        rowMenu.getStyleClass().add("context-menu-dark");

        miScore.setOnAction(e -> {
            Match m = matchTable.getSelectionModel().getSelectedItem();
            if (m != null) showScoreDialog(m, () -> matchTable.refresh());
        });

        miEdit.setOnAction(e -> {
            Match m = matchTable.getSelectionModel().getSelectedItem();
            if (m != null) showMatchFormDialog(m);
        });

        miDelete.setOnAction(e -> {
            Match m = matchTable.getSelectionModel().getSelectedItem();
            if (m != null) handleDeleteMatch(m);
        });

        matchTable.setRowFactory(tv -> {
            TableRow<Match> row = new TableRow<>();
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

        matchTable.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                rowMenu.hide();
            }
        });
    }

    private TableCell<Match, String> createTeamCell(boolean isTeamA) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Match m = getTableView().getItems().get(getIndex());
                    String targetId = isTeamA ? m.getTeamAId() : m.getTeamBId();
                    boolean isWinner = m.getWinnerId() != null && m.getWinnerId().equals(targetId);
                    if (isWinner) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #10B981;");
                    } else if ("TBD".equals(item)) {
                        setStyle("-fx-text-fill: #64748B; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-font-weight: 600; -fx-text-fill: #F8FAFC;");
                    }
                }
            }
        };
    }

    private void bindData() {
        filteredData = new FilteredList<>(ctx.getMatchService().getAll(), p -> true);
        SortedList<Match> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(matchTable.comparatorProperty());
        matchTable.setItems(sortedData);
        updateCountLabel();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        Object selectedTrn = cbTournamentFilter.getValue();
        Object selectedStatus = cbStatusFilter.getValue();

        filteredData.setPredicate(m -> {
            String teamA = ctx.getTeamService().getTeamNameOrTbd(m.getTeamAId()).toLowerCase();
            String teamB = ctx.getTeamService().getTeamNameOrTbd(m.getTeamBId()).toLowerCase();

            boolean matchKeyword = keyword.isEmpty()
                    || m.getId().toLowerCase().contains(keyword)
                    || m.getRoundName().toLowerCase().contains(keyword)
                    || teamA.contains(keyword)
                    || teamB.contains(keyword);

            boolean matchTrn = !(selectedTrn instanceof Tournament t)
                    || m.getTournamentId().equals(t.getId());

            boolean matchStatus = !(selectedStatus instanceof MatchStatus st)
                    || m.getStatus() == st;

            return matchKeyword && matchTrn && matchStatus;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText(com.esports.manager.util.I18n.get("match.count_format", filteredData.size()));
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cbTournamentFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleCreateMatch() {
        showMatchFormDialog(null);
    }

    private void handleDeleteMatch(Match m) {
        if (!ctx.getSettings().isConfirmBeforeDelete() || DialogHelper.confirmDelete(
                "Xóa trận đấu",
                "Bạn có chắc chắn muốn xóa trận đấu " + m.getRoundName() + " (" + m.getId() + ")?")) {
            try {
                if (ctx.getMatchService().delete(m.getId())) {
                    updateCountLabel();
                    ToastManager.showSuccess("Đã xóa trận đấu: " + m.getId());
                } else {
                    ToastManager.showError("Không tìm thấy trận đấu cần xóa.");
                }
            } catch (RuntimeException ex) {
                ToastManager.showOperationError("Xóa trận đấu không thành công", ex);
            }
        }
    }

    /**
     * Hộp thoại cập nhật tỉ số trực tiếp (dùng chung cho Match Management & Tournament Bracket)
     */
    public static void showScoreDialog(Match match, Runnable onUpdated) {
        AppContext ctx = AppContext.getInstance();
        String nameA = ctx.getTeamService().getTeamNameOrTbd(match.getTeamAId());
        String nameB = ctx.getTeamService().getTeamNameOrTbd(match.getTeamBId());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(com.esports.manager.util.I18n.get("match.dialog.title"));
        dialog.setHeaderText(match.getRoundName() + " • BO" + match.getBestOf());
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(com.esports.manager.util.I18n.get("btn.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        VBox content = new VBox(16);
        content.setPadding(new Insets(18));

        // Bảng điều khiển tỉ số 2 đội
        HBox scoreBoard = new HBox(18);
        scoreBoard.setAlignment(Pos.CENTER);
        scoreBoard.getStyleClass().add("card-elevated");

        Spinner<Integer> spScoreA = new Spinner<>(0, 10, match.getScoreA());
        spScoreA.setPrefWidth(85);
        spScoreA.setEditable(true);

        Spinner<Integer> spScoreB = new Spinner<>(0, 10, match.getScoreB());
        spScoreB.setPrefWidth(85);
        spScoreB.setEditable(true);

        VBox boxA = new VBox(6, new Label(nameA), spScoreA);
        boxA.setAlignment(Pos.CENTER);
        boxA.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #F8FAFC;");

        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #7C5CFC;");

        VBox boxB = new VBox(6, new Label(nameB), spScoreB);
        boxB.setAlignment(Pos.CENTER);
        boxB.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #F8FAFC;");

        scoreBoard.getChildren().addAll(boxA, vsLabel, boxB);

        // Trạng thái trận đấu
        HBox statusRow = new HBox(12);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSt = new Label(com.esports.manager.util.I18n.get("match.dialog.status") + ":");
        ComboBox<MatchStatus> cbStatus = new ComboBox<>(FXCollections.observableArrayList(MatchStatus.values()));
        cbStatus.setValue(match.getStatus());
        cbStatus.setPrefWidth(200);
        statusRow.getChildren().addAll(lblSt, cbStatus);

        Label hint = new Label(com.esports.manager.util.I18n.get("match.dialog.hint"));
        hint.setWrapText(true);
        hint.setMaxWidth(400);
        hint.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11.5px;");

        content.getChildren().addAll(scoreBoard, statusRow, hint);
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        okBtn.getStyleClass().add("btn-primary");
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            int sA = spScoreA.getValue();
            int sB = spScoreB.getValue();
            if (cbStatus.getValue() == MatchStatus.COMPLETED && sA == sB) {
                ToastManager.showError(String.format(com.esports.manager.util.I18n.get("match.dialog.draw_error"), sA, sB));
                ev.consume();
            }
            if (cbStatus.getValue() == MatchStatus.COMPLETED && (match.getTeamAId() == null || match.getTeamBId() == null)) {
                ToastManager.showError(com.esports.manager.util.I18n.get("match.dialog.incomplete_teams"));
                ev.consume();
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSave) {
            try {
                ctx.getMatchService().updateMatchResult(match.getId(), spScoreA.getValue(), spScoreB.getValue(), cbStatus.getValue());
                ToastManager.showSuccess(String.format(com.esports.manager.util.I18n.get("match.dialog.updated"), match.getId()));
                if (onUpdated != null) onUpdated.run();
            } catch (RuntimeException ex) {
                ToastManager.showOperationError("Cập nhật kết quả trận đấu không thành công", ex);
            }
        }
    }

    private void showMatchFormDialog(Match existing) {
        boolean isEdit = existing != null;
        Dialog<Match> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? com.esports.manager.util.I18n.get("match.dialog.edit_title") : com.esports.manager.util.I18n.get("match.dialog.create_title"));
        dialog.setHeaderText(isEdit ? com.esports.manager.util.I18n.get("match.dialog.edit_header") + ": " + existing.getId() : com.esports.manager.util.I18n.get("match.dialog.create_header"));
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(com.esports.manager.util.I18n.get("match.dialog.save_btn"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        ComboBox<Tournament> cbTrn = new ComboBox<>(ctx.getTournamentService().getAll());
        cbTrn.setPrefWidth(280);
        if (isEdit) {
            ctx.getTournamentService().findById(existing.getTournamentId()).ifPresent(cbTrn::setValue);
        } else if (!cbTrn.getItems().isEmpty()) {
            cbTrn.getSelectionModel().selectFirst();
        }

        Spinner<Integer> spRound = new Spinner<>(1, 6, isEdit ? existing.getRound() : 1);
        TextField txtRoundName = new TextField(isEdit ? existing.getRoundName() : "Vòng bảng - Trận 1");

        ComboBox<Team> cbTeamA = new ComboBox<>(ctx.getTeamService().getAll());
        ComboBox<Team> cbTeamB = new ComboBox<>(ctx.getTeamService().getAll());
        cbTeamA.setPrefWidth(280);
        cbTeamB.setPrefWidth(280);

        if (isEdit) {
            if (existing.getTeamAId() != null) ctx.getTeamService().findById(existing.getTeamAId()).ifPresent(cbTeamA::setValue);
            if (existing.getTeamBId() != null) ctx.getTeamService().findById(existing.getTeamBId()).ifPresent(cbTeamB::setValue);
        } else if (cbTeamA.getItems().size() >= 2) {
            cbTeamA.getSelectionModel().select(0);
            cbTeamB.getSelectionModel().select(1);
        }

        ComboBox<Integer> cbBestOf = new ComboBox<>(FXCollections.observableArrayList(1, 3, 5));
        cbBestOf.setValue(isEdit ? existing.getBestOf() : ctx.getSettings().getDefaultBestOf());

        DatePicker dpDate = new DatePicker(isEdit && existing.getScheduledTime() != null
                ? existing.getScheduledTime().toLocalDate() : LocalDate.now().plusDays(1));
        TextField txtHour = new TextField(isEdit && existing.getScheduledTime() != null
                ? existing.getScheduledTime().toLocalTime().toString() : "19:00");

        grid.add(new Label("Giải đấu *:"), 0, 0);
        grid.add(cbTrn, 1, 0);
        grid.add(new Label("Vòng số:"), 0, 1);
        grid.add(spRound, 1, 1);
        grid.add(new Label("Tên vòng đấu *:"), 0, 2);
        grid.add(txtRoundName, 1, 2);
        grid.add(new Label(com.esports.manager.util.I18n.get("match.dialog.team_a") + ":"), 0, 3);
        grid.add(cbTeamA, 1, 3);
        grid.add(new Label(com.esports.manager.util.I18n.get("match.dialog.team_b") + ":"), 0, 4);
        grid.add(cbTeamB, 1, 4);
        grid.add(new Label("Thể thức (BO):"), 0, 5);
        grid.add(cbBestOf, 1, 5);
        grid.add(new Label("Ngày thi đấu:"), 0, 6);
        grid.add(dpDate, 1, 6);
        grid.add(new Label("Giờ (HH:mm):"), 0, 7);
        grid.add(txtHour, 1, 7);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cbTrn.getValue() == null || txtRoundName.getText().trim().isEmpty()) {
                ToastManager.showError("Vui lòng chọn giải đấu và nhập tên vòng đấu!");
                ev.consume();
                return;
            }
            if (cbTeamA.getValue() != null && cbTeamB.getValue() != null
                    && cbTeamA.getValue().getId().equals(cbTeamB.getValue().getId())) {
                ToastManager.showError(com.esports.manager.util.I18n.get("match.dialog.same_team_error"));
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                Match target = isEdit ? existing : new Match();
                target.setTournamentId(cbTrn.getValue().getId());
                target.setRound(spRound.getValue());
                target.setRoundName(txtRoundName.getText().trim());
                target.setTeamAId(cbTeamA.getValue() != null ? cbTeamA.getValue().getId() : null);
                target.setTeamBId(cbTeamB.getValue() != null ? cbTeamB.getValue().getId() : null);
                target.setBestOf(cbBestOf.getValue());

                LocalTime time = LocalTime.of(19, 0);
                try {
                    time = LocalTime.parse(txtHour.getText().trim());
                } catch (Exception ignored) {}
                LocalDate date = dpDate.getValue() != null ? dpDate.getValue() : LocalDate.now();
                target.setScheduledTime(LocalDateTime.of(date, time));
                return target;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(m -> {
            try {
                ctx.getMatchService().save(m);
                matchTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess(isEdit ? com.esports.manager.util.I18n.get("match.dialog.updated_msg") : com.esports.manager.util.I18n.get("match.dialog.created_msg"));
            } catch (RuntimeException ex) {
                ToastManager.showOperationError("Lưu trận đấu không thành công", ex);
            }
        });
    }
}

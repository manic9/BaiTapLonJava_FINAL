package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.BracketFormat;
import com.esports.manager.model.enums.TournamentStatus;
import com.esports.manager.util.DialogHelper;
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
    @FXML private TableColumn<Tournament, Void> colActions;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<Tournament> filteredData;
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        bindData();
    }

    private void setupFilters() {
        // Thu thập danh sách tựa game từ dữ liệu mẫu
        Set<String> games = new LinkedHashSet<>(List.of("Tất cả tựa game", "League of Legends", "VALORANT", "CS:GO", "Dota 2", "FC Online"));
        for (Tournament t : ctx.getTournamentService().getAll()) {
            if (t.getGameTitle() != null && !t.getGameTitle().isBlank()) {
                games.add(t.getGameTitle());
            }
        }
        cbGameFilter.setItems(FXCollections.observableArrayList(games));
        cbGameFilter.getSelectionModel().selectFirst();

        List<String> statuses = new ArrayList<>();
        statuses.add("Tất cả trạng thái");
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

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetail = new Button("Chi tiết");
            private final Button btnBracket = new Button("Nhánh đấu");
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(6, btnDetail, btnBracket, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnDetail.getStyleClass().add("btn-primary");
                btnDetail.setStyle("-fx-padding: 4 9; -fx-font-size: 11px;");
                btnDetail.setTooltip(new Tooltip("Xem thông tin chi tiết giải đấu"));

                btnBracket.getStyleClass().add("btn-secondary");
                btnBracket.setStyle("-fx-padding: 4 9; -fx-font-size: 11px;");
                btnBracket.setTooltip(new Tooltip("Xem sơ đồ nhánh đấu"));

                btnEdit.setStyle("-fx-text-fill: #00D4FF; -fx-font-size: 12px;");
                btnEdit.getStyleClass().add("btn-icon");
                btnEdit.setTooltip(new Tooltip("Chỉnh sửa giải đấu"));

                btnDelete.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px;");
                btnDelete.getStyleClass().add("btn-icon");
                btnDelete.setTooltip(new Tooltip("Xóa giải đấu"));

                btnDetail.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    ViewNavigator.<TournamentDetailController>navigate(
                            "TournamentDetailView.fxml", "Tournament Detail",
                            "Thông tin vận hành chi tiết giải đấu",
                            ctrl -> ctrl.setTournament(t)
                    );
                });

                btnBracket.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    ViewNavigator.<BracketController>navigate(
                            "BracketView.fxml", "Tournament Bracket",
                            "Sơ đồ nhánh đấu — " + t.getName(),
                            ctrl -> ctrl.selectTournament(t.getId())
                    );
                });

                btnEdit.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    showTournamentFormDialog(t);
                });

                btnDelete.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    handleDeleteTournament(t);
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
                    || "Tất cả tựa game".equals(selectedGame)
                    || (t.getGameTitle() != null && t.getGameTitle().equalsIgnoreCase(selectedGame));

            boolean matchStatus = selectedStatus == null
                    || "Tất cả trạng thái".equals(selectedStatus)
                    || (t.getStatus() != null && t.getStatus().getDisplayName().equalsIgnoreCase(selectedStatus));

            return matchKeyword && matchGame && matchStatus;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        lblResultCount.setText(filteredData.size() + " giải đấu");
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
                "Xóa giải đấu",
                "Bạn có chắc chắn muốn xóa giải đấu \"" + t.getName() + "\" (#" + t.getId() + ")?")) {
            if (ctx.getTournamentService().delete(t.getId())) {
                tournamentTable.refresh();
                updateCountLabel();
                ToastManager.showSuccess("Đã xóa giải đấu: " + t.getName());
            } else {
                ToastManager.showError("Không thể xóa giải đấu này.");
            }
        }
    }

    private void showTournamentFormDialog(Tournament existing) {
        boolean isEdit = existing != null;
        Dialog<Tournament> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chỉnh sửa giải đấu" : "Tạo giải đấu mới");
        dialog.setHeaderText(isEdit ? "Cập nhật giải đấu: " + existing.getName() : "Khởi tạo giải đấu Esports mới");
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ButtonType btnSave = new ButtonType(isEdit ? "Lưu thay đổi" : "Tạo giải đấu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(18));

        TextField txtName = new TextField(isEdit ? existing.getName() : "");
        txtName.setPromptText("VD: Vietnam Championship Series 2026");
        txtName.setPrefWidth(300);

        ComboBox<String> cbGame = new ComboBox<>(FXCollections.observableArrayList(
                "League of Legends", "VALORANT", "CS:GO", "Dota 2", "FC Online", "PUBG"
        ));
        cbGame.setEditable(true);
        cbGame.setValue(isEdit ? existing.getGameTitle() : "League of Legends");
        cbGame.setMaxWidth(Double.MAX_VALUE);

        TextField txtOrganizer = new TextField(isEdit ? existing.getOrganizer() : "");
        txtOrganizer.setPromptText("VD: Riot Games / VNG Games");

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

        TextField txtLocation = new TextField(isEdit ? existing.getLocation() : "TP. Hồ Chí Minh");
        txtLocation.setPromptText("Địa điểm tổ chức");

        TextArea txtDesc = new TextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        txtDesc.setPromptText("Mô tả giải đấu, quy định và thể thức chi tiết...");
        txtDesc.setPrefRowCount(3);

        grid.add(new Label("Tên giải đấu *:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Tựa game *:"), 0, 1);
        grid.add(cbGame, 1, 1);
        grid.add(new Label("Đơn vị tổ chức *:"), 0, 2);
        grid.add(txtOrganizer, 1, 2);
        grid.add(new Label("Thể thức thi đấu:"), 0, 3);
        grid.add(cbFormat, 1, 3);
        grid.add(new Label("Trạng thái:"), 0, 4);
        grid.add(cbStatus, 1, 4);
        grid.add(new Label("Số đội tham gia:"), 0, 5);
        grid.add(spMaxTeams, 1, 5);
        grid.add(new Label("Quỹ thưởng ($):"), 0, 6);
        grid.add(txtPrize, 1, 6);
        grid.add(new Label("Ngày bắt đầu:"), 0, 7);
        grid.add(dpStart, 1, 7);
        grid.add(new Label("Ngày kết thúc:"), 0, 8);
        grid.add(dpEnd, 1, 8);
        grid.add(new Label("Địa điểm:"), 0, 9);
        grid.add(txtLocation, 1, 9);
        grid.add(new Label("Mô tả:"), 0, 10);
        grid.add(txtDesc, 1, 10);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (txtName.getText().trim().isEmpty() || txtOrganizer.getText().trim().isEmpty()) {
                ToastManager.showError("Vui lòng điền đầy đủ Tên giải đấu và Đơn vị tổ chức!");
                ev.consume();
                return;
            }
            double prize;
            try {
                prize = Double.parseDouble(txtPrize.getText().trim().replace(",", ""));
            } catch (NumberFormatException e) {
                ToastManager.showError("Quỹ giải thưởng không hợp lệ!");
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
                ToastManager.showSuccess("Đã cập nhật thông tin giải đấu: " + existing.getName());
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
                ToastManager.showSuccess("Đã tạo giải đấu mới: " + newTournament.getName());
            }

            tournamentTable.refresh();
            updateCountLabel();
        });

        dialog.showAndWait();
    }
}

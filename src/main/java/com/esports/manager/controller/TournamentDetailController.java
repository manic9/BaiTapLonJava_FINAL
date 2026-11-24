package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Match;
import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.model.Registration;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.util.I18n;
import com.esports.manager.util.DialogHelper;
import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TournamentDetailController {

    // TournamentDetailView.fxml (chi tiết giải đấu)
    @FXML private Label lblStatusBadge;
    @FXML private Label lblGameBadge;
    @FXML private Label lblTournamentId;
    @FXML private Label lblName;
    @FXML private Label lblDescription;
    @FXML private Label lblPrizePool;
    @FXML private Label lblTeamSlots;
    @FXML private Label lblFormatOrganizer;
    @FXML private Label lblDateLocation;

    // Language Toggle
    @FXML private StackPane langTogglePane;
    @FXML private ImageView ivLangFlag;

    // Tab 1: Registrations
    @FXML private TableView<Registration> regTable;
    @FXML private TableColumn<Registration, String> colRegSeed;
    @FXML private TableColumn<Registration, String> colRegTeam;
    @FXML private TableColumn<Registration, String> colRegRegion;
    @FXML private TableColumn<Registration, String> colRegNotes;
    @FXML private TableColumn<Registration, RegistrationStatus> colRegStatus;
    @FXML private TableColumn<Registration, Void> colRegAction;

    // Tab 2: Matches
    @FXML private TableView<Match> matchTable;
    @FXML private TableColumn<Match, String> colMatchRound;
    @FXML private TableColumn<Match, String> colMatchTeamA;
    @FXML private TableColumn<Match, String> colMatchScore;
    @FXML private TableColumn<Match, String> colMatchTeamB;
    @FXML private TableColumn<Match, String> colMatchBo;
    @FXML private TableColumn<Match, MatchStatus> colMatchStatus;

    // Tab 3: Prizes
    @FXML private TableView<PrizeAllocation> prizeTable;
    @FXML private TableColumn<PrizeAllocation, String> colPrizeRank;
    @FXML private TableColumn<PrizeAllocation, String> colPrizePercent;
    @FXML private TableColumn<PrizeAllocation, String> colPrizeAmount;
    @FXML private TableColumn<PrizeAllocation, String> colPrizeTeam;
    @FXML private TableColumn<PrizeAllocation, Boolean> colPrizePaid;

    private final AppContext ctx = AppContext.getInstance();
    private Tournament tournament;
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupRegistrationColumns();
        setupMatchColumns();
        setupPrizeColumns();
        updateLangButton();
        I18n.addListener(lang -> updateLangButton());
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
    public void handleToggleLanguage(MouseEvent event) {
        I18n.Language current = I18n.getCurrentLanguage();
        I18n.Language next = (current == I18n.Language.ENGLISH) ? I18n.Language.VIETNAMESE : I18n.Language.ENGLISH;
        I18n.setLanguage(next);
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        refreshAll();
    }

    private void refreshAll() {
        if (tournament == null) return;
        String symbol = ctx.getSettings().getCurrencySymbol();

        if (lblStatusBadge != null) {
            lblStatusBadge.setText(tournament.getStatus().getDisplayName());
            lblStatusBadge.getStyleClass().setAll("badge", tournament.getStatus().getBadgeStyle());
        }
        if (lblGameBadge != null) lblGameBadge.setText(tournament.getGameTitle());
        if (lblTournamentId != null) lblTournamentId.setText("#" + tournament.getId());
        if (lblName != null) lblName.setText(tournament.getName());
        if (lblDescription != null) {
            lblDescription.setText(tournament.getDescription() != null && !tournament.getDescription().isBlank()
                    ? tournament.getDescription() : "Chưa có mô tả chi tiết cho giải đấu này.");
        }

        if (lblPrizePool != null) lblPrizePool.setText(symbol + currencyFmt.format(tournament.getPrizePool()));

        int approvedCount = ctx.getRegistrationService().getApprovedByTournament(tournament.getId()).size();
        if (lblTeamSlots != null) lblTeamSlots.setText(approvedCount + " / " + tournament.getMaxTeams() + " Đội");

        if (lblFormatOrganizer != null) {
            lblFormatOrganizer.setText(tournament.getFormat().name().replace("_", " ") + " • " + tournament.getOrganizer());
        }

        String start = tournament.getStartDate() != null ? tournament.getStartDate().format(dateFmt) : "?";
        String end = tournament.getEndDate() != null ? tournament.getEndDate().format(dateFmt) : "?";
        if (lblDateLocation != null) lblDateLocation.setText(start + " - " + end + " • " + tournament.getLocation());

        // Nạp dữ liệu 3 Tab
        if (regTable != null) {
            List<Registration> regs = ctx.getRegistrationService().getAll().stream()
                    .filter(r -> r.getTournamentId().equals(tournament.getId()))
                    .toList();
            regTable.setItems(FXCollections.observableArrayList(regs));
        }

        if (matchTable != null) {
            List<Match> matches = ctx.getMatchService().getByTournament(tournament.getId());
            matchTable.setItems(FXCollections.observableArrayList(matches));
        }

        if (prizeTable != null) {
            List<PrizeAllocation> prizes = ctx.getPrizeService().getByTournament(tournament.getId());
            prizeTable.setItems(FXCollections.observableArrayList(prizes));
        }
    }

    private void setupRegistrationColumns() {
        if (colRegSeed == null) return;
        colRegSeed.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSeedNumber() > 0 ? "#" + d.getValue().getSeedNumber() : "-"
        ));
        colRegTeam.setCellValueFactory(d -> {
            String teamId = d.getValue().getTeamId();
            return new SimpleStringProperty(ctx.getTeamService().findById(teamId).map(Team::toString).orElse(teamId));
        });
        colRegRegion.setCellValueFactory(d -> {
            String teamId = d.getValue().getTeamId();
            return new SimpleStringProperty(ctx.getTeamService().findById(teamId).map(Team::getRegion).orElse("-"));
        });
        colRegNotes.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));

        colRegStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        colRegStatus.setCellFactory(col -> new TableCell<>() {
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

        colRegAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnApprove = new Button("Duyệt đội");
            {
                btnApprove.getStyleClass().add("btn-primary");
                btnApprove.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                btnApprove.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    try {
                        ctx.getRegistrationService().updateStatus(reg.getId(), RegistrationStatus.APPROVED);
                        ToastManager.showSuccess("Đã duyệt đội tham gia giải đấu!");
                        refreshAll();
                    } catch (IllegalStateException ex) {
                        ToastManager.showError(ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Registration reg = getTableView().getItems().get(getIndex());
                    setGraphic(reg.getStatus() == RegistrationStatus.PENDING ? btnApprove : null);
                }
            }
        });
    }

    private void setupMatchColumns() {
        if (colMatchRound == null) return;
        colMatchRound.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoundName()));
        colMatchTeamA.setCellValueFactory(d -> new SimpleStringProperty(ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamAId())));
        colMatchScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getScoreA() + "  -  " + d.getValue().getScoreB()));
        colMatchTeamB.setCellValueFactory(d -> new SimpleStringProperty(ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamBId())));
        colMatchBo.setCellValueFactory(d -> new SimpleStringProperty("BO" + d.getValue().getBestOf()));

        colMatchStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        colMatchStatus.setCellFactory(col -> new TableCell<>() {
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

    private void setupPrizeColumns() {
        if (colPrizeRank == null) return;
        colPrizeRank.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPlacement()));
        colPrizePercent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPercentage() + "%"));
        colPrizeAmount.setCellValueFactory(d -> {
            String symbol = ctx.getSettings().getCurrencySymbol();
            return new SimpleStringProperty(symbol + currencyFmt.format(d.getValue().getAmount()));
        });
        colPrizeTeam.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTeamId() != null ? ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamId()) : "Chưa xác định"
        ));
        colPrizePaid.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().isPaid()));
        colPrizePaid.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean paid, boolean empty) {
                super.updateItem(paid, empty);
                if (empty || paid == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(paid ? "Đã giải ngân" : "Chờ trao giải");
                    badge.getStyleClass().addAll("badge", paid ? "badge-completed" : "badge-upcoming");
                    setGraphic(badge);
                }
            }
        });
    }

    @FXML
    public void handleBack() {
        ViewNavigator.navigate("TournamentView.fxml", I18n.get("page.tournaments.title"));
    }

    @FXML
    public void handleGenerateBracket() {
        if (tournament == null) return;
        try {
            ctx.getBracketService().generateSingleEliminationBracket(tournament.getId());
            refreshAll();
            ToastManager.showSuccess("Đã khởi tạo sơ đồ nhánh đấu tự động cho " + tournament.getName());
        } catch (IllegalStateException ex) {
            ToastManager.showError(ex.getMessage());
        }
    }

    @FXML
    public void handleOpenBracket() {
        if (tournament == null) return;
        ViewNavigator.<BracketController>navigate(
                "BracketView.fxml", I18n.get("page.bracket.title"),
                ctrl -> ctrl.selectTournament(tournament.getId())
        );
    }

    @FXML
    public void handleAddTeam() {
        if (tournament == null) return;

        List<String> alreadyRegistered = ctx.getRegistrationService().getAll().stream()
                .filter(r -> r.getTournamentId().equals(tournament.getId())
                        && r.getStatus() != RegistrationStatus.REJECTED)
                .map(Registration::getTeamId)
                .toList();
        List<Team> availableTeams = ctx.getTeamService().getAll().stream()
                .filter(team -> !alreadyRegistered.contains(team.getId()))
                .filter(team -> team.getGameTitle() != null && tournament.getGameTitle() != null
                        && team.getGameTitle().trim().equalsIgnoreCase(tournament.getGameTitle().trim()))
                .toList();
        if (availableTeams.isEmpty()) {
            ToastManager.showInfo("Không còn đội chưa đăng ký thuộc game " + tournament.getGameTitle() + ".");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm đội tuyển");
        dialog.setHeaderText("Đăng ký đội tham gia " + tournament.getName());
        dialog.getDialogPane().getButtonTypes().setAll(
                new ButtonType("Thêm đội", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE));
        DialogHelper.applyDarkTheme(dialog.getDialogPane());

        ComboBox<Team> cbTeam = new ComboBox<>(FXCollections.observableArrayList(availableTeams));
        cbTeam.setPromptText("Chọn đội tuyển");
        cbTeam.setMaxWidth(Double.MAX_VALUE);
        TextArea txtNotes = new TextArea();
        txtNotes.setPromptText("Ghi chú (không bắt buộc)");
        txtNotes.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(8, 4, 4, 4));
        form.add(new Label("Đội tuyển"), 0, 0);
        form.add(cbTeam, 1, 0);
        form.add(new Label("Ghi chú"), 0, 1);
        form.add(txtNotes, 1, 1);
        GridPane.setHgrow(cbTeam, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(txtNotes, javafx.scene.layout.Priority.ALWAYS);
        dialog.getDialogPane().setContent(form);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0));
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cbTeam.getValue() == null) {
                ToastManager.showError("Hãy chọn đội tuyển cần thêm.");
                event.consume();
            }
        });

        dialog.showAndWait().filter(button -> button.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .ifPresent(button -> {
                    Registration registration = new Registration();
                    registration.setTournamentId(tournament.getId());
                    registration.setTeamId(cbTeam.getValue().getId());
                    registration.setNotes(txtNotes.getText().trim());
                    try {
                        ctx.getRegistrationService().register(registration);
                        refreshAll();
                        ToastManager.showSuccess("Đã thêm đội vào danh sách chờ duyệt.");
                    } catch (IllegalStateException ex) {
                        ToastManager.showError(ex.getMessage());
                    }
                });
    }
}

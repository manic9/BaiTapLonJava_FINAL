package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Match;
import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.model.Registration;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        ViewNavigator.navigate("TournamentView.fxml", "Tournament Management",
                "Tạo mới, chỉnh sửa, lọc và vận hành các giải đấu Esports");
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
                "BracketView.fxml", "Tournament Bracket",
                "Sơ đồ nhánh đấu — " + tournament.getName(),
                ctrl -> ctrl.selectTournament(tournament.getId())
        );
    }
}

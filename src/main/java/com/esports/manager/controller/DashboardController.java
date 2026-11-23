package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.ActivityLog;
import com.esports.manager.model.Match;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.model.enums.TournamentStatus;
import com.esports.manager.util.I18n;
import com.esports.manager.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblTotalTournaments;
    @FXML private Label lblOngoingTournaments;
    @FXML private Label lblTotalTeams;
    @FXML private Label lblTotalPlayers;
    @FXML private Label lblTotalMatches;
    @FXML private Label lblLiveMatches;
    @FXML private Label lblTotalPrizePool;
    @FXML private Label lblPendingRegs;

    @FXML private VBox featuredTournamentsBox;
    @FXML private VBox spotlightMatchesBox;
    @FXML private VBox activityFeedBox;

    private final AppContext ctx = AppContext.getInstance();
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    public void refreshDashboard() {
        updateKpis();
        renderFeaturedTournaments();
        renderSpotlightMatches();
        renderActivityFeed();
    }

    private void updateKpis() {
        var tournaments = ctx.getTournamentService().getAll();
        long ongoingCount = tournaments.stream().filter(t -> t.getStatus() == TournamentStatus.ONGOING).count();
        double totalPool = tournaments.stream().mapToDouble(Tournament::getPrizePool).sum();

        int teamCount = ctx.getTeamService().getAll().size();
        int playerCount = ctx.getPlayerService().getAll().size();

        var matches = ctx.getMatchService().getAll();
        long liveCount = matches.stream().filter(m -> m.getStatus() == MatchStatus.LIVE).count();

        long pendingRegs = ctx.getRegistrationService().getAll().stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDING).count();

        String symbol = ctx.getSettings().getCurrencySymbol();

        lblTotalTournaments.setText(String.valueOf(tournaments.size()));
        lblOngoingTournaments.setText(com.esports.manager.util.I18n.get("dashboard.kpi.ongoing_format", ongoingCount));

        lblTotalTeams.setText(String.valueOf(teamCount));
        lblTotalPlayers.setText(com.esports.manager.util.I18n.get("dashboard.kpi.players_format", playerCount));

        lblTotalMatches.setText(String.valueOf(matches.size()));
        lblLiveMatches.setText(com.esports.manager.util.I18n.get("dashboard.kpi.live_format", liveCount));

        lblTotalPrizePool.setText(symbol + currencyFmt.format(totalPool));
        lblPendingRegs.setText(com.esports.manager.util.I18n.get("dashboard.kpi.pending_format", pendingRegs));
    }

    private void renderFeaturedTournaments() {
        featuredTournamentsBox.getChildren().clear();
        List<Tournament> featured = ctx.getTournamentService().getAll().stream()
                .sorted(Comparator.comparingInt(t -> t.getStatus() == TournamentStatus.ONGOING ? 0 : 1))
                .limit(4)
                .toList();

        if (featured.isEmpty()) {
            featuredTournamentsBox.getChildren().add(new Label("Chưa có giải đấu nào trong hệ thống."));
            return;
        }

        String symbol = ctx.getSettings().getCurrencySymbol();
        for (Tournament t : featured) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("card-elevated");

            VBox info = new VBox(4);
            HBox titleLine = new HBox(8);
            titleLine.setAlignment(Pos.CENTER_LEFT);

            Label nameLbl = new Label(t.getName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #F8FAFC;");

            Label statusBadge = new Label(t.getStatus().getDisplayName());
            statusBadge.getStyleClass().addAll("badge", t.getStatus().getBadgeStyle());

            titleLine.getChildren().addAll(nameLbl, statusBadge);

            int approvedCount = ctx.getRegistrationService().getApprovedByTournament(t.getId()).size();
            Label metaLbl = new Label(String.format("%s  •  %d/%d Đội  •  Quỹ thưởng: %s%s",
                    t.getGameTitle(), approvedCount, t.getMaxTeams(), symbol, currencyFmt.format(t.getPrizePool())));
            metaLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");

            info.getChildren().addAll(titleLine, metaLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnBracket = new Button(com.esports.manager.util.I18n.get("btn.bracket"));
            btnBracket.getStyleClass().add("btn-secondary");
            btnBracket.setOnAction(e -> ViewNavigator.<BracketController>navigate(
                    "BracketView.fxml", com.esports.manager.util.I18n.get("page.bracket.title"),
                    ctrl -> ctrl.selectTournament(t.getId())
            ));

            Button btnDetail = new Button(com.esports.manager.util.I18n.get("btn.details"));
            btnDetail.getStyleClass().add("btn-primary");
            btnDetail.setOnAction(e -> ViewNavigator.<TournamentDetailController>navigate(
                    "TournamentDetailView.fxml", t.getName(),
                    ctrl -> ctrl.setTournament(t)
            ));

            row.getChildren().addAll(info, spacer, btnBracket, btnDetail);
            featuredTournamentsBox.getChildren().add(row);
        }
    }

    private void renderSpotlightMatches() {
        spotlightMatchesBox.getChildren().clear();
        List<Match> matches = ctx.getMatchService().getAll().stream()
                .filter(m -> m.getTeamAId() != null || m.getTeamBId() != null)
                .sorted(Comparator.comparingInt(m -> m.getStatus() == MatchStatus.LIVE ? 0 : 1))
                .limit(4)
                .toList();

        if (matches.isEmpty()) {
            spotlightMatchesBox.getChildren().add(new Label("Chưa có trận đấu nào được lên lịch."));
            return;
        }

        for (Match m : matches) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("card-elevated");

            String trnName = ctx.getTournamentService().findById(m.getTournamentId())
                    .map(Tournament::getName).orElse(m.getTournamentId());

            VBox left = new VBox(3);
            Label roundLbl = new Label(trnName + " • " + m.getRoundName() + " (BO" + m.getBestOf() + ")");
            roundLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11.5px;");

            String teamA = ctx.getTeamService().getTeamNameOrTbd(m.getTeamAId());
            String teamB = ctx.getTeamService().getTeamNameOrTbd(m.getTeamBId());
            Label matchupLbl = new Label(teamA + "   vs   " + teamB);
            matchupLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #F8FAFC;");

            left.getChildren().addAll(roundLbl, matchupLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label scoreLbl = new Label(m.getScoreA() + " : " + m.getScoreB());
            scoreLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #00D4FF; -fx-padding: 4 12; -fx-background-color: #0B0E14; -fx-background-radius: 8px;");

            Label statusBadge = new Label(m.getStatus().getDisplayName());
            statusBadge.getStyleClass().addAll("badge", m.getStatus().getBadgeStyle());

            row.getChildren().addAll(left, spacer, scoreLbl, statusBadge);
            spotlightMatchesBox.getChildren().add(row);
        }
    }

    private void renderActivityFeed() {
        activityFeedBox.getChildren().clear();
        List<ActivityLog> logs = ctx.getActivityService().getAllLogs().stream().limit(7).toList();

        for (ActivityLog log : logs) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.TOP_LEFT);
            item.getStyleClass().add("card-elevated");

            Label icon = new Label(resolveCategoryIcon(log.getCategory()));
            icon.setStyle("-fx-text-fill: " + resolveCategoryColor(log.getCategory()) + "; -fx-font-size: 14px; -fx-font-weight: bold;");

            VBox body = new VBox(4);
            Label msg = new Label(log.getMessage());
            msg.setWrapText(true);
            msg.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 12.5px;");

            Label time = new Label(log.getTimestamp().format(timeFmt) + " • " + log.getActor());
            time.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

            body.getChildren().addAll(msg, time);
            item.getChildren().addAll(icon, body);
            activityFeedBox.getChildren().add(item);
        }
    }

    private String resolveCategoryIcon(String category) {
        return switch (category) {
            case "MATCH" -> "🎮";
            case "TOURNAMENT" -> "🏆";
            case "REGISTRATION" -> "📋";
            case "PRIZE" -> "💰";
            case "TEAM" -> "🛡";
            default -> "⚡";
        };
    }

    private String resolveCategoryColor(String category) {
        return switch (category) {
            case "MATCH" -> "#00D4FF";
            case "TOURNAMENT" -> "#7C5CFC";
            case "REGISTRATION" -> "#F59E0B";
            case "PRIZE" -> "#10B981";
            default -> "#94A3B8";
        };
    }

    @FXML
    public void handleViewAllTournaments() {
        ViewNavigator.navigate("TournamentView.fxml", I18n.get("page.tournaments.title"));
    }

    @FXML
    public void handleViewAllMatches() {
        ViewNavigator.navigate("MatchView.fxml", I18n.get("page.matches.title"));
    }
}
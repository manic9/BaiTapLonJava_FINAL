package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Match;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.util.ToastManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BracketController {

    @FXML private ComboBox<Tournament> cbTournament;
    @FXML private Pane bracketCanvas;
    @FXML private HBox championBanner;
    @FXML private Label lblChampionName;

    private final AppContext ctx = AppContext.getInstance();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Hằng số kích thước hình học đảm bảo cây nhánh đấu cân đối tuyệt đối
    private static final double CARD_WIDTH = 258.0;
    private static final double CARD_HEIGHT = 92.0;
    private static final double ROUND_GAP = 105.0;
    private static final double BASE_SLOT_HEIGHT = 122.0;
    private static final double PADDING_X = 34.0;
    private static final double PADDING_Y = 66.0;

    private record NodeBounds(double x, double y, double centerY) {}

    @FXML
    public void initialize() {
        var tournaments = ctx.getTournamentService().getAll();
        cbTournament.setItems(tournaments);

        cbTournament.valueProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                renderBracketForTournament(newT.getId());
            }
        });

        if (!tournaments.isEmpty()) {
            cbTournament.getSelectionModel().selectFirst();
        }
    }

    public void selectTournament(String tournamentId) {
        if (tournamentId == null) return;
        for (Tournament t : cbTournament.getItems()) {
            if (t.getId().equals(tournamentId)) {
                cbTournament.getSelectionModel().select(t);
                renderBracketForTournament(tournamentId);
                return;
            }
        }
    }

    @FXML
    public void handleGenerateBracket() {
        Tournament selected = cbTournament.getValue();
        if (selected == null) {
            ToastManager.showError("Vui lòng chọn một giải đấu trước!");
            return;
        }
        try {
            ctx.getBracketService().generateSingleEliminationBracket(selected.getId());
            renderBracketForTournament(selected.getId());
            ToastManager.showSuccess("Đã tạo sơ đồ nhánh đấu cho " + selected.getName());
        } catch (IllegalStateException ex) {
            ToastManager.showError(ex.getMessage());
        }
    }

    private void renderBracketForTournament(String tournamentId) {
        bracketCanvas.getChildren().clear();
        championBanner.setVisible(false);
        championBanner.setManaged(false);

        List<Match> matches = ctx.getMatchService().getByTournament(tournamentId);
        if (matches.isEmpty()) {
            renderEmptyState();
            return;
        }

        // Nhóm các trận đấu theo vòng (Round 1, Round 2, Round 3...)
        Map<Integer, List<Match>> byRound = matches.stream()
                .collect(Collectors.groupingBy(Match::getRound, TreeMap::new, Collectors.toList()));

        int totalRounds = byRound.size();
        int maxFirstRoundMatches = byRound.values().iterator().next().size();

        double canvasWidth = PADDING_X * 2 + totalRounds * CARD_WIDTH + Math.max(0, totalRounds - 1) * ROUND_GAP;
        double canvasHeight = PADDING_Y + maxFirstRoundMatches * BASE_SLOT_HEIGHT + 40;
        bracketCanvas.setPrefSize(Math.max(1040, canvasWidth), Math.max(560, canvasHeight));

        Map<String, NodeBounds> boundsMap = new HashMap<>();
        List<Region> matchCards = new ArrayList<>();

        // 1. Tính tọa độ & tạo Header + Card cho từng vòng
        for (Map.Entry<Integer, List<Match>> entry : byRound.entrySet()) {
            int round = entry.getKey();
            List<Match> roundMatches = entry.getValue();
            roundMatches.sort(Comparator.comparingInt(Match::getMatchNumber));

            double x = PADDING_X + (round - 1) * (CARD_WIDTH + ROUND_GAP);

            // Tiêu đề vòng đấu (Header pill)
            Label roundHeader = createRoundHeader(round, totalRounds, x);
            bracketCanvas.getChildren().add(roundHeader);

            double span = BASE_SLOT_HEIGHT * Math.pow(2, round - 1);

            for (int i = 0; i < roundMatches.size(); i++) {
                Match match = roundMatches.get(i);
                double centerY = PADDING_Y + (i + 0.5) * span;
                double y = centerY - (CARD_HEIGHT / 2.0);

                boundsMap.put(match.getId(), new NodeBounds(x, y, centerY));

                VBox card = createMatchCard(match, tournamentId);
                card.setLayoutX(x);
                card.setLayoutY(y);
                matchCards.add(card);

                // Kiểm tra nhà vô địch ở trận cuối cùng
                if (round == totalRounds && match.getStatus() == MatchStatus.COMPLETED && match.getWinnerId() != null) {
                    championBanner.setVisible(true);
                    championBanner.setManaged(true);
                    lblChampionName.setText(ctx.getTeamService().getTeamNameOrTbd(match.getWinnerId()).toUpperCase());
                }
            }
        }

        // 2. Vẽ các đường nối cong CubicCurve giữa trận hiện tại và nextMatchId
        for (Match match : matches) {
            if (match.getNextMatchId() == null) continue;
            NodeBounds from = boundsMap.get(match.getId());
            NodeBounds to = boundsMap.get(match.getNextMatchId());
            if (from == null || to == null) continue;

            double startX = from.x() + CARD_WIDTH;
            double startY = from.centerY();
            double endX = to.x();
            double endY = to.centerY();

            CubicCurve curve = new CubicCurve();
            curve.setStartX(startX);
            curve.setStartY(startY);
            curve.setControlX1(startX + ROUND_GAP * 0.52);
            curve.setControlY1(startY);
            curve.setControlX2(endX - ROUND_GAP * 0.52);
            curve.setControlY2(endY);
            curve.setEndX(endX);
            curve.setEndY(endY);
            curve.setFill(Color.TRANSPARENT);

            boolean hasWinner = match.getStatus() == MatchStatus.COMPLETED && match.getWinnerId() != null;
            curve.setStroke(hasWinner ? Color.web("#00D4FF", 0.75) : Color.web("#293548"));
            curve.setStrokeWidth(hasWinner ? 2.2 : 1.6);

            bracketCanvas.getChildren().add(curve);
        }

        // 3. Thêm thẻ trận đấu lên trên lớp đường nối
        bracketCanvas.getChildren().addAll(matchCards);
    }

    private Label createRoundHeader(int round, int totalRounds, double x) {
        String title;
        if (round == totalRounds) {
            title = "CHUNG KẾT TỔNG";
        } else if (round == totalRounds - 1) {
            title = "BÁN KẾT";
        } else {
            title = "VÒNG " + round + " (TỨ KẾT)";
        }

        Label lbl = new Label(title);
        lbl.setPrefWidth(CARD_WIDTH);
        lbl.setAlignment(Pos.CENTER);
        lbl.setLayoutX(x);
        lbl.setLayoutY(18.0);
        lbl.setStyle("-fx-background-color: #182030; -fx-text-fill: #94A3B8; -fx-font-weight: bold; " +
                "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 8px; " +
                "-fx-border-color: #1E293B; -fx-border-radius: 8px;");
        return lbl;
    }

    private VBox createMatchCard(Match match, String tournamentId) {
        VBox card = new VBox(5);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setCursor(Cursor.HAND);

        String borderColor = switch (match.getStatus()) {
            case LIVE -> "#00D4FF";
            case COMPLETED -> "#7C5CFC";
            default -> "#1E293B";
        };

        card.setStyle("-fx-background-color: #121722; -fx-background-radius: 11px; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 11px; -fx-border-width: 1.3px; " +
                "-fx-padding: 8 11; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);");

        // Top Meta Line: Match Name + Status Pill
        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String timeText = match.getScheduledTime() != null ? " • " + match.getScheduledTime().format(timeFmt) : "";
        Label lblMeta = new Label(match.getRoundName() + " (BO" + match.getBestOf() + ")" + timeText);
        lblMeta.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = new Label(match.getStatus() == MatchStatus.LIVE ? "● LIVE" : match.getStatus().getDisplayName());
        String statusColor = switch (match.getStatus()) {
            case LIVE -> "#00D4FF";
            case COMPLETED -> "#10B981";
            default -> "#94A3B8";
        };
        lblStatus.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 9.5px; -fx-font-weight: bold;");

        topRow.getChildren().addAll(lblMeta, spacer, lblStatus);

        // Team A & Team B Rows
        boolean isAWinner = match.getWinnerId() != null && match.getWinnerId().equals(match.getTeamAId());
        boolean isBWinner = match.getWinnerId() != null && match.getWinnerId().equals(match.getTeamBId());

        HBox rowA = createTeamRow(match.getTeamAId(), match.getScoreA(), isAWinner);
        HBox rowB = createTeamRow(match.getTeamBId(), match.getScoreB(), isBWinner);

        card.getChildren().addAll(topRow, rowA, rowB);

        // Hover effect & Click to update score
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("#121722", "#182030")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#182030", "#121722")));
        card.setOnMouseClicked(e -> MatchController.showScoreDialog(match, () -> renderBracketForTournament(tournamentId)));

        return card;
    }

    private HBox createTeamRow(String teamId, int score, boolean isWinner) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isWinner ? "rgba(124, 92, 252, 0.16)" : "#0B0E14") + "; " +
                "-fx-background-radius: 6px; -fx-padding: 3 8;");

        Optional<Team> teamOpt = teamId != null ? ctx.getTeamService().findById(teamId) : Optional.empty();
        String tag = teamOpt.map(Team::getTag).orElse("TBD");
        String name = teamOpt.map(Team::getName).orElse("Chờ xác định (TBD)");
        String accent = teamOpt.map(Team::getAccentColor).orElse("#334155");

        Label tagPill = new Label(tag);
        tagPill.setMinWidth(38);
        tagPill.setAlignment(Pos.CENTER);
        tagPill.setStyle("-fx-background-color: " + accent + "; -fx-text-fill: #0B0E14; " +
                "-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-padding: 1 5; -fx-background-radius: 4px;");

        Label nameLbl = new Label(name);
        nameLbl.setMaxWidth(145);
        nameLbl.setStyle("-fx-text-fill: " + (teamOpt.isPresent() ? (isWinner ? "#F8FAFC" : "#CBD5E1") : "#64748B") + "; " +
                "-fx-font-weight: " + (isWinner ? "bold" : "normal") + "; -fx-font-size: 11.5px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreLbl = new Label(String.valueOf(score));
        scoreLbl.setStyle("-fx-text-fill: " + (isWinner ? "#00D4FF" : "#94A3B8") + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        row.getChildren().addAll(tagPill, nameLbl, spacer, scoreLbl);
        return row;
    }

    private void renderEmptyState() {
        VBox emptyBox = new VBox(12);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setLayoutX(330);
        emptyBox.setLayoutY(190);

        FontIcon icon = new FontIcon("fas-sitemap");
        icon.setIconSize(38);
        icon.setStyle("-fx-icon-color: #334155;");

        Label lbl = new Label("Giải đấu này chưa có sơ đồ nhánh đấu.");
        lbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label sub = new Label("Hãy đảm bảo giải có tối thiểu 4 đội được duyệt và nhấn \"Tạo / Làm mới nhánh đấu\".");
        sub.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12.5px;");

        emptyBox.getChildren().addAll(icon, lbl, sub);
        bracketCanvas.getChildren().add(emptyBox);
    }
}
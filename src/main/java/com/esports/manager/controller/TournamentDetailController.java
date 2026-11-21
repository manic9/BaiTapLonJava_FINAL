package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.Match;
import com.esports.manager.model.Registration;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.TournamentStatus;
import com.esports.manager.util.ToastManager;
import com.esports.manager.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

public class TournamentDetailController {

    @FXML private Label lblName;
    @FXML private Label lblGame;
    @FXML private Label lblStatus;
    @FXML private Label lblDates;
    @FXML private Label lblLocation;
    @FXML private Label lblPrizePool;
    @FXML private Label lblFormat;
    @FXML private Label lblTeamCount;
    @FXML private Label lblMatchCount;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<TournamentStatus> cbStatus;

    private final AppContext ctx = AppContext.getInstance();
    private Tournament tournament;
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);

    @FXML
    public void initialize() {
        cbStatus.getItems().setAll(TournamentStatus.values());
    }

    public void setTournament(Tournament t) {
        loadTournament(t);
    }

    private void loadTournament(Tournament t) {
        this.tournament = t;
        lblName.setText(t.getName());
        lblGame.setText(t.getGameTitle());
        lblStatus.setText(t.getStatus().getDisplayName());
        lblDates.setText((t.getStartDate() != null ? t.getStartDate().format(dateFmt) : "?") + " → " +
                (t.getEndDate() != null ? t.getEndDate().format(dateFmt) : "?"));
        lblLocation.setText(t.getLocation() != null ? t.getLocation() : "TBD");
        String symbol = ctx.getSettings().getCurrencySymbol();
        lblPrizePool.setText(symbol + currencyFmt.format(t.getPrizePool()));
        lblFormat.setText(t.getFormat().getDisplayName());
        txtDescription.setText(t.getDescription() != null ? t.getDescription() : "");
        cbStatus.setValue(t.getStatus());

        List<Registration> regs = ctx.getRegistrationService().getApprovedByTournament(t.getId());
        lblTeamCount.setText(regs.size() + " / " + t.getMaxTeams());
        lblMatchCount.setText(String.valueOf(ctx.getMatchService().getByTournament(t.getId()).size()));
    }

    @FXML
    private void handleUpdateStatus() {
        if (tournament != null && cbStatus.getValue() != null) {
            tournament.setStatus(cbStatus.getValue());
            ctx.getTournamentService().update(tournament);
            lblStatus.setText(cbStatus.getValue().getDisplayName());
            ToastManager.showSuccess("Đã cập nhật trạng thái giải đấu!");
        }
    }

    @FXML
    private void handleGenerateBracket() {
        if (tournament == null) return;
        try {
            ctx.getBracketService().generateSingleEliminationBracket(tournament.getId());
            ToastManager.showSuccess("Đã tạo sơ đồ nhánh đấu cho " + tournament.getName());
            loadTournament(tournament);
        } catch (IllegalStateException ex) {
            ToastManager.showError(ex.getMessage());
        }
    }

    @FXML
    private void handleViewBracket() {
        if (tournament != null) {
            ViewNavigator.<BracketController>navigate(
                    "BracketView.fxml", "Tournament Bracket",
                    "Sơ đồ nhánh đấu — " + tournament.getName(),
                    ctrl -> ctrl.selectTournament(tournament.getId())
            );
        }
    }

    @FXML
    private void handleBack() {
        ViewNavigator.navigate("TournamentView.fxml", "Tournament Management",
                "Tạo mới, chỉnh sửa, lọc và vận hành các giải đấu Esports");
    }
}

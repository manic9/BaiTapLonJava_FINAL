package com.esports.manager.controller;

import com.esports.manager.config.AppContext;
import com.esports.manager.model.StandingEntry;
import com.esports.manager.model.Team;
import com.esports.manager.model.Tournament;
import com.esports.manager.util.I18n;
import com.esports.manager.util.ToastManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class StandingsController {

    @FXML private ComboBox<Tournament> cbTournament;
    @FXML private TextField txtSearch;
    @FXML private TableView<StandingEntry> standingsTable;

    @FXML private TableColumn<StandingEntry, Integer> colRank;
    @FXML private TableColumn<StandingEntry, String> colTag;
    @FXML private TableColumn<StandingEntry, String> colTeam;
    @FXML private TableColumn<StandingEntry, String> colPlayed;
    @FXML private TableColumn<StandingEntry, String> colWins;
    @FXML private TableColumn<StandingEntry, String> colLosses;
    @FXML private TableColumn<StandingEntry, Integer> colDiff;
    @FXML private TableColumn<StandingEntry, String> colWinRate;
    @FXML private TableColumn<StandingEntry, String> colPoints;

    private final AppContext ctx = AppContext.getInstance();
    private FilteredList<StandingEntry> filteredData;

    @FXML
    public void initialize() {
        setupColumns();

        var tournaments = ctx.getTournamentService().getAll();
        cbTournament.setItems(tournaments);

        cbTournament.valueProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) loadStandings(newT.getId());
        });

        txtSearch.textProperty().addListener((obs, o, n) -> applySearchFilter());

        if (!tournaments.isEmpty()) {
            cbTournament.getSelectionModel().selectFirst();
        }
    }

    private void loadStandings(String tournamentId) {
        List<StandingEntry> list = ctx.getStandingsService().getByTournament(tournamentId);
        filteredData = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
        standingsTable.setItems(filteredData);
        applySearchFilter();
    }

    private void applySearchFilter() {
        if (filteredData == null) return;
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        filteredData.setPredicate(entry -> {
            if (kw.isEmpty()) return true;
            String name = ctx.getTeamService().getTeamNameOrTbd(entry.getTeamId()).toLowerCase();
            String tag = ctx.getTeamService().getTeamTagOrTbd(entry.getTeamId()).toLowerCase();
            return name.contains(kw) || tag.contains(kw);
        });
    }

    private void setupColumns() {
        colRank.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRank()));
        colRank.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer rank, boolean empty) {
                super.updateItem(rank, empty);
                if (empty || rank == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label("#" + rank);
                    badge.setMinWidth(42);
                    badge.setAlignment(Pos.CENTER);
                    String style = switch (rank) {
                        case 1 -> "-fx-background-color: rgba(245, 158, 11, 0.22); -fx-text-fill: #FBBF24; -fx-border-color: #F59E0B;";
                        case 2 -> "-fx-background-color: rgba(148, 163, 184, 0.22); -fx-text-fill: #E2E8F0; -fx-border-color: #94A3B8;";
                        case 3 -> "-fx-background-color: rgba(217, 119, 6, 0.20); -fx-text-fill: #F59E0B; -fx-border-color: #B45309;";
                        default -> "-fx-background-color: #182030; -fx-text-fill: #94A3B8; -fx-border-color: #1E293B;";
                    };
                    badge.setStyle(style + " -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 7px; -fx-border-radius: 7px;");
                    setGraphic(badge);
                }
            }
        });

        colTag.setCellValueFactory(d -> new SimpleStringProperty(ctx.getTeamService().getTeamTagOrTbd(d.getValue().getTeamId())));
        colTag.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tag, boolean empty) {
                super.updateItem(tag, empty);
                if (empty || tag == null) {
                    setGraphic(null);
                } else {
                    StandingEntry entry = getTableView().getItems().get(getIndex());
                    Optional<Team> teamOpt = ctx.getTeamService().findById(entry.getTeamId());
                    String bg = teamOpt.map(Team::getAccentColor).orElse("#7C5CFC");

                    Label pill = new Label(tag);
                    pill.setMinWidth(48);
                    pill.setAlignment(Pos.CENTER);
                    pill.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: #0B0E14; " +
                            "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 6px;");
                    setGraphic(pill);
                }
            }
        });

        colTeam.setCellValueFactory(d -> new SimpleStringProperty(ctx.getTeamService().getTeamNameOrTbd(d.getValue().getTeamId())));
        colTeam.setCellFactory(col -> new TableCell<>() {
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

        colPlayed.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPlayed())));
        colWins.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getWins())));
        colWins.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String w, boolean empty) {
                super.updateItem(w, empty);
                if (empty || w == null) setText(null);
                else {
                    setText(w);
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                }
            }
        });

        colLosses.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getLosses())));
        colLosses.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) setText(null);
                else {
                    setText(l);
                    setStyle("-fx-text-fill: #F87171; -fx-font-weight: 600;");
                }
            }
        });

        colDiff.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getMapDiff()));
        colDiff.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer diff, boolean empty) {
                super.updateItem(diff, empty);
                if (empty || diff == null) {
                    setText(null);
                } else {
                    setText(diff > 0 ? "+" + diff : String.valueOf(diff));
                    String color = diff > 0 ? "#00D4FF" : (diff < 0 ? "#F87171" : "#94A3B8");
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        colWinRate.setCellValueFactory(d -> {
            StandingEntry s = d.getValue();
            double rate = s.getPlayed() > 0 ? (s.getWins() * 100.0 / s.getPlayed()) : 0.0;
            return new SimpleStringProperty(String.format(Locale.US, "%.1f%%", rate));
        });

        colPoints.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPoints() + " PTS"));
        colPoints.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String pts, boolean empty) {
                super.updateItem(pts, empty);
                if (empty || pts == null) {
                    setText(null);
                } else {
                    setText(pts);
                    setStyle("-fx-text-fill: #A78BFA; -fx-font-weight: bold; -fx-font-size: 13px;");
                }
            }
        });
    }

    @FXML
    public void handleRecalculate() {
        Tournament selected = cbTournament.getValue();
        if (selected == null) return;
        ctx.getStandingsService().recalculate(selected.getId());
        loadStandings(selected.getId());
        ToastManager.showSuccess(I18n.get("standings.toast.recalculated", selected.getName()));
    }
}
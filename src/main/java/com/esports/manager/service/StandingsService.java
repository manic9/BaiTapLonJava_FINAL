package com.esports.manager.service;

import com.esports.manager.model.Match;
import com.esports.manager.model.Registration;
import com.esports.manager.model.StandingEntry;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandingsService {
    private final Repository<StandingEntry> standingRepo;
    private final Repository<Match> matchRepo;
    private final Repository<Registration> registrationRepo;

    public StandingsService(Repository<StandingEntry> standingRepo,
                            Repository<Match> matchRepo,
                            Repository<Registration> registrationRepo) {
        this.standingRepo = standingRepo;
        this.matchRepo = matchRepo;
        this.registrationRepo = registrationRepo;
    }

    public ObservableList<StandingEntry> getAll() {
        return standingRepo.findAll();
    }

    public List<StandingEntry> getByTournament(String tournamentId) {
        return standingRepo.findAll().stream()
                .filter(s -> s.getTournamentId().equals(tournamentId))
                .sorted(Comparator.comparingInt(StandingEntry::getRank))
                .toList();
    }

    public void recalculate(String tournamentId) {
        standingRepo.findAll().removeIf(s -> s.getTournamentId().equals(tournamentId));

        Map<String, StandingEntry> map = new HashMap<>();
        registrationRepo.findAll().stream()
                .filter(r -> r.getTournamentId().equals(tournamentId) && r.getStatus() == RegistrationStatus.APPROVED)
                .forEach(r -> map.put(r.getTeamId(), new StandingEntry(tournamentId, r.getTeamId())));

        for (Match m : matchRepo.findAll()) {
            if (!m.getTournamentId().equals(tournamentId) || m.getStatus() != MatchStatus.COMPLETED) continue;
            if (m.getTeamAId() == null || m.getTeamBId() == null) continue;

            StandingEntry entryA = map.computeIfAbsent(m.getTeamAId(), id -> new StandingEntry(tournamentId, id));
            StandingEntry entryB = map.computeIfAbsent(m.getTeamBId(), id -> new StandingEntry(tournamentId, id));

            entryA.setPlayed(entryA.getPlayed() + 1);
            entryB.setPlayed(entryB.getPlayed() + 1);
            entryA.setMapDiff(entryA.getMapDiff() + (m.getScoreA() - m.getScoreB()));
            entryB.setMapDiff(entryB.getMapDiff() + (m.getScoreB() - m.getScoreA()));

            if (m.getScoreA() > m.getScoreB()) {
                entryA.setWins(entryA.getWins() + 1);
                entryA.setPoints(entryA.getPoints() + 3);
                entryB.setLosses(entryB.getLosses() + 1);
            } else if (m.getScoreB() > m.getScoreA()) {
                entryB.setWins(entryB.getWins() + 1);
                entryB.setPoints(entryB.getPoints() + 3);
                entryA.setLosses(entryA.getLosses() + 1);
            }
        }

        List<StandingEntry> sorted = map.values().stream()
                .sorted(Comparator.comparingInt(StandingEntry::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(StandingEntry::getMapDiff).reversed())
                        .thenComparing(Comparator.comparingInt(StandingEntry::getWins).reversed()))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            StandingEntry entry = sorted.get(i);
            entry.setRank(i + 1);
            standingRepo.save(entry);
        }
    }
}
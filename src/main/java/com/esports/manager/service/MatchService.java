package com.esports.manager.service;

import com.esports.manager.model.Match;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class MatchService {
    private final Repository<Match> matchRepo;
    private final BracketService bracketService;
    private final StandingsService standingsService;
    private final TeamService teamService;
    private final ActivityService activityService;

    public MatchService(Repository<Match> matchRepo,
                        BracketService bracketService,
                        StandingsService standingsService,
                        TeamService teamService,
                        ActivityService activityService) {
        this.matchRepo = matchRepo;
        this.bracketService = bracketService;
        this.standingsService = standingsService;
        this.teamService = teamService;
        this.activityService = activityService;
    }

    public ObservableList<Match> getAll() {
        return matchRepo.findAll();
    }

    public Optional<Match> findById(String id) {
        return matchRepo.findById(id);
    }

    public List<Match> getByTournament(String tournamentId) {
        return matchRepo.findAll().stream()
                .filter(m -> m.getTournamentId().equals(tournamentId))
                .toList();
    }

    public Match save(Match match) {
        matchRepo.save(match);
        activityService.log("MATCH", "Tạo trận đấu mới: " + match.getRoundName());
        return match;
    }

    public boolean delete(String id) {
        Optional<Match> existing = matchRepo.findById(id);
        if (existing.isPresent() && matchRepo.deleteById(id)) {
            activityService.log("MATCH", "Đã xóa trận đấu: " + id);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật tỉ số và trạng thái trận đấu. Tự động xác định winner và advance bracket nếu cần.
     */
    public void updateMatchResult(String matchId, int scoreA, int scoreB, MatchStatus status) {
        matchRepo.findById(matchId).ifPresent(match -> {
            match.setScoreA(scoreA);
            match.setScoreB(scoreB);
            match.setStatus(status);

            if (status == MatchStatus.COMPLETED) {
                if (scoreA > scoreB) {
                    match.setWinnerId(match.getTeamAId());
                } else if (scoreB > scoreA) {
                    match.setWinnerId(match.getTeamBId());
                }
                // Tự động đưa winner vào vòng tiếp theo
                bracketService.advanceWinnerIfEligible(match);
                // Tính lại bảng xếp hạng
                standingsService.recalculate(match.getTournamentId());
            } else {
                match.setWinnerId(null);
            }

            matchRepo.save(match);
            String nameA = teamService.getTeamNameOrTbd(match.getTeamAId());
            String nameB = teamService.getTeamNameOrTbd(match.getTeamBId());
            activityService.log("MATCH", "Cập nhật tỉ số: " + nameA + " " + scoreA + " - " + scoreB + " " + nameB);
        });
    }
}
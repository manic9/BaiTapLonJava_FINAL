package com.esports.manager.service;

import com.esports.manager.model.AppSettings;
import com.esports.manager.model.Match;
import com.esports.manager.model.Registration;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.repository.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BracketService {
    private final Repository<Match> matchRepo;
    private final RegistrationService registrationService;
    private final AppSettings settings;
    private final ActivityService activityService;

    public BracketService(Repository<Match> matchRepo,
                          RegistrationService registrationService,
                          AppSettings settings,
                          ActivityService activityService) {
        this.matchRepo = matchRepo;
        this.registrationService = registrationService;
        this.settings = settings;
        this.activityService = activityService;
    }

    /**
     * Tự động sinh cây nhánh đấu loại trực tiếp (4 hoặc 8 đội) từ danh sách đội đã duyệt.
     */
    public List<Match> generateSingleEliminationBracket(String tournamentId) {
        List<Registration> approved = getUniqueApprovedRegistrations(tournamentId);

        if (approved.size() < 4) {
            throw new IllegalStateException("Cần tối thiểu 4 đội đã được duyệt để tạo nhánh đấu!");
        }

        List<Match> existingMatches = new ArrayList<>(matchRepo.findAll().stream()
                .filter(match -> tournamentId.equals(match.getTournamentId()))
                .toList());
        List<Match> repairedBracket = repairMissingFourTeamFinal(tournamentId, approved, existingMatches);
        if (repairedBracket != null) {
            activityService.log("BRACKET", "Đã bổ sung trận chung kết còn thiếu cho giải " + tournamentId);
            return repairedBracket;
        }

        // Delete old bracket records from the repository/database, not only from
        // the observable cache returned by findAll().
        for (Match oldMatch : existingMatches) {
            matchRepo.deleteById(oldMatch.getId());
        }

        int bracketSize = approved.size() >= 8 ? 8 : 4;
        List<Match> createdMatches = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);

        if (bracketSize == 8) {
            // Tạo trước Chung kết (Vòng 3) và Bán kết (Vòng 2) để lấy ID liên kết
            Match finalMatch = buildEmptyMatch(tournamentId, 3, 1, "Chung kết tổng", 5, null, 0, baseTime.plusDays(2));
            Match sf1 = buildEmptyMatch(tournamentId, 2, 1, "Bán kết 1", 3, finalMatch.getId(), 1, baseTime.plusDays(1));
            Match sf2 = buildEmptyMatch(tournamentId, 2, 2, "Bán kết 2", 3, finalMatch.getId(), 2, baseTime.plusDays(1).plusHours(3));

            // Cặp đấu chuẩn Seed: (1v8), (4v5), (2v7), (3v6)
            int[][] pairs = {{0, 7}, {3, 4}, {1, 6}, {2, 5}};
            for (int i = 0; i < 4; i++) {
                String nextId = (i < 2) ? sf1.getId() : sf2.getId();
                int slot = (i % 2 == 0) ? 1 : 2;
                Match qf = buildEmptyMatch(tournamentId, 1, i + 1, "Tứ kết " + (i + 1),
                        settings.getDefaultBestOf(), nextId, slot, baseTime.plusHours(i * 2L));
                qf.setTeamAId(approved.get(pairs[i][0]).getTeamId());
                qf.setTeamBId(approved.get(pairs[i][1]).getTeamId());
                createdMatches.add(qf);
            }
            createdMatches.add(sf1);
            createdMatches.add(sf2);
            createdMatches.add(finalMatch);
        } else {
            // Nhánh 4 đội: Bán kết (Vòng 1) -> Chung kết (Vòng 2)
            Match finalMatch = buildEmptyMatch(tournamentId, 2, 1, "Chung kết tổng", 5, null, 0, baseTime.plusDays(1));
            Match sf1 = buildEmptyMatch(tournamentId, 1, 1, "Bán kết 1", settings.getDefaultBestOf(), finalMatch.getId(), 1, baseTime);
            sf1.setTeamAId(approved.get(0).getTeamId());
            sf1.setTeamBId(approved.get(3).getTeamId());

            Match sf2 = buildEmptyMatch(tournamentId, 1, 2, "Bán kết 2", settings.getDefaultBestOf(), finalMatch.getId(), 2, baseTime.plusHours(3));
            sf2.setTeamAId(approved.get(1).getTeamId());
            sf2.setTeamBId(approved.get(2).getTeamId());

            createdMatches.addAll(List.of(sf1, sf2, finalMatch));
        }

        // Self-referencing FK_Matches_NextMatch requires every destination row to
        // exist before a source row can point to it. Insert the whole bracket
        // without links first, then persist the links in a second pass.
        for (Match match : createdMatches) {
            String nextMatchId = match.getNextMatchId();
            match.setNextMatchId(null);
            matchRepo.save(match);
            match.setNextMatchId(nextMatchId);
        }
        createdMatches.forEach(matchRepo::save);
        activityService.log("BRACKET", "Đã khởi tạo nhánh đấu " + bracketSize + " đội cho giải " + tournamentId);
        return createdMatches;
    }

    /** Add a missing final to an existing four-team bracket without resetting its results. */
    public void ensureFourTeamFinalExists(String tournamentId) {
        List<Registration> approved = getUniqueApprovedRegistrations(tournamentId);
        if (approved.size() != 4) return;

        List<Match> existingMatches = new ArrayList<>(matchRepo.findAll().stream()
                .filter(match -> tournamentId.equals(match.getTournamentId()))
                .toList());
        if (repairMissingFourTeamFinal(tournamentId, approved, existingMatches) != null) {
            activityService.log("BRACKET", "Đã tự bổ sung trận chung kết còn thiếu cho giải " + tournamentId);
        }
    }

    private List<Registration> getUniqueApprovedRegistrations(String tournamentId) {
        List<Registration> approved = new ArrayList<>(registrationService.getApprovedByTournament(tournamentId));
        approved.sort(Comparator.comparingInt(Registration::getSeedNumber));

        // Historical data may contain multiple approved registrations for one team.
        // A team must occupy only one slot in a bracket.
        Set<String> teamIds = new HashSet<>();
        approved.removeIf(registration -> registration.getTeamId() == null
                || !teamIds.add(registration.getTeamId()));
        return approved;
    }

    /**
     * Older/incomplete four-team brackets may contain only their two semifinals.
     * Add the missing final and link existing semifinals without discarding scores.
     */
    private List<Match> repairMissingFourTeamFinal(String tournamentId,
                                                    List<Registration> approved,
                                                    List<Match> existingMatches) {
        if (approved.size() != 4 || existingMatches.size() != 2) return null;

        List<Match> semifinals = existingMatches.stream()
                .filter(match -> isSemifinal(match.getRoundName()))
                .sorted(Comparator.comparingInt(Match::getMatchNumber))
                .toList();
        boolean alreadyHasFinal = existingMatches.stream().anyMatch(match -> isFinal(match.getRoundName()));
        if (semifinals.size() != 2 || alreadyHasFinal) return null;

        LocalDateTime finalTime = semifinals.stream()
                .map(Match::getScheduledTime)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(time -> time.plusDays(1))
                .orElseGet(() -> LocalDateTime.now().plusDays(1).withHour(18).withMinute(0));
        Match finalMatch = buildEmptyMatch(tournamentId, 2, 1, "Chung kết tổng", 5,
                null, 0, finalTime);

        for (int i = 0; i < semifinals.size(); i++) {
            Match semifinal = semifinals.get(i);
            if (semifinal.getStatus() == MatchStatus.COMPLETED && semifinal.getWinnerId() != null) {
                if (i == 0) finalMatch.setTeamAId(semifinal.getWinnerId());
                else finalMatch.setTeamBId(semifinal.getWinnerId());
            }
        }

        // Insert destination first to satisfy the self-referencing SQL foreign key.
        matchRepo.save(finalMatch);
        for (int i = 0; i < semifinals.size(); i++) {
            Match semifinal = semifinals.get(i);
            semifinal.setNextMatchId(finalMatch.getId());
            semifinal.setNextMatchSlot(i + 1);
            matchRepo.save(semifinal);
        }

        return new ArrayList<>(matchRepo.findAll().stream()
                .filter(match -> tournamentId.equals(match.getTournamentId()))
                .toList());
    }

    private boolean isSemifinal(String roundName) {
        if (roundName == null) return false;
        String normalized = roundName.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("bán kết") || normalized.contains("semi-final")
                || normalized.contains("semifinal");
    }

    private boolean isFinal(String roundName) {
        if (roundName == null) return false;
        String normalized = roundName.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("chung kết") || normalized.startsWith("final")
                || normalized.contains("grand final");
    }

    /**
     * Tự động đưa đội thắng vào trận đấu vòng kế tiếp khi trận hiện tại hoàn thành.
     */
    public void advanceWinnerIfEligible(Match completedMatch) {
        if (!settings.isAutoAdvanceBracket()) return;
        if (completedMatch.getWinnerId() == null || completedMatch.getNextMatchId() == null) return;

        matchRepo.findById(completedMatch.getNextMatchId()).ifPresent(nextMatch -> {
            if (completedMatch.getNextMatchSlot() == 1) {
                nextMatch.setTeamAId(completedMatch.getWinnerId());
            } else if (completedMatch.getNextMatchSlot() == 2) {
                nextMatch.setTeamBId(completedMatch.getWinnerId());
            }
            matchRepo.save(nextMatch);
        });
    }

    private Match buildEmptyMatch(String tournamentId, int round, int matchNum, String roundName,
                                  int bestOf, String nextId, int nextSlot, LocalDateTime time) {
        String id = "MCH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return new Match(id, tournamentId, round, matchNum, roundName,
                null, null, 0, 0, bestOf, null, nextId, nextSlot, time, MatchStatus.SCHEDULED);
    }
}

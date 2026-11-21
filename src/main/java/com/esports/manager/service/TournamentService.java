package com.esports.manager.service;

import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.model.Tournament;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

import java.util.Optional;
import java.util.UUID;

public class TournamentService {
    private final Repository<Tournament> tournamentRepo;
    private final Repository<PrizeAllocation> prizeRepo;
    private final ActivityService activityService;

    public TournamentService(Repository<Tournament> tournamentRepo,
                             Repository<PrizeAllocation> prizeRepo,
                             ActivityService activityService) {
        this.tournamentRepo = tournamentRepo;
        this.prizeRepo = prizeRepo;
        this.activityService = activityService;
    }

    public ObservableList<Tournament> getAll() {
        return tournamentRepo.findAll();
    }

    public Optional<Tournament> findById(String id) {
        return tournamentRepo.findById(id);
    }

    public Tournament create(Tournament tournament) {
        tournamentRepo.save(tournament);
        // Tự động tạo cơ cấu giải thưởng mặc định (50% - 30% - 20%)
        createDefaultPrizes(tournament);
        activityService.log("TOURNAMENT", "Tạo giải đấu mới: " + tournament.getName());
        return tournament;
    }

    public Tournament update(Tournament tournament) {
        tournamentRepo.save(tournament);
        activityService.log("TOURNAMENT", "Cập nhật giải đấu: " + tournament.getName());
        return tournament;
    }

    public boolean delete(String id) {
        Optional<Tournament> existing = tournamentRepo.findById(id);
        if (existing.isPresent() && tournamentRepo.deleteById(id)) {
            prizeRepo.findAll().removeIf(p -> p.getTournamentId().equals(id));
            activityService.log("TOURNAMENT", "Đã xóa giải đấu: " + existing.get().getName());
            return true;
        }
        return false;
    }

    private void createDefaultPrizes(Tournament t) {
        double pool = t.getPrizePool();
        if (pool <= 0) return;
        prizeRepo.save(new PrizeAllocation("PRZ-" + shortId(), t.getId(), "Hạng 1 (Vô địch)", 50.0, pool * 0.50, null, false));
        prizeRepo.save(new PrizeAllocation("PRZ-" + shortId(), t.getId(), "Hạng 2 (Á quân)", 30.0, pool * 0.30, null, false));
        prizeRepo.save(new PrizeAllocation("PRZ-" + shortId(), t.getId(), "Hạng 3 (Đồng hạng)", 20.0, pool * 0.20, null, false));
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
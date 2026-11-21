package com.esports.manager.service;

import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

import java.util.List;

public class PrizeService {
    private final Repository<PrizeAllocation> prizeRepo;
    private final ActivityService activityService;

    public PrizeService(Repository<PrizeAllocation> prizeRepo, ActivityService activityService) {
        this.prizeRepo = prizeRepo;
        this.activityService = activityService;
    }

    public ObservableList<PrizeAllocation> getAll() {
        return prizeRepo.findAll();
    }

    public List<PrizeAllocation> getByTournament(String tournamentId) {
        return prizeRepo.findAll().stream()
                .filter(p -> p.getTournamentId().equals(tournamentId))
                .toList();
    }

    public PrizeAllocation save(PrizeAllocation allocation) {
        prizeRepo.save(allocation);
        activityService.log("PRIZE", "Cập nhật hạng mục giải thưởng: " + allocation.getPlacement());
        return allocation;
    }

    public boolean delete(String id) {
        return prizeRepo.deleteById(id);
    }
}
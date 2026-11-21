package com.esports.manager.service;

import com.esports.manager.model.Player;
import com.esports.manager.model.Team;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class TeamService {
    private final Repository<Team> teamRepo;
    private final Repository<Player> playerRepo;
    private final ActivityService activityService;

    public TeamService(Repository<Team> teamRepo,
                       Repository<Player> playerRepo,
                       ActivityService activityService) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.activityService = activityService;
    }

    public ObservableList<Team> getAll() {
        return teamRepo.findAll();
    }

    public Optional<Team> findById(String id) {
        return teamRepo.findById(id);
    }

    public String getTeamNameOrTbd(String teamId) {
        if (teamId == null || teamId.isBlank()) return "TBD";
        return teamRepo.findById(teamId).map(Team::getName).orElse("TBD");
    }

    public String getTeamTagOrTbd(String teamId) {
        if (teamId == null || teamId.isBlank()) return "TBD";
        return teamRepo.findById(teamId).map(Team::getTag).orElse("TBD");
    }

    public List<Player> getRoster(String teamId) {
        return playerRepo.findAll().stream()
                .filter(p -> teamId.equals(p.getTeamId()))
                .toList();
    }

    public Team save(Team team, boolean isNew) {
        teamRepo.save(team);
        activityService.log("TEAM", (isNew ? "Thêm đội tuyển: " : "Cập nhật đội tuyển: ") + team.getName());
        return team;
    }

    public boolean delete(String id) {
        Optional<Team> existing = teamRepo.findById(id);
        if (existing.isPresent() && teamRepo.deleteById(id)) {
            activityService.log("TEAM", "Đã xóa đội tuyển: " + existing.get().getName());
            return true;
        }
        return false;
    }
}

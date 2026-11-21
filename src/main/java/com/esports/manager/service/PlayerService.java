package com.esports.manager.service;

import com.esports.manager.model.Player;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

public class PlayerService {
    private final Repository<Player> playerRepo;
    private final ActivityService activityService;

    public PlayerService(Repository<Player> playerRepo, ActivityService activityService) {
        this.playerRepo = playerRepo;
        this.activityService = activityService;
    }

    public ObservableList<Player> getAll() {
        return playerRepo.findAll();
    }

    public Player save(Player player, boolean isNew) {
        playerRepo.save(player);
        activityService.log("PLAYER", (isNew ? "Thêm tuyển thủ: " : "Cập nhật tuyển thủ: ") + player.getInGameName());
        return player;
    }

    public boolean delete(String id) {
        return playerRepo.findById(id).map(p -> {
            playerRepo.deleteById(id);
            activityService.log("PLAYER", "Đã xóa tuyển thủ: " + p.getInGameName());
            return true;
        }).orElse(false);
    }
}
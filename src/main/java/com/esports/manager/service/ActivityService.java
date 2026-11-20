package com.esports.manager.service;

import com.esports.manager.model.ActivityLog;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

public class ActivityService {
    private final Repository<ActivityLog> activityRepo;

    public ActivityService(Repository<ActivityLog> activityRepo) {
        this.activityRepo = activityRepo;
    }

    public ObservableList<ActivityLog> getAllLogs() {
        return activityRepo.findAll();
    }

    public void log(String category, String message) {
        ObservableList<ActivityLog> list = activityRepo.findAll();
        list.add(0, new ActivityLog(category, message, "Admin"));
        if (list.size() > 50) {
            list.remove(list.size() - 1);
        }
    }
}
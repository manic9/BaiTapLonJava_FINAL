package com.esports.manager.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ActivityLog implements Identifiable {
    private String id;
    private LocalDateTime timestamp;
    private String category;
    private String message;
    private String actor;

    public ActivityLog(String category, String message, String actor) {
        this.id = "LOG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.timestamp = LocalDateTime.now();
        this.category = category;
        this.message = message;
        this.actor = actor;
    }

    public ActivityLog(LocalDateTime timestamp, String category, String message, String actor) {
        this(category, message, actor);
        this.timestamp = timestamp;
    }

    @Override
    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public String getActor() { return actor; }
}

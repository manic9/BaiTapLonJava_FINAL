package com.esports.manager.model.enums;

public enum MatchStatus {
    SCHEDULED("Chưa đấu", "badge-upcoming"),
    LIVE("Đang diễn ra", "badge-ongoing"),
    COMPLETED("Hoàn thành", "badge-completed");

    private final String displayName;
    private final String badgeStyle;

    MatchStatus(String displayName, String badgeStyle) {
        this.displayName = displayName;
        this.badgeStyle = badgeStyle;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeStyle() { return badgeStyle; }

    @Override
    public String toString() { return displayName; }
}
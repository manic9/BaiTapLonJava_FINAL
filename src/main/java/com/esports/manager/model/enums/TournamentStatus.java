package com.esports.manager.model.enums;

public enum TournamentStatus {
    UPCOMING("Sắp diễn ra", "badge-upcoming"),
    ONGOING("Đang diễn ra", "badge-ongoing"),
    COMPLETED("Đã kết thúc", "badge-completed"),
    CANCELLED("Đã hủy", "badge-danger");

    private final String displayName;
    private final String badgeStyle;

    TournamentStatus(String displayName, String badgeStyle) {
        this.displayName = displayName;
        this.badgeStyle = badgeStyle;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeStyle() { return badgeStyle; }

    @Override
    public String toString() { return displayName; }
}
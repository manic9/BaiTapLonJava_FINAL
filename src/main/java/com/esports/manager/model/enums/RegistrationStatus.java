package com.esports.manager.model.enums;

public enum RegistrationStatus {
    PENDING("Chờ duyệt", "badge-upcoming"),
    APPROVED("Đã duyệt", "badge-completed"),
    REJECTED("Từ chối", "badge-danger");

    private final String displayName;
    private final String badgeStyle;

    RegistrationStatus(String displayName, String badgeStyle) {
        this.displayName = displayName;
        this.badgeStyle = badgeStyle;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeStyle() { return badgeStyle; }

    @Override
    public String toString() { return displayName; }
}
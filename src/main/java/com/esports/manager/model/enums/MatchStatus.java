package com.esports.manager.model.enums;

import com.esports.manager.util.I18n;

public enum MatchStatus {
    SCHEDULED("status.match.scheduled", "badge-upcoming"),
    LIVE("status.match.live", "badge-ongoing"),
    COMPLETED("status.match.completed", "badge-completed");

    private final String i18nKey;
    private final String badgeStyle;

    MatchStatus(String i18nKey, String badgeStyle) {
        this.i18nKey = i18nKey;
        this.badgeStyle = badgeStyle;
    }

    public String getDisplayName() { return I18n.get(i18nKey); }
    public String getBadgeStyle() { return badgeStyle; }

    @Override
    public String toString() { return getDisplayName(); }
}
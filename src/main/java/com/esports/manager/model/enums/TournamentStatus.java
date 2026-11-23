package com.esports.manager.model.enums;

import com.esports.manager.util.I18n;

public enum TournamentStatus {
    UPCOMING("status.tournament.upcoming", "badge-upcoming"),
    ONGOING("status.tournament.ongoing", "badge-ongoing"),
    COMPLETED("status.tournament.completed", "badge-completed"),
    CANCELLED("status.tournament.cancelled", "badge-danger");

    private final String i18nKey;
    private final String badgeStyle;

    TournamentStatus(String i18nKey, String badgeStyle) {
        this.i18nKey = i18nKey;
        this.badgeStyle = badgeStyle;
    }

    public String getDisplayName() { return I18n.get(i18nKey); }
    public String getBadgeStyle() { return badgeStyle; }

    @Override
    public String toString() { return getDisplayName(); }
}
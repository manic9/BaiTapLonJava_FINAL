package com.esports.manager.model.enums;

import com.esports.manager.util.I18n;

public enum RegistrationStatus {
    PENDING("status.registration.pending", "badge-upcoming"),
    APPROVED("status.registration.approved", "badge-completed"),
    REJECTED("status.registration.rejected", "badge-danger");

    private final String i18nKey;
    private final String badgeStyle;

    RegistrationStatus(String i18nKey, String badgeStyle) {
        this.i18nKey = i18nKey;
        this.badgeStyle = badgeStyle;
    }

    public String getDisplayName() { return I18n.get(i18nKey); }
    public String getBadgeStyle() { return badgeStyle; }

    @Override
    public String toString() { return getDisplayName(); }
}
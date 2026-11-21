package com.esports.manager.model;

public class AppSettings {
    private String organizationName = "Nexus Arena Esports";
    private String currencySymbol = "$";
    private int defaultBestOf = 3;
    private boolean autoAdvanceBracket = true;
    private boolean confirmBeforeDelete = true;

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public int getDefaultBestOf() { return defaultBestOf; }
    public void setDefaultBestOf(int defaultBestOf) { this.defaultBestOf = defaultBestOf; }

    public boolean isAutoAdvanceBracket() { return autoAdvanceBracket; }
    public void setAutoAdvanceBracket(boolean autoAdvanceBracket) { this.autoAdvanceBracket = autoAdvanceBracket; }

    public boolean isConfirmBeforeDelete() { return confirmBeforeDelete; }
    public void setConfirmBeforeDelete(boolean confirmBeforeDelete) { this.confirmBeforeDelete = confirmBeforeDelete; }
}
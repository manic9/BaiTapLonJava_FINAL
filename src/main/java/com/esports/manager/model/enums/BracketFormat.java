package com.esports.manager.model.enums;

public enum BracketFormat {
    SINGLE_ELIMINATION("Loại trực tiếp (Single Elimination)"),
    ROUND_ROBIN("Vòng tròn tính điểm (Round Robin)");

    private final String displayName;

    BracketFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
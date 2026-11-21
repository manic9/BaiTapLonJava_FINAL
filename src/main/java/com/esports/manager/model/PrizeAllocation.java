package com.esports.manager.model;

import java.util.UUID;

public class PrizeAllocation implements Identifiable {
    private String id;
    private String tournamentId;
    private String placement;
    private double percentage;
    private double amount;
    private String teamId;
    private boolean paid;

    public PrizeAllocation() {
        this.id = "PRZ-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public PrizeAllocation(String id, String tournamentId, String placement,
                           double percentage, double amount, String teamId, boolean paid) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.placement = placement;
        this.percentage = percentage;
        this.amount = amount;
        this.teamId = teamId;
        this.paid = paid;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getPlacement() { return placement; }
    public void setPlacement(String placement) { this.placement = placement; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
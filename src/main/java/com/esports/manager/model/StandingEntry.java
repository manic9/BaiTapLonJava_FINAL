package com.esports.manager.model;

import java.util.UUID;

public class StandingEntry implements Identifiable {
    private String id;
    private String tournamentId;
    private String teamId;
    private int rank;
    private int played;
    private int wins;
    private int losses;
    private int mapDiff;
    private int points;

    public StandingEntry(String tournamentId, String teamId) {
        this.id = "STD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.tournamentId = tournamentId;
        this.teamId = teamId;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getMapDiff() { return mapDiff; }
    public void setMapDiff(int mapDiff) { this.mapDiff = mapDiff; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
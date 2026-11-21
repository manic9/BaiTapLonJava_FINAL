package com.esports.manager.model;

import com.esports.manager.model.enums.MatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Match implements Identifiable {
    private String id;
    private String tournamentId;
    private int round;
    private int matchNumber;
    private String roundName;
    private String teamAId;
    private String teamBId;
    private int scoreA;
    private int scoreB;
    private int bestOf;
    private String winnerId;
    private String nextMatchId;
    private int nextMatchSlot; // 1 = Team A slot, 2 = Team B slot
    private LocalDateTime scheduledTime;
    private MatchStatus status;

    public Match() {
        this.id = "MCH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.bestOf = 3;
        this.status = MatchStatus.SCHEDULED;
    }

    public Match(String id, String tournamentId, int round, int matchNumber, String roundName,
                 String teamAId, String teamBId, int scoreA, int scoreB, int bestOf,
                 String winnerId, String nextMatchId, int nextMatchSlot,
                 LocalDateTime scheduledTime, MatchStatus status) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.round = round;
        this.matchNumber = matchNumber;
        this.roundName = roundName;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.bestOf = bestOf;
        this.winnerId = winnerId;
        this.nextMatchId = nextMatchId;
        this.nextMatchSlot = nextMatchSlot;
        this.scheduledTime = scheduledTime;
        this.status = status;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public int getMatchNumber() { return matchNumber; }
    public void setMatchNumber(int matchNumber) { this.matchNumber = matchNumber; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public String getTeamAId() { return teamAId; }
    public void setTeamAId(String teamAId) { this.teamAId = teamAId; }

    public String getTeamBId() { return teamBId; }
    public void setTeamBId(String teamBId) { this.teamBId = teamBId; }

    public int getScoreA() { return scoreA; }
    public void setScoreA(int scoreA) { this.scoreA = scoreA; }

    public int getScoreB() { return scoreB; }
    public void setScoreB(int scoreB) { this.scoreB = scoreB; }

    public int getBestOf() { return bestOf; }
    public void setBestOf(int bestOf) { this.bestOf = bestOf; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public String getNextMatchId() { return nextMatchId; }
    public void setNextMatchId(String nextMatchId) { this.nextMatchId = nextMatchId; }

    public int getNextMatchSlot() { return nextMatchSlot; }
    public void setNextMatchSlot(int nextMatchSlot) { this.nextMatchSlot = nextMatchSlot; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
}
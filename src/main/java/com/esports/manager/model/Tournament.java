package com.esports.manager.model;

import com.esports.manager.model.enums.BracketFormat;
import com.esports.manager.model.enums.TournamentStatus;

import java.time.LocalDate;
import java.util.UUID;

public class Tournament implements Identifiable {
    private String id;
    private String name;
    private String gameTitle;
    private String organizer;
    private BracketFormat format;
    private TournamentStatus status;
    private int maxTeams;
    private double prizePool;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String description;

    public Tournament() {
        this.id = "TRN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public Tournament(String id, String name, String gameTitle, String organizer,
                      BracketFormat format, TournamentStatus status, int maxTeams,
                      double prizePool, LocalDate startDate, LocalDate endDate,
                      String location, String description) {
        this.id = id;
        this.name = name;
        this.gameTitle = gameTitle;
        this.organizer = organizer;
        this.format = format;
        this.status = status;
        this.maxTeams = maxTeams;
        this.prizePool = prizePool;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.description = description;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public BracketFormat getFormat() { return format; }
    public void setFormat(BracketFormat format) { this.format = format; }

    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public int getMaxTeams() { return maxTeams; }
    public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }

    public double getPrizePool() { return prizePool; }
    public void setPrizePool(double prizePool) { this.prizePool = prizePool; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name + " (" + gameTitle + ")";
    }
}
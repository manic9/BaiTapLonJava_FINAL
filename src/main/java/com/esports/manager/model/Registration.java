package com.esports.manager.model;

import com.esports.manager.model.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Registration implements Identifiable {
    private String id;
    private String tournamentId;
    private String teamId;
    private LocalDateTime registeredAt;
    private int seedNumber;
    private RegistrationStatus status;
    private String notes;

    public Registration() {
        this.id = "REG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.registeredAt = LocalDateTime.now();
        this.status = RegistrationStatus.PENDING;
    }

    public Registration(String id, String tournamentId, String teamId,
                        LocalDateTime registeredAt, int seedNumber,
                        RegistrationStatus status, String notes) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        this.registeredAt = registeredAt;
        this.seedNumber = seedNumber;
        this.status = status;
        this.notes = notes;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public int getSeedNumber() { return seedNumber; }
    public void setSeedNumber(int seedNumber) { this.seedNumber = seedNumber; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
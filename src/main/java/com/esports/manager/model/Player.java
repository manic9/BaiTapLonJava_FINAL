package com.esports.manager.model;

import java.util.UUID;

public class Player implements Identifiable {
    private String id;
    private String teamId;
    private String inGameName;
    private String fullName;
    private String role;
    private String nationality;
    private int age;
    private double kdaRatio;
    private boolean captain;

    public Player() {
        this.id = "PLR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public Player(String id, String teamId, String inGameName, String fullName,
                  String role, String nationality, int age, double kdaRatio, boolean captain) {
        this.id = id;
        this.teamId = teamId;
        this.inGameName = inGameName;
        this.fullName = fullName;
        this.role = role;
        this.nationality = nationality;
        this.age = age;
        this.kdaRatio = kdaRatio;
        this.captain = captain;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getInGameName() { return inGameName; }
    public void setInGameName(String inGameName) { this.inGameName = inGameName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getKdaRatio() { return kdaRatio; }
    public void setKdaRatio(double kdaRatio) { this.kdaRatio = kdaRatio; }

    public boolean isCaptain() { return captain; }
    public void setCaptain(boolean captain) { this.captain = captain; }
}
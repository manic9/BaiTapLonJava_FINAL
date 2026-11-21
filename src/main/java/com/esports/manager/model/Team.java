package com.esports.manager.model;

import java.util.UUID;

public class Team implements Identifiable {
    private String id;
    private String name;
    private String tag;
    private String region;
    private String coach;
    private String contactEmail;
    private int foundedYear;
    private String accentColor;

    public Team() {
        this.id = "TEAM-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.accentColor = "#7C5CFC";
    }

    public Team(String id, String name, String tag, String region, String coach,
                String contactEmail, int foundedYear, String accentColor) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.region = region;
        this.coach = coach;
        this.contactEmail = contactEmail;
        this.foundedYear = foundedYear;
        this.accentColor = accentColor;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public int getFoundedYear() { return foundedYear; }
    public void setFoundedYear(int foundedYear) { this.foundedYear = foundedYear; }

    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

    @Override
    public String toString() {
        return "[" + tag + "] " + name;
    }
}
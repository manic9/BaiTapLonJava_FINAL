package com.nexusarena.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tournament {
    private String id;
    private String name;
    private String game;
    private String format;
    private int maxTeams;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private BigDecimal prizePool;
    private String status;
    private String bannerUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Trường bổ sung phục vụ hiển thị nhanh số đội đã duyệt trên UI (JOIN/COUNT)
    private int approvedTeamsCount;

    public Tournament() {
        this.format = "SINGLE_ELIMINATION";
        this.maxTeams = 8;
        this.prizePool = BigDecimal.ZERO;
        this.status = "UPCOMING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public int getMaxTeams() { return maxTeams; }
    public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getPrizePool() { return prizePool; }
    public void setPrizePool(BigDecimal prizePool) { this.prizePool = prizePool; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getApprovedTeamsCount() { return approvedTeamsCount; }
    public void setApprovedTeamsCount(int approvedTeamsCount) { this.approvedTeamsCount = approvedTeamsCount; }
}
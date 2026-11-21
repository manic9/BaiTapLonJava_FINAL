package com.esports.manager.service;

import com.esports.manager.model.Registration;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.repository.Repository;
import javafx.collections.ObservableList;

import java.util.List;

public class RegistrationService {
    private final Repository<Registration> registrationRepo;
    private final Repository<Tournament> tournamentRepo;
    private final TeamService teamService;
    private final ActivityService activityService;

    public RegistrationService(Repository<Registration> registrationRepo,
                               Repository<Tournament> tournamentRepo,
                               TeamService teamService,
                               ActivityService activityService) {
        this.registrationRepo = registrationRepo;
        this.tournamentRepo = tournamentRepo;
        this.teamService = teamService;
        this.activityService = activityService;
    }

    public ObservableList<Registration> getAll() {
        return registrationRepo.findAll();
    }

    public List<Registration> getApprovedByTournament(String tournamentId) {
        return registrationRepo.findAll().stream()
                .filter(r -> r.getTournamentId().equals(tournamentId) && r.getStatus() == RegistrationStatus.APPROVED)
                .toList();
    }

    public Registration register(Registration reg) {
        boolean duplicate = registrationRepo.findAll().stream()
                .anyMatch(r -> r.getTournamentId().equals(reg.getTournamentId())
                        && r.getTeamId().equals(reg.getTeamId())
                        && r.getStatus() != RegistrationStatus.REJECTED);
        if (duplicate) {
            throw new IllegalStateException("Đội tuyển này đã đăng ký tham gia giải đấu!");
        }
        registrationRepo.save(reg);
        activityService.log("REGISTRATION", "Đơn đăng ký mới từ đội " + teamService.getTeamNameOrTbd(reg.getTeamId()));
        return reg;
    }

    public void updateStatus(String registrationId, RegistrationStatus newStatus) {
        registrationRepo.findById(registrationId).ifPresent(reg -> {
            if (newStatus == RegistrationStatus.APPROVED) {
                int maxTeams = tournamentRepo.findById(reg.getTournamentId())
                        .map(Tournament::getMaxTeams).orElse(16);
                long approvedCount = getApprovedByTournament(reg.getTournamentId()).size();
                if (reg.getStatus() != RegistrationStatus.APPROVED && approvedCount >= maxTeams) {
                    throw new IllegalStateException("Giải đấu đã đạt tối đa " + maxTeams + " đội tham dự!");
                }
                if (reg.getSeedNumber() <= 0) {
                    reg.setSeedNumber((int) approvedCount + 1);
                }
            }
            reg.setStatus(newStatus);
            registrationRepo.save(reg);
            activityService.log("REGISTRATION",
                    "Cập nhật trạng thái đăng ký đội " + teamService.getTeamNameOrTbd(reg.getTeamId()) + " -> " + newStatus.getDisplayName());
        });
    }

    public boolean delete(String id) {
        return registrationRepo.deleteById(id);
    }

    public void updateSeedAndNotes(String registrationId, int seedNumber, String notes) {
        registrationRepo.findById(registrationId).ifPresent(reg -> {
            reg.setSeedNumber(seedNumber);
            reg.setNotes(notes);
            registrationRepo.save(reg);
            activityService.log("REGISTRATION",
                    "Cập nhật seed & ghi chú cho đội " + teamService.getTeamNameOrTbd(reg.getTeamId()));
        });
    }
}
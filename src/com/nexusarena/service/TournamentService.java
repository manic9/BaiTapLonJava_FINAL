package com.nexusarena.service;

import com.nexusarena.model.Tournament;
import com.nexusarena.repository.TournamentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TournamentService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "DRAFT", "UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"
    );

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = Objects.requireNonNull(tournamentRepository);
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Optional<Tournament> getTournamentById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return tournamentRepository.findById(id);
    }

    public List<Tournament> searchAndFilter(String keyword, String game, String status) {
        return tournamentRepository.searchAndFilter(keyword, game, status);
    }

    public Tournament createTournament(Tournament tournament) {
        validateTournament(tournament, false);
        return tournamentRepository.save(tournament);
    }

    public Tournament updateTournament(Tournament tournament) {
        validateTournament(tournament, true);

        Tournament existing = tournamentRepository.findById(tournament.getId())
                .orElseThrow(() -> new IllegalArgumentException("Giải đấu không tồn tại hoặc đã bị xóa."));

        if (tournament.getMaxTeams() < existing.getApprovedTeamsCount()) {
            throw new IllegalArgumentException(
                    "Giới hạn đội (" + tournament.getMaxTeams() + ") không thể nhỏ hơn số đội đã được duyệt ("
                            + existing.getApprovedTeamsCount() + ").");
        }

        return tournamentRepository.update(tournament);
    }

    public void changeStatus(String tournamentId, String newStatus) {
        if (tournamentId == null || tournamentId.isBlank()) {
            throw new IllegalArgumentException("Mã giải đấu không hợp lệ.");
        }
        if (newStatus == null || !VALID_STATUSES.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Trạng thái giải đấu không hợp lệ: " + newStatus);
        }
        boolean updated = tournamentRepository.updateStatus(tournamentId, newStatus.toUpperCase());
        if (!updated) {
            throw new IllegalStateException("Không thể cập nhật trạng thái cho giải đấu này.");
        }
    }

    public void deleteTournament(String tournamentId) {
        if (tournamentId == null || tournamentId.isBlank()) {
            throw new IllegalArgumentException("Mã giải đấu không hợp lệ.");
        }
        Tournament existing = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Giải đấu không tồn tại."));

        if ("ONGOING".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException(
                    "Không thể xóa giải đấu đang diễn ra (ONGOING). Hãy chuyển trạng thái sang CANCELLED hoặc COMPLETED trước.");
        }

        tournamentRepository.deleteById(tournamentId);
    }

    public long getTotalCount() {
        return tournamentRepository.countAll();
    }

    public long getOngoingCount() {
        return tournamentRepository.countByStatus("ONGOING");
    }

    private void validateTournament(Tournament t, boolean isUpdate) {
        if (t == null) {
            throw new IllegalArgumentException("Dữ liệu giải đấu không được để trống.");
        }
        if (isUpdate && (t.getId() == null || t.getId().isBlank())) {
            throw new IllegalArgumentException("Thiếu mã giải đấu để cập nhật.");
        }
        if (t.getName() == null || t.getName().isBlank()) {
            throw new IllegalArgumentException("Tên giải đấu không được để trống.");
        }
        if (t.getGame() == null || t.getGame().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn bộ môn thi đấu.");
        }
        if (t.getFormat() == null || t.getFormat().isBlank()) {
            t.setFormat("SINGLE_ELIMINATION");
        }
        if (t.getMaxTeams() < 2) {
            throw new IllegalArgumentException("Số lượng đội tham gia tối thiểu phải từ 2 đội trở lên.");
        }
        if (t.getStartDate() == null || t.getEndDate() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc.");
        }
        if (t.getEndDate().isBefore(t.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu.");
        }
        if (t.getPrizePool() != null && t.getPrizePool().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quỹ giải thưởng không được là số âm.");
        }
        if (t.getStatus() == null || t.getStatus().isBlank()) {
            t.setStatus("UPCOMING");
        } else {
            t.setStatus(t.getStatus().toUpperCase());
        }
    }
}
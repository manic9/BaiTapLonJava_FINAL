package com.nexusarena.repository;

import com.nexusarena.model.Tournament;
import java.util.List;
import java.util.Optional;

public interface TournamentRepository {
    List<Tournament> findAll();
    Optional<Tournament> findById(String id);
    List<Tournament> searchAndFilter(String keyword, String game, String status);
    Tournament save(Tournament tournament);
    Tournament update(Tournament tournament);
    boolean updateStatus(String id, String newStatus);
    boolean deleteById(String id);
    long countAll();
    long countByStatus(String status);
}
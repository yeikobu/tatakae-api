package fit.tatakae.domain.repository;

import fit.tatakae.domain.entity.TrainingSession;

import java.util.List;

public interface SessionRepository {
    List<TrainingSession> getAll();
    void save(TrainingSession session);
    void deleteAllOf(String userId);
}

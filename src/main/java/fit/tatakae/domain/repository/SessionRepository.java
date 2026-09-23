package fit.tatakae.domain.repository;

import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.TrainingSession;

import java.util.List;

public interface SessionRepository {
    List<TrainingSession> getAll();
    List<TrainingSession> findByExercise(Exercise exercise);
    void save(TrainingSession session);
    void deleteAllOf(String userId);
}

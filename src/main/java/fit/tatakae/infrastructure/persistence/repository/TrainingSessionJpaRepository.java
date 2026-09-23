package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.infrastructure.persistence.entity.TrainingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSessionJpaRepository extends JpaRepository<TrainingSessionEntity, String> {
    List<TrainingSessionEntity> findByExercise(Exercise exercise);

    void deleteByUserId(String userId);
}

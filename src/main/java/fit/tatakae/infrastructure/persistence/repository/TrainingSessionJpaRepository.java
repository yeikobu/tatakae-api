package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.infrastructure.persistence.entity.TrainingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingSessionJpaRepository extends JpaRepository<TrainingSessionEntity, String> {
    void deleteByUserId(String userId);
}

package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.infrastructure.persistence.entity.RankingAlertPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingAlertPreferenceJpaRepository extends JpaRepository<RankingAlertPreferenceEntity, String> {
}

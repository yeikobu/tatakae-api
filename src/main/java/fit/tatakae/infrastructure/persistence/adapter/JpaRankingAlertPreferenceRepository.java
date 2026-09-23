package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.domain.repository.RankingAlertPreferenceRepository;
import fit.tatakae.infrastructure.persistence.entity.RankingAlertPreferenceEntity;
import fit.tatakae.infrastructure.persistence.repository.RankingAlertPreferenceJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRankingAlertPreferenceRepository implements RankingAlertPreferenceRepository {

    private final RankingAlertPreferenceJpaRepository preferences;

    public JpaRankingAlertPreferenceRepository(RankingAlertPreferenceJpaRepository preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean isEnabled(String userId) {
        return preferences.findById(userId)
                .map(RankingAlertPreferenceEntity::isEnabled)
                .orElse(true);
    }

    @Override
    public void setEnabled(String userId, boolean enabled) {
        RankingAlertPreferenceEntity entity = preferences.findById(userId)
                .orElseGet(() -> new RankingAlertPreferenceEntity(userId, enabled));
        entity.setEnabled(enabled);
        preferences.save(entity);
    }

    @Override
    public void delete(String userId) {
        preferences.deleteById(userId);
    }
}

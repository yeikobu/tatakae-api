package fit.tatakae.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ranking_alert_preferences")
public class RankingAlertPreferenceEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected RankingAlertPreferenceEntity() {
    }

    public RankingAlertPreferenceEntity(String userId, boolean enabled) {
        this.userId = userId;
        this.enabled = enabled;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

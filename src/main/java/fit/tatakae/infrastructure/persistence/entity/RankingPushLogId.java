package fit.tatakae.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RankingPushLogId implements Serializable {

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "scope", nullable = false, length = 16)
    private String scope;

    protected RankingPushLogId() {
    }

    public RankingPushLogId(String userId, String scope) {
        this.userId = userId;
        this.scope = scope;
    }

    public String getUserId() {
        return userId;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RankingPushLogId other)) {
            return false;
        }
        return Objects.equals(userId, other.userId) && Objects.equals(scope, other.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, scope);
    }
}

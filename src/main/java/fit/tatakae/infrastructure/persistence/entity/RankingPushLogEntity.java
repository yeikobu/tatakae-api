package fit.tatakae.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ranking_push_log")
public class RankingPushLogEntity {

    @EmbeddedId
    private RankingPushLogId id;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected RankingPushLogEntity() {
    }

    public RankingPushLogEntity(RankingPushLogId id, Instant sentAt) {
        this.id = id;
        this.sentAt = sentAt;
    }

    public RankingPushLogId getId() {
        return id;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}

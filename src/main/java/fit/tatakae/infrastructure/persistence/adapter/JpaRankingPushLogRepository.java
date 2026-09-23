package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.domain.entity.RankingBoard;
import fit.tatakae.domain.repository.RankingPushLogRepository;
import fit.tatakae.infrastructure.persistence.entity.RankingPushLogEntity;
import fit.tatakae.infrastructure.persistence.entity.RankingPushLogId;
import fit.tatakae.infrastructure.persistence.repository.RankingPushLogJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public class JpaRankingPushLogRepository implements RankingPushLogRepository {

    private final RankingPushLogJpaRepository pushLog;

    public JpaRankingPushLogRepository(RankingPushLogJpaRepository pushLog) {
        this.pushLog = pushLog;
    }

    @Override
    public Optional<Instant> lastSentAt(String userId, RankingBoard board) {
        return pushLog.findById(new RankingPushLogId(userId, board.name()))
                .map(RankingPushLogEntity::getSentAt);
    }

    @Override
    public void markSent(String userId, RankingBoard board, Instant sentAt) {
        RankingPushLogId id = new RankingPushLogId(userId, board.name());
        RankingPushLogEntity entity = pushLog.findById(id)
                .orElseGet(() -> new RankingPushLogEntity(id, sentAt));
        entity.setSentAt(sentAt);
        pushLog.save(entity);
    }

    @Override
    @Transactional
    public void deleteAllOf(String userId) {
        pushLog.deleteByUserId(userId);
    }
}

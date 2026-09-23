package fit.tatakae.domain.repository;

import fit.tatakae.domain.entity.RankingBoard;

import java.time.Instant;
import java.util.Optional;

public interface RankingPushLogRepository {
    Optional<Instant> lastSentAt(String userId, RankingBoard board);

    void markSent(String userId, RankingBoard board, Instant sentAt);

    void deleteAllOf(String userId);
}

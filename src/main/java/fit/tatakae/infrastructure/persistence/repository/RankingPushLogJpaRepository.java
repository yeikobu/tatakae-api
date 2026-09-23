package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.infrastructure.persistence.entity.RankingPushLogEntity;
import fit.tatakae.infrastructure.persistence.entity.RankingPushLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingPushLogJpaRepository extends JpaRepository<RankingPushLogEntity, RankingPushLogId> {

    @Modifying
    @Query("delete from RankingPushLogEntity log where log.id.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}

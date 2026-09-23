package fit.tatakae.domain.repository;

import fit.tatakae.domain.entity.DeviceToken;

import java.time.Instant;
import java.util.List;

public interface DeviceTokenRepository {
    void upsert(String token, String userId, boolean sandbox, Instant updatedAt);

    List<DeviceToken> findByUserId(String userId);

    void delete(String token);

    void deleteAllOf(String userId);
}

package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.repository.DeviceTokenRepository;
import fit.tatakae.infrastructure.persistence.entity.DeviceTokenEntity;
import fit.tatakae.infrastructure.persistence.repository.DeviceTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public class JpaDeviceTokenRepository implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository deviceTokens;

    public JpaDeviceTokenRepository(DeviceTokenJpaRepository deviceTokens) {
        this.deviceTokens = deviceTokens;
    }

    @Override
    public void upsert(String token, String userId, boolean sandbox, Instant updatedAt) {
        DeviceTokenEntity entity = deviceTokens.findById(token)
                .orElseGet(() -> new DeviceTokenEntity(token, userId, sandbox, updatedAt));
        entity.setUserId(userId);
        entity.setSandbox(sandbox);
        entity.setUpdatedAt(updatedAt);
        deviceTokens.save(entity);
    }

    @Override
    public List<DeviceToken> findByUserId(String userId) {
        return deviceTokens.findByUserId(userId).stream()
                .map(entity -> new DeviceToken(entity.getToken(), entity.getUserId(), entity.isSandbox()))
                .toList();
    }

    @Override
    public void delete(String token) {
        deviceTokens.deleteById(token);
    }

    @Override
    @Transactional
    public void deleteAllOf(String userId) {
        deviceTokens.deleteByUserId(userId);
    }
}

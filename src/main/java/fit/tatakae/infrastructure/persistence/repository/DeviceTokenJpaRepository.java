package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.infrastructure.persistence.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenEntity, String> {
    List<DeviceTokenEntity> findByUserId(String userId);

    void deleteByUserId(String userId);
}

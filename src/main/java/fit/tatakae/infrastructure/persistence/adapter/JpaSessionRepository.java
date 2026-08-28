package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.SessionRepository;
import fit.tatakae.infrastructure.persistence.entity.UserEntity;
import fit.tatakae.infrastructure.persistence.mapper.TrainingSessionMapper;
import fit.tatakae.infrastructure.persistence.repository.TrainingSessionJpaRepository;
import fit.tatakae.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Repository
public class JpaSessionRepository implements SessionRepository {
    private final TrainingSessionJpaRepository sessionJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final Clock clock;

    public JpaSessionRepository(TrainingSessionJpaRepository sessionJpaRepository,
                                UserJpaRepository userJpaRepository,
                                Clock clock) {
        this.sessionJpaRepository = sessionJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.clock = clock;
    }

    @Override
    public List<TrainingSession> getAll() {
        return sessionJpaRepository.findAll().stream()
                .map(entity -> TrainingSessionMapper.toDomain(entity, clock))
                .toList();
    }

    @Override
    public void save(TrainingSession session) {
        String userId = session.getUser().getUserId();
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found"));
        sessionJpaRepository.save(TrainingSessionMapper.toEntity(session, user));
    }

    @Override
    @Transactional
    public void deleteAllOf(String userId) {
        sessionJpaRepository.deleteByUserId(userId);
    }
}

package fit.tatakae.infrastructure.persistence.mapper;

import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.infrastructure.persistence.entity.TrainingSessionEntity;
import fit.tatakae.infrastructure.persistence.entity.UserEntity;

import java.time.Clock;

public final class TrainingSessionMapper {

    private TrainingSessionMapper() {
    }

    public static TrainingSession toDomain(TrainingSessionEntity entity, Clock clock) {
        return new TrainingSession(
                entity.getId(),
                UserMapper.toDomain(entity.getUser()),
                entity.getExercise(),
                entity.getReps(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                clock);
    }

    public static TrainingSessionEntity toEntity(TrainingSession session, UserEntity user) {
        return new TrainingSessionEntity(
                session.getId(),
                user,
                session.getExercise(),
                session.getReps(),
                session.getStart(),
                session.getEnd());
    }
}

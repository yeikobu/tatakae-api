package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.repository.SessionRepository;

import java.time.Clock;
import java.time.Instant;

public class RecordTrainingSessionUseCase {
    private final SessionRepository sessionRepository;

    public RecordTrainingSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public TrainingSession execute(User user, Exercise exercise, int reps, Instant start, Instant end, Clock clock) {
        TrainingSession session = new TrainingSession(user, exercise, reps, start, end, clock);
        sessionRepository.save(session);
        return session;
    }
}

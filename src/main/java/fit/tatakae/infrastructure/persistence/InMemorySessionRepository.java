package fit.tatakae.infrastructure.persistence;

import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.repository.SessionRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemorySessionRepository implements SessionRepository {
    private final List<TrainingSession> sessions = new ArrayList<>();

    @Override
    public List<TrainingSession> getAll() {
        return List.copyOf(sessions);
    }

    @Override
    public void save(TrainingSession session) {
        sessions.add(session);
    }
}

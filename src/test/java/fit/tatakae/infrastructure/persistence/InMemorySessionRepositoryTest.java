package fit.tatakae.infrastructure.persistence;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemorySessionRepositoryTest {

    @Test
    public void shouldReturnEmptyListWhenNoSessionsSaved() {
        // Arrange
        InMemorySessionRepository repository = new InMemorySessionRepository();

        // Act
        List<TrainingSession> sessions = repository.getAll();

        // Assert
        assertTrue(sessions.isEmpty());
    }

    @Test
    public void shouldReturnSavedSessions() {
        // Arrange
        InMemorySessionRepository repository = new InMemorySessionRepository();
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        repository.save(session);

        // Assert
        assertEquals(List.of(session), repository.getAll());
    }

    @Test
    public void shouldNotExposeInternalMutableList() {
        // Arrange
        InMemorySessionRepository repository = new InMemorySessionRepository();
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);
        repository.save(session);

        // Act
        List<TrainingSession> sessions = repository.getAll();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> sessions.add(session));
    }

    @Test
    public void shouldDropEverySessionOfOneAthleteAndKeepTheRest() {
        // Arrange
        InMemorySessionRepository repository = new InMemorySessionRepository();
        User owner = TestUsers.user("yeikobu");
        User other = TestUsers.user("kenshin");
        Instant start = Instant.parse("2026-08-28T10:00:00Z");
        Clock clock = Clock.fixed(start, ZoneOffset.UTC);
        repository.save(new TrainingSession(owner, Exercise.PULL_UP, 20, start, start.plusSeconds(60), clock));
        repository.save(new TrainingSession(other, Exercise.PULL_UP, 30, start, start.plusSeconds(60), clock));

        // Act
        repository.deleteAllOf(owner.getUserId());

        // Assert
        assertEquals(1, repository.getAll().size());
        assertEquals(other, repository.getAll().get(0).getUser());
    }
}

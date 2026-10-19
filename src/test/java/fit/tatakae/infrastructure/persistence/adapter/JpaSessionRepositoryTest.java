package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.*;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaSessionRepository.class, JpaUserRepository.class, PostgresIntegrationTest.TestClockConfiguration.class})
public class JpaSessionRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private JpaSessionRepository sessionRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Test
    public void shouldStoreASessionAttachedToItsAthlete() {
        // Arrange
        User user = TestUsers.user("athlete_1", "cl", PrivacyLevel.PUBLIC);
        userRepository.save(user);
        TrainingSession session =
                new TrainingSession(user, Exercise.PULL_UP, 20, NOW, NOW.plusSeconds(60), CLOCK);

        // Act
        sessionRepository.save(session);
        List<TrainingSession> stored = sessionRepository.getAll();

        // Assert
        TrainingSession persisted = stored.stream()
                .filter(candidate -> candidate.getId().equals(session.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(20, persisted.getReps());
        assertEquals(Exercise.PULL_UP, persisted.getExercise());
        assertEquals(NOW, persisted.getStart());
        assertEquals(NOW.plusSeconds(60), persisted.getEnd());
        assertEquals("athlete_1", persisted.getUser().getUsername());
    }

    @Test
    public void shouldStoreASessionOfANewlyCountedExercise() {
        // Arrange
        User user = TestUsers.user("athlete_burpees", "cl", PrivacyLevel.PUBLIC);
        userRepository.save(user);
        TrainingSession session =
                new TrainingSession(user, Exercise.BURPEES, 12, NOW, NOW.plusSeconds(60), CLOCK);

        // Act
        sessionRepository.save(session);
        List<TrainingSession> stored = sessionRepository.getAll();

        // Assert
        TrainingSession persisted = stored.stream()
                .filter(candidate -> candidate.getId().equals(session.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Exercise.BURPEES, persisted.getExercise());
        assertEquals(12, persisted.getReps());
    }

    // The CHECK on training_sessions lists every value by hand, so each new one needs a migration.
    @ParameterizedTest
    @EnumSource(value = Exercise.class, names = {"KNEE_PUSH_UP", "DECLINE_PUSH_UP"})
    public void shouldStoreASessionOfAPushUpVariant(Exercise exercise) {
        // Arrange
        User user = TestUsers.user("athlete_" + exercise.name().toLowerCase(), "cl", PrivacyLevel.PUBLIC);
        userRepository.save(user);
        TrainingSession session =
                new TrainingSession(user, exercise, 15, NOW, NOW.plusSeconds(60), CLOCK);

        // Act
        sessionRepository.save(session);
        List<TrainingSession> stored = sessionRepository.findByExercise(exercise);

        // Assert
        TrainingSession persisted = stored.stream()
                .filter(candidate -> candidate.getId().equals(session.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(exercise, persisted.getExercise());
        assertEquals(15, persisted.getReps());
    }

    @Test
    public void shouldKeepPushUpVariantsOffThePushUpBoard() {
        // Arrange
        User user = TestUsers.user("trunks", "cl", PrivacyLevel.PUBLIC);
        userRepository.save(user);
        TrainingSession pushUp = new TrainingSession(user, Exercise.PUSH_UP, 10, NOW, NOW.plusSeconds(60), CLOCK);
        TrainingSession knee = new TrainingSession(user, Exercise.KNEE_PUSH_UP, 40, NOW, NOW.plusSeconds(60), CLOCK);
        TrainingSession decline = new TrainingSession(user, Exercise.DECLINE_PUSH_UP, 20, NOW, NOW.plusSeconds(60), CLOCK);
        sessionRepository.save(pushUp);
        sessionRepository.save(knee);
        sessionRepository.save(decline);

        // Act
        List<String> board = sessionRepository.findByExercise(Exercise.PUSH_UP).stream()
                .map(TrainingSession::getId)
                .toList();

        // Assert
        assertTrue(board.contains(pushUp.getId()));
        assertFalse(board.contains(knee.getId()));
        assertFalse(board.contains(decline.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenTheAthleteWasNeverStored() {
        // Arrange
        User unknown = TestUsers.user("ghost", "cl", PrivacyLevel.PUBLIC);
        TrainingSession session =
                new TrainingSession(unknown, Exercise.SQUAT, 30, NOW, NOW.plusSeconds(60), CLOCK);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> sessionRepository.save(session));
    }

    // Deleting an athlete has to be able to clear its sessions first, or the foreign key blows up.
    @Test
    public void shouldDropEverySessionOfOneAthlete() {
        // Arrange
        User owner = TestUsers.user("goten", "jp", PrivacyLevel.PUBLIC);
        User other = TestUsers.user("bra", "jp", PrivacyLevel.PUBLIC);
        userRepository.save(owner);
        userRepository.save(other);
        sessionRepository.save(new TrainingSession(owner, Exercise.PULL_UP, 20, NOW, NOW.plusSeconds(60), CLOCK));
        sessionRepository.save(new TrainingSession(other, Exercise.PULL_UP, 30, NOW, NOW.plusSeconds(60), CLOCK));

        // Act
        sessionRepository.deleteAllOf(owner.getUserId());

        // Assert
        assertTrue(sessionRepository.getAll().stream().noneMatch(s -> s.getUser().equals(owner)));
        assertTrue(sessionRepository.getAll().stream().anyMatch(s -> s.getUser().equals(other)));
    }
}

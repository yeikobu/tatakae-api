package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.*;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
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
    public void shouldThrowExceptionWhenTheAthleteWasNeverStored() {
        // Arrange
        User unknown = TestUsers.user("ghost", "cl", PrivacyLevel.PUBLIC);
        TrainingSession session =
                new TrainingSession(unknown, Exercise.SQUAT, 30, NOW, NOW.plusSeconds(60), CLOCK);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> sessionRepository.save(session));
    }
}

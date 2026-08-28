package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.FraudulentSessionException;
import fit.tatakae.domain.exception.InconsistentSessionException;
import fit.tatakae.domain.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RecordTrainingSessionUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private RecordTrainingSessionUseCase useCase;

    @Test
    public void shouldSaveSessionWhenItIsValid() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        TrainingSession session = useCase.execute(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Assert
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    public void shouldNotSaveWhenSessionIsFraudulent() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        // Assert
        assertThrows(FraudulentSessionException.class, () ->
                useCase.execute(user, Exercise.PULL_UP, 78, dateExecuted, dateExecuted.plusSeconds(60), clock));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    public void shouldNotSaveWhenSessionIsInconsistent() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        // Assert
        assertThrows(InconsistentSessionException.class, () ->
                useCase.execute(user, Exercise.PULL_UP, 0, dateExecuted, dateExecuted.plusSeconds(60), clock));
        verify(sessionRepository, never()).save(any());
    }
}

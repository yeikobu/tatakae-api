package fit.tatakae.domain.entity;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.exception.FraudulentSessionException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class TrainingSessionTest {

    @Test
    public void isTrainingSessionValid() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Exercise exercise = Exercise.PULL_UP;
        int reps = 15;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        TrainingSession trainingSession = new TrainingSession(user, exercise, reps, start, end, clock);

        //Assert
        assertEquals(user, trainingSession.getUser());
        assertEquals(exercise, trainingSession.getExercise());
        assertEquals(reps, trainingSession.getReps());
        assertEquals(start, trainingSession.getStart());
        assertEquals(end, trainingSession.getEnd());
        assertEquals(clock, trainingSession.getClock());
    }

    // Prueba que el usuario haya ejecutado una serie con repeticiones realistas
    @Test
    public void shouldTrownAndExceptionWhenRepsAreMoreThanTheMaxRepsAllowedPerMinute() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Exercise exercise = Exercise.PULL_UP;
        int reps = 78;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        // Assert
        assertThrows(FraudulentSessionException.class, () -> {
            new TrainingSession(user, exercise, reps, start, end, clock);
        });
    }

    @Test
    public void shouldAssignAUniqueIdToEachSessionWhenNotProvided() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        TrainingSession first = new TrainingSession(user, exercise, 15, start, end, clock);
        TrainingSession second = new TrainingSession(user, exercise, 15, start, end, clock);

        // Assert
        assertNotNull(first.getId());
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    public void shouldExposeTheIdItWasReconstitutedWith() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        // Act
        TrainingSession session = new TrainingSession("session-1", user, exercise, 15, start, end, clock);

        // Assert
        assertEquals("session-1", session.getId());
    }

    @Test
    public void shouldBeEqualToItself() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        boolean isEqual = session.equals(session);

        // Assert
        assertTrue(isEqual);
    }

    @Test
    public void shouldNotBeEqualToNullOrToADifferentType() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        // Assert
        assertFalse(session.equals(null));
        assertFalse(session.equals("not a session"));
    }

    @Test
    public void shouldBeEqualToAnotherSessionWithTheSameIdEvenIfAttributesDiffer() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Clock clock = Clock.fixed(Instant.parse("2026-07-22T10:00:00Z"), ZoneOffset.UTC);
        TrainingSession original = new TrainingSession("session-1", user, Exercise.PULL_UP, 15,
                Instant.parse("2026-07-22T10:00:00Z"), Instant.parse("2026-07-22T10:01:00Z"), clock);
        TrainingSession sameIdDifferentAttributes = new TrainingSession("session-1", user, Exercise.SQUAT, 30,
                Instant.parse("2026-07-22T11:00:00Z"), Instant.parse("2026-07-22T11:02:00Z"), clock);

        // Act
        // Assert
        assertEquals(original, sameIdDifferentAttributes);
        assertEquals(original.hashCode(), sameIdDifferentAttributes.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenIdsDifferEvenIfAttributesMatch() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession first = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);
        TrainingSession second = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        // Assert
        assertNotEquals(first, second);
    }

    @Test
    public void shouldConfirmWhenSessionMatchesTheQueriedExercise() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        boolean matches = session.isForExercise(Exercise.PULL_UP);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void shouldDenyWhenSessionDoesNotMatchTheQueriedExercise() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        boolean matches = session.isForExercise(Exercise.SQUAT);

        // Assert
        assertFalse(matches);
    }

    @Test
    public void shouldOutperformASessionWithFewerReps() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        TrainingSession better = new TrainingSession(user, Exercise.PULL_UP, 30, dateExecuted, dateExecuted.plusSeconds(60), clock);
        TrainingSession worse = new TrainingSession(user, Exercise.PULL_UP, 12, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Act
        // Assert
        assertTrue(better.outperforms(worse));
        assertFalse(worse.outperforms(better));
    }

    @Test
    public void shouldOutperformATiedSessionThatStartedLater() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        Instant olderStart = dateExecuted.minusSeconds(3600);
        Instant newerStart = dateExecuted.minusSeconds(600);
        TrainingSession older = new TrainingSession(user, Exercise.PULL_UP, 30, olderStart, olderStart.plusSeconds(60), clock);
        TrainingSession newer = new TrainingSession(user, Exercise.PULL_UP, 30, newerStart, newerStart.plusSeconds(60), clock);

        // Act
        // Assert
        assertTrue(older.outperforms(newer));
        assertFalse(newer.outperforms(older));
    }
}

package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.application.event.LeaderboardUpdateEvent;
import fit.tatakae.application.port.RankingPushNotifier;
import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.RankingBoard;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.service.RankingOvertake;
import fit.tatakae.domain.exception.FraudulentSessionException;
import fit.tatakae.domain.exception.InconsistentSessionException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecordTrainingSessionUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private RankingPushNotifier rankingPushNotifier;

    @InjectMocks
    private RecordTrainingSessionUseCase useCase;

    @Test
    public void shouldSaveSessionWhenItIsValid() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        Friendship friendship = new Friendship(user.getUserId(), TestUsers.idOf("friend"), clock);
        when(friendshipRepository.findAcceptedFor(user.getUserId())).thenReturn(List.of(friendship));

        // Act
        TrainingSession session = useCase.execute(user, Exercise.PULL_UP, 15, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Assert
        verify(sessionRepository, times(1)).save(session);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Collection<String>> recipientsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        ArgumentCaptor<LeaderboardUpdateEvent> eventCaptor = ArgumentCaptor.forClass(LeaderboardUpdateEvent.class);
        verify(userEventPublisher, times(2)).publishLeaderboardUpdate(recipientsCaptor.capture(), eventCaptor.capture());

        assertTrue(recipientsCaptor.getAllValues().get(0).contains(user.getUserId()));
        assertTrue(recipientsCaptor.getAllValues().get(0).contains(TestUsers.idOf("friend")));
        assertEquals("GLOBAL", eventCaptor.getAllValues().get(0).scope());
        assertEquals("FRIENDS", eventCaptor.getAllValues().get(1).scope());
        assertEquals(Exercise.PULL_UP, eventCaptor.getAllValues().get(0).exercise());
        assertEquals(15, eventCaptor.getAllValues().get(0).reps());
    }

    @Test
    public void shouldNotifyOnlyScorerWhenTheyHaveNoFriends() {
        // Arrange
        User user = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);
        when(friendshipRepository.findAcceptedFor(user.getUserId())).thenReturn(List.of());

        // Act
        useCase.execute(user, Exercise.PUSH_UP, 10, dateExecuted, dateExecuted.plusSeconds(60), clock);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Collection<String>> recipientsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(userEventPublisher, times(2)).publishLeaderboardUpdate(recipientsCaptor.capture(), any());
        assertEquals(1, recipientsCaptor.getValue().size());
        assertTrue(recipientsCaptor.getValue().contains(user.getUserId()));
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
        verify(userEventPublisher, never()).publishLeaderboardUpdate(any(), any());
        verify(rankingPushNotifier, never()).notifyOvertaken(any());
    }

    @Test
    public void shouldPushAthletesTheNewMarkPassesOnEachBoard() {
        User jacob = TestUsers.user("Jacob", "CL", PrivacyLevel.PUBLIC);
        User pal = TestUsers.user("friend", "CL", PrivacyLevel.PUBLIC);
        User stranger = TestUsers.user("stranger", "US", PrivacyLevel.PUBLIC);
        Instant start = Instant.parse("2026-07-22T10:00:00Z");
        Instant earlier = start.minusSeconds(3600);
        Clock clock = Clock.fixed(start, ZoneOffset.UTC);
        when(friendshipRepository.findAcceptedFor(jacob.getUserId()))
                .thenReturn(List.of(new Friendship(jacob.getUserId(), pal.getUserId(), clock)));
        when(sessionRepository.findByExercise(Exercise.PUSH_UP)).thenReturn(List.of(
                new TrainingSession(pal, Exercise.PUSH_UP, 10, earlier, earlier.plusSeconds(60), clock),
                new TrainingSession(stranger, Exercise.PUSH_UP, 12, earlier, earlier.plusSeconds(60), clock)
        ));

        useCase.execute(jacob, Exercise.PUSH_UP, 20, start, start.plusSeconds(60), clock);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RankingOvertake>> captor = ArgumentCaptor.forClass(List.class);
        verify(rankingPushNotifier).notifyOvertaken(captor.capture());
        List<RankingOvertake> overtakes = captor.getValue();
        assertTrue(overtakes.contains(new RankingOvertake(pal.getUserId(), RankingBoard.GLOBAL)));
        assertTrue(overtakes.contains(new RankingOvertake(pal.getUserId(), RankingBoard.COUNTRY)));
        assertTrue(overtakes.contains(new RankingOvertake(pal.getUserId(), RankingBoard.FRIENDS)));
        assertTrue(overtakes.contains(new RankingOvertake(stranger.getUserId(), RankingBoard.GLOBAL)));
        assertFalse(overtakes.contains(new RankingOvertake(stranger.getUserId(), RankingBoard.COUNTRY)));
        assertFalse(overtakes.contains(new RankingOvertake(stranger.getUserId(), RankingBoard.FRIENDS)));
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
        verify(userEventPublisher, never()).publishLeaderboardUpdate(any(), any());
    }
}

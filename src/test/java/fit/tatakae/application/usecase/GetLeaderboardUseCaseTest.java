package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetLeaderboardUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LeaderboardService leaderboardService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private GetLeaderboardUseCase useCase;

    @Test
    public void shouldDelegateGlobalRankingToLeaderboardService() {
        // Arrange
        List<TrainingSession> expected = List.of();
        when(leaderboardService.getGlobalRanking(Exercise.PULL_UP, null)).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeGlobal(Exercise.PULL_UP, null);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getGlobalRanking(Exercise.PULL_UP, null);
    }

    @Test
    public void shouldForwardTheGenderFilterOnTheGlobalRanking() {
        // Arrange
        List<TrainingSession> expected = List.of();
        when(leaderboardService.getGlobalRanking(Exercise.PULL_UP, Gender.FEMALE)).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeGlobal(Exercise.PULL_UP, Gender.FEMALE);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getGlobalRanking(Exercise.PULL_UP, Gender.FEMALE);
    }

    @Test
    public void shouldDelegateCountryRankingToLeaderboardService() {
        // Arrange
        List<TrainingSession> expected = List.of();
        when(leaderboardService.getLocalRanking(Exercise.PULL_UP, "cl", null)).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeByCountry(Exercise.PULL_UP, "cl", null);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getLocalRanking(Exercise.PULL_UP, "cl", null);
    }

    @Test
    public void shouldForwardTheGenderFilterOnTheCountryRanking() {
        // Arrange
        List<TrainingSession> expected = List.of();
        when(leaderboardService.getLocalRanking(Exercise.PULL_UP, "cl", Gender.MALE)).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeByCountry(Exercise.PULL_UP, "cl", Gender.MALE);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getLocalRanking(Exercise.PULL_UP, "cl", Gender.MALE);
    }

    @Test
    public void shouldResolveTheFriendCircleBeforeDelegatingTheFriendsRanking() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("1"), TestUsers.idOf("2"), CLOCK);
        Friendship incoming = new Friendship(TestUsers.idOf("3"), TestUsers.idOf("1"), CLOCK);
        List<TrainingSession> expected = List.of();
        when(friendshipRepository.findAcceptedFor(TestUsers.idOf("1"))).thenReturn(List.of(outgoing, incoming));
        when(leaderboardService.getFriendsRanking(
                Exercise.PULL_UP, TestUsers.idOf("1"), Set.of(TestUsers.idOf("2"), TestUsers.idOf("3")), null))
                .thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeByFriends(Exercise.PULL_UP, TestUsers.idOf("1"), null);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getFriendsRanking(
                Exercise.PULL_UP, TestUsers.idOf("1"), Set.of(TestUsers.idOf("2"), TestUsers.idOf("3")), null);
    }

    @Test
    public void shouldForwardTheGenderFilterOnTheFriendsRanking() {
        // Arrange
        when(friendshipRepository.findAcceptedFor(TestUsers.idOf("1"))).thenReturn(List.of());
        List<TrainingSession> expected = List.of();
        when(leaderboardService.getFriendsRanking(Exercise.PULL_UP, TestUsers.idOf("1"), Set.of(), Gender.FEMALE))
                .thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeByFriends(Exercise.PULL_UP, TestUsers.idOf("1"), Gender.FEMALE);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getFriendsRanking(
                Exercise.PULL_UP, TestUsers.idOf("1"), Set.of(), Gender.FEMALE);
    }
}

package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Friendship;
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
        when(leaderboardService.getGlobalRanking(Exercise.PULL_UP)).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeGlobal(Exercise.PULL_UP);

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getGlobalRanking(Exercise.PULL_UP);
    }

    @Test
    public void shouldDelegateCountryRankingToLeaderboardService() {
        // Arrange
        List<TrainingSession> expected = List.of();
        when(leaderboardService.getLocalRanking(Exercise.PULL_UP, "cl")).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeByCountry(Exercise.PULL_UP, "cl");

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getLocalRanking(Exercise.PULL_UP, "cl");
    }

    @Test
    public void shouldResolveTheFriendCircleBeforeDelegatingTheFriendsRanking() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("1"), TestUsers.idOf("2"), CLOCK);
        Friendship incoming = new Friendship(TestUsers.idOf("3"), TestUsers.idOf("1"), CLOCK);
        List<TrainingSession> expected = List.of();
        when(friendshipRepository.findAcceptedFor(TestUsers.idOf("1"))).thenReturn(List.of(outgoing, incoming));
        when(leaderboardService.getFriendsRanking(Exercise.PULL_UP, TestUsers.idOf("1"), Set.of(TestUsers.idOf("2"), TestUsers.idOf("3")))).thenReturn(expected);

        // Act
        List<TrainingSession> ranking = useCase.executeByFriends(Exercise.PULL_UP, TestUsers.idOf("1"));

        // Assert
        assertEquals(expected, ranking);
        verify(leaderboardService, times(1)).getFriendsRanking(Exercise.PULL_UP, TestUsers.idOf("1"), Set.of(TestUsers.idOf("2"), TestUsers.idOf("3")));
    }
}

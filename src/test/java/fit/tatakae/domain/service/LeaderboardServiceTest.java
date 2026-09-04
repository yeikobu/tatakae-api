package fit.tatakae.domain.service;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.repository.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Leaderboard Service")
public class LeaderboardServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    public void shouldReturnGlobalRankingOrderedByRepsDescending() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User user2 = TestUsers.user("user_2", "us", PrivacyLevel.PUBLIC);
        User user3 = TestUsers.user("user_3", "br", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession trainingSession1 = new TrainingSession(user1, exercise, 20, start, end, clock);
        TrainingSession trainingSession2 = new TrainingSession(user2, exercise, 27, start, end, clock);
        TrainingSession trainingSession3 = new TrainingSession(user3, exercise, 35, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(trainingSession1, trainingSession2, trainingSession3));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(trainingSession3, ranking.get(0));
        assertEquals(trainingSession2, ranking.get(1));
        assertEquals(trainingSession1, ranking.get(2));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldReturnLocalRanking() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User user2 = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);
        User user3 = TestUsers.user("user_3", "cl", PrivacyLevel.PUBLIC);
        User user4 = TestUsers.user("user_4", "us", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession trainingSession1 = new TrainingSession(user1, exercise, 20, start, end, clock);
        TrainingSession trainingSession2 = new TrainingSession(user2, exercise, 27, start, end, clock);
        TrainingSession trainingSession3 = new TrainingSession(user3, exercise, 35, start, end, clock);
        TrainingSession trainingSession4 = new TrainingSession(user4, exercise, 28, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(trainingSession1, trainingSession2, trainingSession3, trainingSession4));

        //Act
        List<TrainingSession> ranking = leaderboardService.getLocalRanking(exercise, "cl");

        //Assert
        assertEquals(3, ranking.size());
        assertEquals(trainingSession3, ranking.get(0));
        assertEquals(trainingSession2, ranking.get(1));
        assertEquals(trainingSession1, ranking.get(2));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldReturnOnlyPublicUsersInRanking() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User user2 = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);
        User user3 = TestUsers.user("user_3", "cl", PrivacyLevel.PRIVATE);
        User user4 = TestUsers.user("user_4", "us", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession trainingSession1 = new TrainingSession(user1, exercise, 20, start, end, clock);
        TrainingSession trainingSession2 = new TrainingSession(user2, exercise, 27, start, end, clock);
        TrainingSession trainingSession3 = new TrainingSession(user3, exercise, 35, start, end, clock);
        TrainingSession trainingSession4 = new TrainingSession(user4, exercise, 28, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(trainingSession1, trainingSession2, trainingSession3, trainingSession4));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(3, ranking.size());
        assertEquals(trainingSession4, ranking.get(0));
        assertEquals(trainingSession2, ranking.get(1));
        assertEquals(trainingSession1, ranking.get(2));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldRankOnlyTheBestSessionOfEachUser() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User user2 = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession user1WorstSession = new TrainingSession(user1, exercise, 12, start, end, clock);
        TrainingSession user1BestSession = new TrainingSession(user1, exercise, 30, start, end, clock);
        TrainingSession user2Session = new TrainingSession(user2, exercise, 25, start, end, clock);

        when(sessionRepository.getAll())
                .thenReturn(List.of(user1WorstSession, user1BestSession, user2Session));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(user1BestSession, ranking.get(0));
        assertEquals(user2Session, ranking.get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldBreakRepsTieWithTheOldestSession() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User user2 = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        Instant oldestStart = dateExecuted.minusSeconds(3600);
        Instant newestStart = dateExecuted.minusSeconds(600);

        TrainingSession oldestSession = new TrainingSession(user1, exercise, 30, oldestStart, oldestStart.plusSeconds(60), clock);
        TrainingSession newestSession = new TrainingSession(user2, exercise, 30, newestStart, newestStart.plusSeconds(60), clock);

        when(sessionRepository.getAll()).thenReturn(List.of(newestSession, oldestSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(oldestSession, ranking.get(0));
        assertEquals(newestSession, ranking.get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldIgnoreSessionsOfOtherExercisesInGlobalRanking() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User user2 = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession pullUpSession = new TrainingSession(user1, Exercise.PULL_UP, 20, start, end, clock);
        TrainingSession squatSession = new TrainingSession(user2, Exercise.SQUAT, 40, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(pullUpSession, squatSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(Exercise.PULL_UP);

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(pullUpSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldIgnorePrivateUsersAndOtherExercisesInLocalRanking() {
        //Arrange
        User publicChileanUser = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User privateChileanUser = TestUsers.user("user_2", "cl", PrivacyLevel.PRIVATE);
        User squatChileanUser = TestUsers.user("user_3", "cl", PrivacyLevel.PUBLIC);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession publicSession = new TrainingSession(publicChileanUser, Exercise.PULL_UP, 20, start, end, clock);
        TrainingSession privateSession = new TrainingSession(privateChileanUser, Exercise.PULL_UP, 50, start, end, clock);
        TrainingSession otherExerciseSession = new TrainingSession(squatChileanUser, Exercise.SQUAT, 60, start, end, clock);

        when(sessionRepository.getAll())
                .thenReturn(List.of(publicSession, privateSession, otherExerciseSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getLocalRanking(Exercise.PULL_UP, "cl");

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(publicSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldKeepBestSessionWhenItIsRegisteredFirst() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession bestSession = new TrainingSession(user1, exercise, 30, start, end, clock);
        TrainingSession worstSession = new TrainingSession(user1, exercise, 12, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(bestSession, worstSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(bestSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldKeepOldestSessionWhenSameUserTiesInRepsAndNewestIsRegisteredFirst() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        Instant oldestStart = dateExecuted.minusSeconds(3600);
        Instant newestStart = dateExecuted.minusSeconds(600);

        TrainingSession newestSession = new TrainingSession(user1, exercise, 30, newestStart, newestStart.plusSeconds(60), clock);
        TrainingSession oldestSession = new TrainingSession(user1, exercise, 30, oldestStart, oldestStart.plusSeconds(60), clock);

        when(sessionRepository.getAll()).thenReturn(List.of(newestSession, oldestSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(oldestSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldKeepOldestSessionWhenSameUserTiesInRepsAndOldestIsRegisteredFirst() {
        //Arrange
        User user1 = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        Instant oldestStart = dateExecuted.minusSeconds(3600);
        Instant newestStart = dateExecuted.minusSeconds(600);

        TrainingSession oldestSession = new TrainingSession(user1, exercise, 30, oldestStart, oldestStart.plusSeconds(60), clock);
        TrainingSession newestSession = new TrainingSession(user1, exercise, 30, newestStart, newestStart.plusSeconds(60), clock);

        when(sessionRepository.getAll()).thenReturn(List.of(oldestSession, newestSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(exercise);

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(oldestSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldRankTheUserTogetherWithItsFriendsOnly() {
        //Arrange
        User user = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User friend = TestUsers.user("user_2", "us", PrivacyLevel.PUBLIC);
        User stranger = TestUsers.user("user_3", "br", PrivacyLevel.PUBLIC);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession userSession = new TrainingSession(user, exercise, 20, start, end, clock);
        TrainingSession friendSession = new TrainingSession(friend, exercise, 35, start, end, clock);
        TrainingSession strangerSession = new TrainingSession(stranger, exercise, 50, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(userSession, friendSession, strangerSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getFriendsRanking(exercise, TestUsers.idOf("user_1"), Set.of(TestUsers.idOf("user_2")));

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(friendSession, ranking.get(0));
        assertEquals(userSession, ranking.get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    // Privacy only hides a user from strangers: friends always see each other in their shared ranking.
    @Test
    public void shouldIncludePrivateFriendsInFriendsRanking() {
        //Arrange
        User user = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User privateFriend = TestUsers.user("user_2", "cl", PrivacyLevel.PRIVATE);

        Exercise exercise = Exercise.PULL_UP;
        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession userSession = new TrainingSession(user, exercise, 20, start, end, clock);
        TrainingSession privateFriendSession = new TrainingSession(privateFriend, exercise, 35, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(userSession, privateFriendSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getFriendsRanking(exercise, TestUsers.idOf("user_1"), Set.of(TestUsers.idOf("user_2")));

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(privateFriendSession, ranking.get(0));
        assertEquals(userSession, ranking.get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldIgnoreOtherExercisesInFriendsRanking() {
        //Arrange
        User user = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC);
        User friend = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession pullUpSession = new TrainingSession(user, Exercise.PULL_UP, 20, start, end, clock);
        TrainingSession squatSession = new TrainingSession(friend, Exercise.SQUAT, 60, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(pullUpSession, squatSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getFriendsRanking(Exercise.PULL_UP, TestUsers.idOf("user_1"), Set.of(TestUsers.idOf("user_2")));

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(pullUpSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldKeepTheMixedGlobalRankingWhenNoGenderIsAsked() {
        //Arrange
        User man = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        User woman = TestUsers.user("user_2", "us", PrivacyLevel.PUBLIC, Gender.FEMALE);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession manSession = new TrainingSession(man, Exercise.PULL_UP, 20, start, end, clock);
        TrainingSession womanSession = new TrainingSession(woman, Exercise.PULL_UP, 35, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(manSession, womanSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(Exercise.PULL_UP, null);

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(womanSession, ranking.get(0));
        assertEquals(manSession, ranking.get(1));
    }

    @Test
    public void shouldReturnOnlyWomenInTheGlobalRankingWhenFilteredByFemale() {
        //Arrange
        User man = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        User woman = TestUsers.user("user_2", "us", PrivacyLevel.PUBLIC, Gender.FEMALE);
        User anotherWoman = TestUsers.user("user_3", "br", PrivacyLevel.PUBLIC, Gender.FEMALE);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession manSession = new TrainingSession(man, Exercise.PULL_UP, 50, start, end, clock);
        TrainingSession womanSession = new TrainingSession(woman, Exercise.PULL_UP, 27, start, end, clock);
        TrainingSession anotherWomanSession = new TrainingSession(anotherWoman, Exercise.PULL_UP, 35, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(manSession, womanSession, anotherWomanSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getGlobalRanking(Exercise.PULL_UP, Gender.FEMALE);

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(anotherWomanSession, ranking.get(0));
        assertEquals(womanSession, ranking.get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldCombineCountryAndFemaleFiltersAndKeepRequiringPublicAthletes() {
        //Arrange
        User chileanWoman = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC, Gender.FEMALE);
        User chileanMan = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        User americanWoman = TestUsers.user("user_3", "us", PrivacyLevel.PUBLIC, Gender.FEMALE);
        User privateChileanWoman = TestUsers.user("user_4", "cl", PrivacyLevel.PRIVATE, Gender.FEMALE);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession chileanWomanSession = new TrainingSession(chileanWoman, Exercise.PULL_UP, 20, start, end, clock);
        TrainingSession chileanManSession = new TrainingSession(chileanMan, Exercise.PULL_UP, 40, start, end, clock);
        TrainingSession americanWomanSession = new TrainingSession(americanWoman, Exercise.PULL_UP, 45, start, end, clock);
        TrainingSession privateSession = new TrainingSession(privateChileanWoman, Exercise.PULL_UP, 60, start, end, clock);

        when(sessionRepository.getAll())
                .thenReturn(List.of(chileanWomanSession, chileanManSession, americanWomanSession, privateSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getLocalRanking(Exercise.PULL_UP, "cl", Gender.FEMALE);

        //Assert
        assertEquals(1, ranking.size());
        assertEquals(chileanWomanSession, ranking.get(0));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }

    @Test
    public void shouldKeepPrivateFriendsWhenFilteringTheFriendsRankingByMale() {
        //Arrange
        User user = TestUsers.user("user_1", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        User maleFriend = TestUsers.user("user_2", "cl", PrivacyLevel.PRIVATE, Gender.MALE);
        User femaleFriend = TestUsers.user("user_3", "cl", PrivacyLevel.PUBLIC, Gender.FEMALE);

        Instant dateExecuted = Instant.parse("2026-07-22T10:00:00Z");
        Instant start = dateExecuted;
        Instant end = start.plusSeconds(60);
        Clock clock = Clock.fixed(dateExecuted, ZoneOffset.UTC);

        TrainingSession userSession = new TrainingSession(user, Exercise.PULL_UP, 20, start, end, clock);
        TrainingSession maleFriendSession = new TrainingSession(maleFriend, Exercise.PULL_UP, 35, start, end, clock);
        TrainingSession femaleFriendSession = new TrainingSession(femaleFriend, Exercise.PULL_UP, 50, start, end, clock);

        when(sessionRepository.getAll()).thenReturn(List.of(userSession, maleFriendSession, femaleFriendSession));

        //Act
        List<TrainingSession> ranking = leaderboardService.getFriendsRanking(
                Exercise.PULL_UP,
                TestUsers.idOf("user_1"),
                Set.of(TestUsers.idOf("user_2"), TestUsers.idOf("user_3")),
                Gender.MALE);

        //Assert
        assertEquals(2, ranking.size());
        assertEquals(maleFriendSession, ranking.get(0));
        assertEquals(userSession, ranking.get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).getAll();
    }
}

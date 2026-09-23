package fit.tatakae.domain.service;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.RankingBoard;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RankingOvertakeDetectorTest {

    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");
    private static final Instant EARLIER = NOW.minusSeconds(3600);

    @Test
    public void shouldNotifyGlobalAndCountryWhenAPublicCompatriotIsPassed() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of());

        assertEquals(List.of(
                new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL),
                new RankingOvertake(rival.getUserId(), RankingBoard.COUNTRY)
        ), overtakes);
    }

    @Test
    public void shouldNotifyGlobalOnlyWhenTheCountriesDiffer() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "US", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of());

        assertEquals(List.of(new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL)), overtakes);
    }

    @Test
    public void shouldNotifyFriendsEvenWhenBothAthletesArePrivate() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PRIVATE);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PRIVATE);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of(rival.getUserId()));

        assertEquals(List.of(new RankingOvertake(rival.getUserId(), RankingBoard.FRIENDS)), overtakes);
    }

    @Test
    public void shouldNotifyEveryBoardAPublicFriendFromTheSameCountrySitsOn() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of(rival.getUserId()));

        assertEquals(List.of(
                new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL),
                new RankingOvertake(rival.getUserId(), RankingBoard.COUNTRY),
                new RankingOvertake(rival.getUserId(), RankingBoard.FRIENDS)
        ), overtakes);
    }

    @Test
    public void shouldNotNotifySomeoneAlreadyBehindThePreviousBest() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User behind = TestUsers.user("behind", "CL", PrivacyLevel.PUBLIC);
        User ahead = TestUsers.user("ahead", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 50, NOW),
                List.of(
                        mark(scorer, 40, EARLIER),
                        mark(behind, 30, EARLIER),
                        mark(ahead, 45, EARLIER)
                ),
                Set.of());

        assertEquals(List.of(
                new RankingOvertake(ahead.getUserId(), RankingBoard.GLOBAL),
                new RankingOvertake(ahead.getUserId(), RankingBoard.COUNTRY)
        ), overtakes);
    }

    @Test
    public void shouldNotNotifyOnATieTheOlderSessionAlreadyWon() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 20, EARLIER)),
                Set.of());

        assertTrue(overtakes.isEmpty());
    }

    @Test
    public void shouldNotifyWhenTheNewSessionWinsAnEqualRepsTieByBeingOlder() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, EARLIER),
                List.of(mark(rival, 20, NOW)),
                Set.of());

        assertEquals(List.of(
                new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL),
                new RankingOvertake(rival.getUserId(), RankingBoard.COUNTRY)
        ), overtakes);
    }

    @Test
    public void shouldIgnoreAWeakerSessionWhenPickingTheRivalsBest() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 30, NOW),
                List.of(mark(rival, 10, EARLIER), mark(rival, 40, EARLIER.minusSeconds(60))),
                Set.of());

        assertTrue(overtakes.isEmpty());
    }

    @Test
    public void shouldNotTreatUnknownCountryAsALocalBoard() {
        User scorer = TestUsers.user("scorer", "unknown", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "unknown", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of());

        assertEquals(List.of(new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL)), overtakes);
        assertFalse(RankingOvertakeDetector.isRankedCountry(null));
        assertFalse(RankingOvertakeDetector.isRankedCountry("  "));
        assertFalse(RankingOvertakeDetector.isRankedCountry("n/a"));
    }

    @Test
    public void shouldKeepCountryComparisonCaseSensitive() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "cl", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of());

        assertEquals(List.of(new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL)), overtakes);
    }

    @Test
    public void shouldNotNotifyAPrivateAthleteOnThePublicBoards() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PRIVATE);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of());

        assertTrue(overtakes.isEmpty());
    }

    @Test
    public void shouldNotNotifyTheScorer() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(scorer, 10, EARLIER)),
                Set.of(scorer.getUserId()));

        assertTrue(overtakes.isEmpty());
    }

    @Test
    public void shouldIgnoreSessionsForAnotherExercise() {
        User scorer = TestUsers.user("scorer", "CL", PrivacyLevel.PUBLIC);
        User rival = TestUsers.user("rival", "CL", PrivacyLevel.PUBLIC);
        Clock clock = Clock.fixed(EARLIER, ZoneOffset.UTC);
        TrainingSession squat = new TrainingSession(rival, Exercise.SQUAT, 5, EARLIER, EARLIER.plusSeconds(60), clock);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(squat),
                Set.of());

        assertTrue(overtakes.isEmpty());
    }

    private static TrainingSession mark(User user, int reps, Instant start) {
        Clock clock = Clock.fixed(start, ZoneOffset.UTC);
        return new TrainingSession(user, Exercise.PUSH_UP, reps, start, start.plusSeconds(60), clock);
    }

    @Test
    public void shouldNotBuildALocalBoardWhenTheCountryIsMissing() {
        User scorer = new User(TestUsers.idOf("scorer2"), "scorer2", null, PrivacyLevel.PUBLIC, Gender.MALE);
        User rival = new User(TestUsers.idOf("rival2"), "rival2", null, PrivacyLevel.PUBLIC, Gender.MALE);

        List<RankingOvertake> overtakes = RankingOvertakeDetector.detect(
                mark(scorer, 20, NOW),
                List.of(mark(rival, 10, EARLIER)),
                Set.of());

        assertEquals(List.of(new RankingOvertake(rival.getUserId(), RankingBoard.GLOBAL)), overtakes);
    }
}

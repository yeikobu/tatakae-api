package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.GetLeaderboardUseCase;
import fit.tatakae.domain.exception.AuthenticationRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LeaderboardControllerAuthTest {

    private LeaderboardController controller;
    private GetLeaderboardUseCase getLeaderboardUseCase;

    @BeforeEach
    public void setUp() {
        getLeaderboardUseCase = mock(GetLeaderboardUseCase.class);
        controller = new LeaderboardController(getLeaderboardUseCase);
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldReturn401WhenFriendsRankingRequestedWithoutAuthentication() {
        // Arrange - no authentication set

        // Act & Assert
        assertThrows(AuthenticationRequiredException.class, () -> {
            controller.ranking(
                fit.tatakae.domain.entity.Exercise.PULL_UP,
                "FRIENDS",
                null,
                null
            );
        });

        // Verify use case was never called (401 thrown before reaching it)
        verify(getLeaderboardUseCase, never()).executeByFriends(null, null, null);
    }

    @Test
    public void shouldAllowGlobalLeaderboardWithoutAuthentication() {
        // Arrange - no authentication set

        // Act - should not throw
        try {
            controller.ranking(
                fit.tatakae.domain.entity.Exercise.PULL_UP,
                "GLOBAL",
                null,
                null
            );
        } catch (Exception e) {
            throw new AssertionError("Global leaderboard should not require authentication", e);
        }

        // Verify use case was called
        verify(getLeaderboardUseCase).executeGlobal(
            fit.tatakae.domain.entity.Exercise.PULL_UP, 
            null
        );
    }

    @Test
    public void shouldAllowCountryLeaderboardWithoutAuthentication() {
        // Arrange - no authentication set

        // Act - should not throw
        try {
            controller.ranking(
                fit.tatakae.domain.entity.Exercise.PULL_UP,
                "COUNTRY",
                "cl",
                null
            );
        } catch (Exception e) {
            throw new AssertionError("Country leaderboard should not require authentication", e);
        }

        // Verify use case was called
        verify(getLeaderboardUseCase).executeByCountry(
            fit.tatakae.domain.entity.Exercise.PULL_UP,
            "cl",
            null
        );
    }
}

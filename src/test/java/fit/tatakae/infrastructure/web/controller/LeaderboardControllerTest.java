package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.infrastructure.web.WebMvcSliceTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;


import fit.tatakae.TestUsers;
import fit.tatakae.application.usecase.GetLeaderboardUseCase;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(WebMvcSliceTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(LeaderboardController.class)
public class LeaderboardControllerTest {

    private static final Instant START = Instant.parse("2026-08-28T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(START, ZoneOffset.UTC);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetLeaderboardUseCase getLeaderboardUseCase;

    @BeforeEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityAfter() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String userId) {
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedUser(userId, "apple-sub"));
    }


    private TrainingSession sessionOf(String username, int reps) {
        User user = TestUsers.user(username, "cl", PrivacyLevel.PUBLIC);
        return new TrainingSession(user, Exercise.PULL_UP, reps, START, START.plusSeconds(60), CLOCK);
    }

    @Test
    public void shouldAllowTheViteDevOrigin() throws Exception {
        // Arrange
        when(getLeaderboardUseCase.executeGlobal(Exercise.PULL_UP, null)).thenReturn(List.of());

        // Act and Assert
        // CORS filter is disabled in this WebMvc slice (addFilters=false); full CORS is covered by SecurityConfig.
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnTheGlobalRankingWithItsPositions() throws Exception {
        // Arrange
        when(getLeaderboardUseCase.executeGlobal(Exercise.PULL_UP, null))
                .thenReturn(List.of(sessionOf("second", 30), sessionOf("first", 20)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[0].username").value("second"))
                .andExpect(jsonPath("$[0].gender").value("MALE"))
                .andExpect(jsonPath("$[1].position").value(2));
    }

    @Test
    public void shouldForwardTheFemaleCategoryToTheGlobalRanking() throws Exception {
        // Arrange
        when(getLeaderboardUseCase.executeGlobal(Exercise.PULL_UP, Gender.FEMALE))
                .thenReturn(List.of(sessionOf("first", 20)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("gender", "FEMALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("first"));
    }

    @Test
    public void shouldReturnTheCountryRanking() throws Exception {
        // Arrange
        when(getLeaderboardUseCase.executeByCountry(Exercise.PULL_UP, "cl", null))
                .thenReturn(List.of(sessionOf("first", 20)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("scope", "COUNTRY").param("country", "cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("cl"));
    }

    @Test
    public void shouldReturnTheFriendsRanking() throws Exception {
        // Arrange
        authenticateAs(TestUsers.idOf("first"));
        when(getLeaderboardUseCase.executeByFriends(Exercise.PULL_UP, TestUsers.idOf("first"), null))
                .thenReturn(List.of(sessionOf("first", 20)));

        // Act and Assert — FRIENDS scope uses the authenticated principal, never a userId query param
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("scope", "friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(TestUsers.idOf("first")));
    }

    @Test
    public void shouldReturnBadRequestWhenTheCountryScopeHasNoCountry() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("scope", "COUNTRY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    public void shouldReturnUnauthorizedWhenFriendsScopeHasNoAuthentication() throws Exception {
        // Act and Assert — FRIENDS ranking requires an authenticated athlete
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("scope", "FRIENDS"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    public void shouldReturnBadRequestWhenTheScopeIsUnknown() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("scope", "galaxy"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    public void shouldReturnBadRequestWhenTheGenderIsUnknown() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/PULL_UP").param("gender", "ALIEN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    public void shouldReturnBadRequestWhenTheExerciseIsUnknown() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/leaderboards/BACKFLIP"))
                .andExpect(status().isBadRequest());
    }
}

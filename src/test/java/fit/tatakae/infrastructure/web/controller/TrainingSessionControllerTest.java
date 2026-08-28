package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.TestUsers;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.application.usecase.RecordTrainingSessionUseCase;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.FraudulentSessionException;
import fit.tatakae.domain.exception.InconsistentSessionException;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.infrastructure.web.dto.CreateTrainingSessionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingSessionController.class)
public class TrainingSessionControllerTest {

    private static final Instant START = Instant.parse("2026-08-28T10:00:00Z");
    private static final Instant END = START.plusSeconds(60);
    private static final Clock CLOCK = Clock.fixed(START, ZoneOffset.UTC);

    @TestConfiguration
    static class ClockConfiguration {
        @Bean
        Clock clock() {
            return CLOCK;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordTrainingSessionUseCase recordTrainingSessionUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @Test
    public void shouldRecordASessionAndReturnCreated() throws Exception {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 20, START, END, CLOCK);
        when(getUserUseCase.execute("user_1")).thenReturn(user);
        when(recordTrainingSessionUseCase.execute(eq(user), eq(Exercise.PULL_UP), eq(20), eq(START), eq(END), any()))
                .thenReturn(session);
        CreateTrainingSessionRequest request =
                new CreateTrainingSessionRequest("user_1", Exercise.PULL_UP, 20, START, END);

        // Act and Assert
        mockMvc.perform(post("/api/v1/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("yeikobu"))
                .andExpect(jsonPath("$.reps").value(20));
    }

    @Test
    public void shouldReturnNotFoundWhenTheAthleteDoesNotExist() throws Exception {
        // Arrange
        when(getUserUseCase.execute("ghost")).thenThrow(new ResourceNotFoundException("User ghost was not found"));
        CreateTrainingSessionRequest request =
                new CreateTrainingSessionRequest("ghost", Exercise.PULL_UP, 20, START, END);

        // Act and Assert
        mockMvc.perform(post("/api/v1/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenTheSetLooksFraudulent() throws Exception {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);
        when(getUserUseCase.execute("user_1")).thenReturn(user);
        when(recordTrainingSessionUseCase.execute(any(), any(), anyInt(), any(), any(), any()))
                .thenThrow(new FraudulentSessionException("Registered reps are not humanly possible"));
        CreateTrainingSessionRequest request =
                new CreateTrainingSessionRequest("user_1", Exercise.PULL_UP, 76, START, END);

        // Act and Assert
        mockMvc.perform(post("/api/v1/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    public void shouldReturnBadRequestWhenTheTimeframeIsInconsistent() throws Exception {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);
        when(getUserUseCase.execute("user_1")).thenReturn(user);
        when(recordTrainingSessionUseCase.execute(any(), any(), anyInt(), any(), any(), any()))
                .thenThrow(new InconsistentSessionException("End time must be after start time"));
        CreateTrainingSessionRequest request =
                new CreateTrainingSessionRequest("user_1", Exercise.PULL_UP, 20, END, START);

        // Act and Assert
        mockMvc.perform(post("/api/v1/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    public void shouldReturnBadRequestWhenRepsAreNotPositive() throws Exception {
        // Arrange
        CreateTrainingSessionRequest request =
                new CreateTrainingSessionRequest("user_1", Exercise.PULL_UP, 0, START, END);

        // Act and Assert
        mockMvc.perform(post("/api/v1/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("reps: reps must be greater than 0"));
    }
}

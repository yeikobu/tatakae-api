package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.TestUsers;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.tatakae.application.usecase.GetFriendshipUseCase;
import fit.tatakae.application.usecase.RemoveFriendshipUseCase;
import fit.tatakae.application.usecase.RespondFriendRequestUseCase;
import fit.tatakae.application.usecase.SendFriendRequestUseCase;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.DuplicateFriendshipException;
import fit.tatakae.domain.exception.InvalidFriendshipTransitionException;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.exception.SelfFriendshipException;
import fit.tatakae.infrastructure.web.dto.CreateFriendshipRequest;
import fit.tatakae.infrastructure.web.dto.UpdateFriendshipRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendshipController.class)
public class FriendshipControllerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SendFriendRequestUseCase sendFriendRequestUseCase;
    @MockitoBean
    private RespondFriendRequestUseCase respondFriendRequestUseCase;
    @MockitoBean
    private GetFriendshipUseCase getFriendshipUseCase;
    @MockitoBean
    private RemoveFriendshipUseCase removeFriendshipUseCase;

    @Test
    public void shouldCreateAPendingRequestAndReturnCreated() throws Exception {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(sendFriendRequestUseCase.execute("user_1", "user_2")).thenReturn(pending);
        CreateFriendshipRequest request = new CreateFriendshipRequest("user_1", "user_2");

        // Act and Assert
        mockMvc.perform(post("/api/v1/friendships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/friendships/" + pending.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.respondedAt").doesNotExist());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenAUserBefriendsItself() throws Exception {
        // Arrange
        when(sendFriendRequestUseCase.execute(anyString(), anyString()))
                .thenThrow(new SelfFriendshipException("A user cannot befriend itself"));
        CreateFriendshipRequest request = new CreateFriendshipRequest("user_1", "user_1");

        // Act and Assert
        mockMvc.perform(post("/api/v1/friendships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    public void shouldReturnConflictWhenTheRelationAlreadyExists() throws Exception {
        // Arrange
        when(sendFriendRequestUseCase.execute(anyString(), anyString()))
                .thenThrow(new DuplicateFriendshipException("A friendship already exists"));
        CreateFriendshipRequest request = new CreateFriendshipRequest("user_1", "user_2");

        // Act and Assert
        mockMvc.perform(post("/api/v1/friendships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    public void shouldReturnNotFoundWhenOneAthleteDoesNotExist() throws Exception {
        // Arrange
        when(sendFriendRequestUseCase.execute(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("User ghost was not found"));
        CreateFriendshipRequest request = new CreateFriendshipRequest("ghost", "user_2");

        // Act and Assert
        mockMvc.perform(post("/api/v1/friendships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    public void shouldReturnBadRequestWhenTheRequesterIsMissing() throws Exception {
        // Arrange
        CreateFriendshipRequest request = new CreateFriendshipRequest("", "user_2");

        // Act and Assert
        mockMvc.perform(post("/api/v1/friendships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("requesterId: requesterId is required"));
    }

    @Test
    public void shouldReturnOneFriendship() throws Exception {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(getFriendshipUseCase.execute(pending.getId())).thenReturn(pending);

        // Act and Assert
        mockMvc.perform(get("/api/v1/friendships/" + pending.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requesterId").value(TestUsers.idOf("user_1")));
    }

    @Test
    public void shouldAcceptAPendingRequest() throws Exception {
        // Arrange
        Friendship accepted = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        accepted.accept();
        when(respondFriendRequestUseCase.accept(accepted.getId())).thenReturn(accepted);
        UpdateFriendshipRequest request =
                new UpdateFriendshipRequest(UpdateFriendshipRequest.FriendshipAnswer.ACCEPTED);

        // Act and Assert
        mockMvc.perform(patch("/api/v1/friendships/" + accepted.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        verify(respondFriendRequestUseCase, never()).reject(anyString());
    }

    @Test
    public void shouldRejectAPendingRequest() throws Exception {
        // Arrange
        Friendship rejected = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        rejected.reject();
        when(respondFriendRequestUseCase.reject(rejected.getId())).thenReturn(rejected);
        UpdateFriendshipRequest request =
                new UpdateFriendshipRequest(UpdateFriendshipRequest.FriendshipAnswer.REJECTED);

        // Act and Assert
        mockMvc.perform(patch("/api/v1/friendships/" + rejected.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
        verify(respondFriendRequestUseCase, never()).accept(anyString());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenTheRequestWasAlreadyAnswered() throws Exception {
        // Arrange
        when(respondFriendRequestUseCase.accept(anyString()))
                .thenThrow(new InvalidFriendshipTransitionException("Only a pending request can be accepted"));
        UpdateFriendshipRequest request =
                new UpdateFriendshipRequest(UpdateFriendshipRequest.FriendshipAnswer.ACCEPTED);

        // Act and Assert
        mockMvc.perform(patch("/api/v1/friendships/friendship-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    public void shouldRemoveAFriendship() throws Exception {
        // Act and Assert
        mockMvc.perform(delete("/api/v1/friendships/friendship-1"))
                .andExpect(status().isNoContent());
        verify(removeFriendshipUseCase, times(1)).execute("friendship-1");
    }

    @Test
    public void shouldReturnNotFoundWhenRemovingAnUnknownFriendship() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Friendship ghost was not found"))
                .when(removeFriendshipUseCase).execute("ghost");

        // Act and Assert
        mockMvc.perform(delete("/api/v1/friendships/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/v1/friendships/ghost"));
    }

    @Test
    public void shouldReturnMethodNotAllowedWhenTheVerbIsNotSupported() throws Exception {
        // Act and Assert
        mockMvc.perform(put("/api/v1/friendships/friendship-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }
}

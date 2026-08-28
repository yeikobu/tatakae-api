package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.TestUsers;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.tatakae.application.usecase.*;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.DuplicateUserException;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.infrastructure.web.dto.CreateUserRequest;
import fit.tatakae.infrastructure.web.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private GetUserUseCase getUserUseCase;
    @MockitoBean
    private ListUsersUseCase listUsersUseCase;
    @MockitoBean
    private FindUserByUsernameUseCase findUserByUsernameUseCase;
    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;
    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;
    @MockitoBean
    private ListFriendsUseCase listFriendsUseCase;
    @MockitoBean
    private ListFriendRequestsUseCase listFriendRequestsUseCase;

    @Test
    public void shouldRegisterAUserAndReturnCreated() throws Exception {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);
        when(registerUserUseCase.execute("yeikobu", "cl", PrivacyLevel.PUBLIC)).thenReturn(user);
        CreateUserRequest request = new CreateUserRequest("yeikobu", "cl", PrivacyLevel.PUBLIC);

        // Act and Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/" + TestUsers.idOf("yeikobu")))
                .andExpect(jsonPath("$.userId").value(TestUsers.idOf("yeikobu")))
                .andExpect(jsonPath("$.username").value("yeikobu"));
    }

    @Test
    public void shouldReturnBadRequestWithFieldDetailWhenPayloadIsInvalid() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("jacob aguilar", "cl", PrivacyLevel.PUBLIC);

        // Act and Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0]")
                        .value("username: username must use only letters, digits, dots and underscores"));
        verify(registerUserUseCase, never()).execute(anyString(), anyString(), any());
    }

    @Test
    public void shouldReturnConflictWhenUsernameIsAlreadyTaken() throws Exception {
        // Arrange
        when(registerUserUseCase.execute(anyString(), anyString(), any()))
                .thenThrow(new DuplicateUserException("Username yeikobu is already taken"));
        CreateUserRequest request = new CreateUserRequest("yeikobu", "cl", PrivacyLevel.PUBLIC);

        // Act and Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/users"));
    }

    @Test
    public void shouldReturnBadRequestWhenTheBodyIsMalformed() throws Exception {
        // Act and Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"yeikobu\", \"privacyLevel\": \"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    public void shouldListEveryUser() throws Exception {
        // Arrange
        when(listUsersUseCase.execute()).thenReturn(List.of(TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("yeikobu"));
    }

    @Test
    public void shouldReturnOneUser() throws Exception {
        // Arrange
        when(getUserUseCase.execute("user_1")).thenReturn(TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users/user_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.privacyLevel").value("PUBLIC"));
    }

    @Test
    public void shouldReturnNotFoundWhenTheUserDoesNotExist() throws Exception {
        // Arrange
        when(getUserUseCase.execute("ghost")).thenThrow(new ResourceNotFoundException("User ghost was not found"));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User ghost was not found"));
    }

    @Test
    public void shouldUpdateAUser() throws Exception {
        // Arrange
        when(updateUserUseCase.execute(TestUsers.idOf("yeikobu"), "kenshin", "us", PrivacyLevel.PRIVATE))
                .thenReturn(new User(TestUsers.idOf("yeikobu"), "kenshin", "us", PrivacyLevel.PRIVATE));
        UpdateUserRequest request = new UpdateUserRequest("kenshin", "us", PrivacyLevel.PRIVATE);

        // Act and Assert
        mockMvc.perform(put("/api/v1/users/" + TestUsers.idOf("yeikobu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("us"));
    }

    @Test
    public void shouldDeleteAUser() throws Exception {
        // Act and Assert
        mockMvc.perform(delete("/api/v1/users/user_1"))
                .andExpect(status().isNoContent());
        verify(deleteUserUseCase, times(1)).execute("user_1");
    }

    @Test
    public void shouldListTheFriendsOfAUser() throws Exception {
        // Arrange
        when(listFriendsUseCase.execute("user_1"))
                .thenReturn(List.of(TestUsers.user("friend", "cl", PrivacyLevel.PUBLIC)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users/user_1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("friend"));
    }

    @Test
    public void shouldListIncomingRequestsByDefault() throws Exception {
        // Arrange
        when(listFriendRequestsUseCase.execute("user_1", FriendRequestDirection.INCOMING))
                .thenReturn(List.of(new Friendship(TestUsers.idOf("user_2"), TestUsers.idOf("user_1"), CLOCK)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users/user_1/friend-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].requesterId").value(TestUsers.idOf("user_2")));
    }

    @Test
    public void shouldListOutgoingRequestsWhenAsked() throws Exception {
        // Arrange
        when(listFriendRequestsUseCase.execute("user_1", FriendRequestDirection.OUTGOING))
                .thenReturn(List.of(new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK)));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users/user_1/friend-requests").param("direction", "outgoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addresseeId").value(TestUsers.idOf("user_2")));
    }

    @Test
    public void shouldReturnBadRequestWhenTheDirectionIsUnknown() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/users/user_1/friend-requests").param("direction", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    public void shouldReturnInternalServerErrorWithoutLeakingTheStackTrace() throws Exception {
        // Arrange
        when(getUserUseCase.execute("boom")).thenThrow(new IllegalStateException("connection pool exhausted"));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred while processing the request"));
    }

    // Resolving a handle into an identity is what lets a client start from a name it can actually type.
    @Test
    public void shouldResolveAHandleIntoItsAthlete() throws Exception {
        // Arrange
        when(findUserByUsernameUseCase.execute("yeikobu")).thenReturn(TestUsers.user("yeikobu"));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users").param("username", "yeikobu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(TestUsers.idOf("yeikobu")))
                .andExpect(jsonPath("$[0].username").value("yeikobu"));
        verify(listUsersUseCase, never()).execute();
    }

    @Test
    public void shouldReturnNotFoundWhenNoAthleteOwnsTheHandle() throws Exception {
        // Arrange
        when(findUserByUsernameUseCase.execute("nobody"))
                .thenThrow(new ResourceNotFoundException("Username nobody was not found"));

        // Act and Assert
        mockMvc.perform(get("/api/v1/users").param("username", "nobody"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    public void shouldReturnConflictWhenTheNewHandleBelongsToAnotherAthlete() throws Exception {
        // Arrange
        when(updateUserUseCase.execute(anyString(), anyString(), anyString(), any()))
                .thenThrow(new DuplicateUserException("Username kenshin is already taken"));
        UpdateUserRequest request = new UpdateUserRequest("kenshin", "cl", PrivacyLevel.PUBLIC);

        // Act and Assert
        mockMvc.perform(put("/api/v1/users/" + TestUsers.idOf("yeikobu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));
    }
}

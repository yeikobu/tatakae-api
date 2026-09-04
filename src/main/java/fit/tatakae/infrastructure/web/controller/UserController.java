package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.*;
import fit.tatakae.infrastructure.web.dto.CreateUserRequest;
import fit.tatakae.infrastructure.web.dto.FriendshipResponse;
import fit.tatakae.infrastructure.web.dto.UpdateUserRequest;
import fit.tatakae.infrastructure.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
// Vite (:5173) and this service (:8080) are different origins. Without the header
// the browser drops the JSON; a Vite proxy would hide that from the course rubric.
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@Tag(name = "Users", description = "Athletes registered in Tatakae")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final FindUserByUsernameUseCase findUserByUsernameUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ListFriendsUseCase listFriendsUseCase;
    private final ListFriendRequestsUseCase listFriendRequestsUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          GetUserUseCase getUserUseCase,
                          ListUsersUseCase listUsersUseCase,
                          FindUserByUsernameUseCase findUserByUsernameUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          ListFriendsUseCase listFriendsUseCase,
                          ListFriendRequestsUseCase listFriendRequestsUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.findUserByUsernameUseCase = findUserByUsernameUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.listFriendsUseCase = listFriendsUseCase;
        this.listFriendRequestsUseCase = listFriendRequestsUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a new athlete")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Athlete registered"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "409", description = "Handle already taken")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse body = UserResponse.from(registerUserUseCase.execute(
                request.username(), request.country(), request.privacyLevel(), request.gender()));
        return ResponseEntity.created(URI.create("/api/v1/users/" + body.userId())).body(body);
    }

    @GetMapping
    @Operation(summary = "List athletes, optionally resolving one public handle into its identity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athletes returned"),
            @ApiResponse(responseCode = "400", description = "Malformed handle"),
            @ApiResponse(responseCode = "404", description = "No athlete owns that handle")
    })
    public List<UserResponse> list(
            @Parameter(description = "Public handle to resolve into an athlete identity", example = "yeikobu")
            @RequestParam(required = false) String username) {
        if (username != null) {
            return List.of(UserResponse.from(findUserByUsernameUseCase.execute(username)));
        }
        return listUsersUseCase.execute().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get one athlete by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlete found"),
            @ApiResponse(responseCode = "404", description = "Athlete not found")
    })
    public UserResponse get(@PathVariable String userId) {
        return UserResponse.from(getUserUseCase.execute(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update an athlete profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Athlete updated"),
            @ApiResponse(responseCode = "404", description = "Athlete not found"),
            @ApiResponse(responseCode = "409", description = "Handle already taken by another athlete")
    })
    public UserResponse update(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(updateUserUseCase.execute(
                userId, request.username(), request.country(), request.privacyLevel(), request.gender()));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an athlete")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Athlete deleted"),
            @ApiResponse(responseCode = "404", description = "Athlete not found")
    })
    public void delete(@PathVariable String userId) {
        deleteUserUseCase.execute(userId);
    }

    @GetMapping("/{userId}/friends")
    @Operation(summary = "List the accepted friends of an athlete")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friends returned"),
            @ApiResponse(responseCode = "404", description = "Athlete not found")
    })
    public List<UserResponse> friends(@PathVariable String userId) {
        return listFriendsUseCase.execute(userId).stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{userId}/friend-requests")
    @Operation(summary = "List the pending friend requests of an athlete")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requests returned"),
            @ApiResponse(responseCode = "400", description = "Unknown direction"),
            @ApiResponse(responseCode = "404", description = "Athlete not found")
    })
    public List<FriendshipResponse> friendRequests(
            @PathVariable String userId,
            @Parameter(description = "incoming for requests waiting for this athlete, outgoing for the ones it sent")
            @RequestParam(defaultValue = "incoming") String direction) {
        return listFriendRequestsUseCase.execute(userId, parseDirection(direction)).stream()
                .map(FriendshipResponse::from)
                .toList();
    }

    private FriendRequestDirection parseDirection(String direction) {
        try {
            return FriendRequestDirection.valueOf(direction.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown direction " + direction + ", expected incoming or outgoing");
        }
    }
}

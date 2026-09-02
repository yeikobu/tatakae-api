package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.GetFriendshipUseCase;
import fit.tatakae.application.usecase.RemoveFriendshipUseCase;
import fit.tatakae.application.usecase.RespondFriendRequestUseCase;
import fit.tatakae.application.usecase.SendFriendRequestUseCase;
import fit.tatakae.infrastructure.web.dto.CreateFriendshipRequest;
import fit.tatakae.infrastructure.web.dto.FriendshipResponse;
import fit.tatakae.infrastructure.web.dto.UpdateFriendshipRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/friendships")
// PATCH with JSON is a CORS preflight. Naming the Vite origin is what lets the browser send it.
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@Tag(name = "Friendships", description = "Friend requests and friendships between athletes")
public class FriendshipController {

    private final SendFriendRequestUseCase sendFriendRequestUseCase;
    private final RespondFriendRequestUseCase respondFriendRequestUseCase;
    private final GetFriendshipUseCase getFriendshipUseCase;
    private final RemoveFriendshipUseCase removeFriendshipUseCase;

    public FriendshipController(SendFriendRequestUseCase sendFriendRequestUseCase,
                                RespondFriendRequestUseCase respondFriendRequestUseCase,
                                GetFriendshipUseCase getFriendshipUseCase,
                                RemoveFriendshipUseCase removeFriendshipUseCase) {
        this.sendFriendRequestUseCase = sendFriendRequestUseCase;
        this.respondFriendRequestUseCase = respondFriendRequestUseCase;
        this.getFriendshipUseCase = getFriendshipUseCase;
        this.removeFriendshipUseCase = removeFriendshipUseCase;
    }

    @PostMapping
    @Operation(summary = "Send a friend request")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Request created and left pending"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "404", description = "One of the athletes does not exist"),
            @ApiResponse(responseCode = "409", description = "A relation between both athletes already exists"),
            @ApiResponse(responseCode = "422", description = "An athlete cannot befriend itself")
    })
    public ResponseEntity<FriendshipResponse> send(@Valid @RequestBody CreateFriendshipRequest request) {
        FriendshipResponse body = FriendshipResponse.from(
                sendFriendRequestUseCase.execute(request.requesterId(), request.addresseeId()));
        return ResponseEntity.created(URI.create("/api/v1/friendships/" + body.id())).body(body);
    }

    @GetMapping("/{friendshipId}")
    @Operation(summary = "Get one friendship by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friendship found"),
            @ApiResponse(responseCode = "404", description = "Friendship not found")
    })
    public FriendshipResponse get(@PathVariable String friendshipId) {
        return FriendshipResponse.from(getFriendshipUseCase.execute(friendshipId));
    }

    @PatchMapping("/{friendshipId}")
    @Operation(summary = "Answer a pending friend request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request answered"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "404", description = "Friendship not found"),
            @ApiResponse(responseCode = "422", description = "The request was already answered")
    })
    public FriendshipResponse answer(@PathVariable String friendshipId,
                                     @Valid @RequestBody UpdateFriendshipRequest request) {
        return FriendshipResponse.from(
                request.status() == UpdateFriendshipRequest.FriendshipAnswer.ACCEPTED
                        ? respondFriendRequestUseCase.accept(friendshipId)
                        : respondFriendRequestUseCase.reject(friendshipId));
    }

    @DeleteMapping("/{friendshipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a friendship or cancel a pending request")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Friendship removed"),
            @ApiResponse(responseCode = "404", description = "Friendship not found")
    })
    public void remove(@PathVariable String friendshipId) {
        removeFriendshipUseCase.execute(friendshipId);
    }
}

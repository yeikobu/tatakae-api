package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.infrastructure.sse.SseHub;
import fit.tatakae.infrastructure.web.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Server-Sent Events stream for real-time friend and leaderboard updates")
public class EventsController {

    private final SseHub sseHub;

    public EventsController(SseHub sseHub) {
        this.sseHub = sseHub;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Open an authenticated SSE stream",
            description = """
                    Requires a valid Apple identity JWT via Authorization: Bearer <token>.
                    For browser EventSource (which cannot set Authorization), pass the same JWT as
                    ?access_token=<token> on this endpoint only. iOS should prefer URLSession with
                    the Authorization header. Keep-alive comments are sent about every 20s; the
                    connection times out after 30 minutes — reconnect with last-event-id optional.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "text/event-stream opened"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public SseEmitter subscribe() {
        String userId = SecurityContextHelper.getAuthenticatedUserId();
        return sseHub.subscribe(userId);
    }
}

package fit.tatakae.infrastructure.web.dto.sse;

import fit.tatakae.application.event.LeaderboardUpdateEvent;
import fit.tatakae.domain.entity.Exercise;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LeaderboardUpdateSsePayload",
        description = "Lean SSE hint that a leaderboard for an exercise/scope should be refreshed")
public record LeaderboardUpdateSsePayload(
        @Schema(example = "PULL_UP") Exercise exercise,
        @Schema(example = "GLOBAL", allowableValues = {"GLOBAL", "FRIENDS", "COUNTRY"}) String scope,
        @Schema(example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3") String userId,
        @Schema(example = "yeikobu") String username,
        @Schema(example = "15") int reps,
        @Schema(example = "cl", nullable = true) String country) {

    public static LeaderboardUpdateSsePayload from(LeaderboardUpdateEvent event) {
        return new LeaderboardUpdateSsePayload(
                event.exercise(),
                event.scope(),
                event.user().getUserId(),
                event.user().getUsername(),
                event.reps(),
                event.user().getCountry());
    }
}

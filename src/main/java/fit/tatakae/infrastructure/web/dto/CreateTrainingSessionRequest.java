package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Exercise;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

@Schema(name = "CreateTrainingSessionRequest", description = "Set counted by the app rep detector")
public record CreateTrainingSessionRequest(

        @Schema(description = "Identity of the athlete who trained",
                example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3")
        @NotBlank(message = "userId is required")
        String userId,

        @Schema(example = "PULL_UP")
        @NotNull(message = "exercise is required")
        Exercise exercise,

        @Schema(example = "20")
        @Positive(message = "reps must be greater than 0")
        int reps,

        @Schema(example = "2026-08-28T10:00:00Z")
        @NotNull(message = "start is required")
        Instant start,

        @Schema(example = "2026-08-28T10:01:00Z")
        @NotNull(message = "end is required")
        Instant end) {
}

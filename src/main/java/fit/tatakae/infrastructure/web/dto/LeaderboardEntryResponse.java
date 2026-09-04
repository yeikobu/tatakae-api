package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.TrainingSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "LeaderboardEntryResponse", description = "One row of a ranking")
public record LeaderboardEntryResponse(

        @Schema(description = "Position in the ranking, starting at 1", example = "1") int position,
        String userId,
        String username,
        String country,
        Gender gender,
        int reps,
        Instant achievedAt) {

    public static LeaderboardEntryResponse from(int position, TrainingSession session) {
        return new LeaderboardEntryResponse(
                position,
                session.getUser().getUserId(),
                session.getUser().getUsername(),
                session.getUser().getCountry(),
                session.getUser().getGender(),
                session.getReps(),
                session.getStart());
    }
}

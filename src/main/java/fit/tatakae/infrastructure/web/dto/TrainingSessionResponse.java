package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.TrainingSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "TrainingSessionResponse", description = "Stored training session")
public record TrainingSessionResponse(

        String id,
        String userId,
        String username,
        Exercise exercise,
        int reps,
        Instant start,
        Instant end) {

    public static TrainingSessionResponse from(TrainingSession session) {
        return new TrainingSessionResponse(
                session.getId(),
                session.getUser().getUserId(),
                session.getUser().getUsername(),
                session.getExercise(),
                session.getReps(),
                session.getStart(),
                session.getEnd());
    }
}

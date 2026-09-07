package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.application.usecase.RecordTrainingSessionUseCase;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.CreateTrainingSessionRequest;
import fit.tatakae.infrastructure.web.dto.TrainingSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Clock;

@RestController
@RequestMapping("/api/v1/training-sessions")
@Tag(name = "Training sessions", description = "Sets counted by the on device rep detector")
public class TrainingSessionController {

    private final RecordTrainingSessionUseCase recordTrainingSessionUseCase;
    private final GetUserUseCase getUserUseCase;
    private final Clock clock;

    public TrainingSessionController(RecordTrainingSessionUseCase recordTrainingSessionUseCase,
                                     GetUserUseCase getUserUseCase,
                                     Clock clock) {
        this.recordTrainingSessionUseCase = recordTrainingSessionUseCase;
        this.getUserUseCase = getUserUseCase;
        this.clock = clock;
    }

    @PostMapping
    @Operation(summary = "Record a training session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or inconsistent timeframe"),
            @ApiResponse(responseCode = "404", description = "Athlete not found"),
            @ApiResponse(responseCode = "422", description = "The set exceeds the reps humanly possible for the exercise")
    })
    public ResponseEntity<TrainingSessionResponse> record(@Valid @RequestBody CreateTrainingSessionRequest request) {
        User user = getUserUseCase.execute(request.userId());
        TrainingSessionResponse body = TrainingSessionResponse.from(recordTrainingSessionUseCase.execute(
                user, request.exercise(), request.reps(), request.start(), request.end(), clock));
        return ResponseEntity.created(URI.create("/api/v1/training-sessions/" + body.id())).body(body);
    }
}

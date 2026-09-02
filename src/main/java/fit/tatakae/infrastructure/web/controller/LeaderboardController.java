package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.GetLeaderboardUseCase;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.infrastructure.web.dto.LeaderboardEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/leaderboards")
// First call the SPA makes. If this origin is missing, the console stays red and the ranking never paints.
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@Tag(name = "Leaderboards", description = "Global, local and friends rankings per exercise")
public class LeaderboardController {

    private final GetLeaderboardUseCase getLeaderboardUseCase;

    public LeaderboardController(GetLeaderboardUseCase getLeaderboardUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
    }

    @GetMapping("/{exercise}")
    @Operation(summary = "Get the ranking of one exercise in the requested scope")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking returned"),
            @ApiResponse(responseCode = "400", description = "Unknown scope or missing parameter for the scope")
    })
    public List<LeaderboardEntryResponse> ranking(
            @Parameter(description = "Exercise counted by the app", example = "PULL_UP")
            @PathVariable Exercise exercise,
            @Parameter(description = "GLOBAL, COUNTRY or FRIENDS", example = "GLOBAL")
            @RequestParam(defaultValue = "GLOBAL") String scope,
            @Parameter(description = "Required when the scope is COUNTRY", example = "cl")
            @RequestParam(required = false) String country,
            @Parameter(description = "Required when the scope is FRIENDS",
                    example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3")
            @RequestParam(required = false) String userId) {

        return position(switch (parseScope(scope)) {
            case GLOBAL -> getLeaderboardUseCase.executeGlobal(exercise);
            case COUNTRY -> getLeaderboardUseCase.executeByCountry(exercise, require(country, "country", "COUNTRY"));
            case FRIENDS -> getLeaderboardUseCase.executeByFriends(exercise, require(userId, "userId", "FRIENDS"));
        });
    }

    private List<LeaderboardEntryResponse> position(List<TrainingSession> ranking) {
        return IntStream.range(0, ranking.size())
                .mapToObj(index -> LeaderboardEntryResponse.from(index + 1, ranking.get(index)))
                .toList();
    }

    private LeaderboardScope parseScope(String scope) {
        try {
            return LeaderboardScope.valueOf(scope.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown scope " + scope + ", expected GLOBAL, COUNTRY or FRIENDS");
        }
    }

    private String require(String value, String parameter, String scope) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Parameter " + parameter + " is required when the scope is " + scope);
        }
        return value;
    }

    private enum LeaderboardScope {
        GLOBAL,
        COUNTRY,
        FRIENDS
    }
}

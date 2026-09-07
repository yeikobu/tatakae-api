package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.GetLeaderboardUseCase;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.infrastructure.web.dto.LeaderboardEntryResponse;
import fit.tatakae.infrastructure.web.security.SecurityContextHelper;
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
            @ApiResponse(responseCode = "400", description = "Unknown scope or missing parameter for the scope"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: FRIENDS scope requires authentication")
    })
    public List<LeaderboardEntryResponse> ranking(
            @Parameter(description = "Exercise counted by the app", example = "PULL_UP")
            @PathVariable Exercise exercise,
            @Parameter(description = "GLOBAL, COUNTRY or FRIENDS", example = "GLOBAL")
            @RequestParam(defaultValue = "GLOBAL") String scope,
            @Parameter(description = "Required when the scope is COUNTRY", example = "cl")
            @RequestParam(required = false) String country,
            @Parameter(description = "Optional men or women category. Omit it for a mixed ranking",
                    example = "FEMALE")
            @RequestParam(required = false) Gender gender) {

        return position(switch (parseScope(scope)) {
            case GLOBAL -> getLeaderboardUseCase.executeGlobal(exercise, gender);
            case COUNTRY -> getLeaderboardUseCase.executeByCountry(exercise, require(country, "country", "COUNTRY"), gender);
            case FRIENDS -> {
                // Friends ranking is scoped to the authenticated user's friendship graph
                // Never trust userId from request params - always use the authenticated principal
                String authenticatedUserId = SecurityContextHelper.getAuthenticatedUserId();
                yield getLeaderboardUseCase.executeByFriends(exercise, authenticatedUserId, gender);
            }
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

package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.GetRankingAlertsUseCase;
import fit.tatakae.application.usecase.SetRankingAlertsUseCase;
import fit.tatakae.domain.exception.AuthenticationRequiredException;
import fit.tatakae.infrastructure.web.dto.RankingAlertsRequest;
import fit.tatakae.infrastructure.web.dto.RankingAlertsResponse;
import fit.tatakae.infrastructure.web.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@Tag(name = "Notification preferences", description = "Which pushes this athlete wants")
public class NotificationPreferencesController {

    private final GetRankingAlertsUseCase getRankingAlertsUseCase;
    private final SetRankingAlertsUseCase setRankingAlertsUseCase;

    public NotificationPreferencesController(GetRankingAlertsUseCase getRankingAlertsUseCase,
                                             SetRankingAlertsUseCase setRankingAlertsUseCase) {
        this.getRankingAlertsUseCase = getRankingAlertsUseCase;
        this.setRankingAlertsUseCase = setRankingAlertsUseCase;
    }

    @GetMapping
    @Operation(summary = "Read ranking-alert preference")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preference, defaulting to enabled"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public RankingAlertsResponse get() {
        return new RankingAlertsResponse(getRankingAlertsUseCase.execute(currentUserId()));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Turn ranking alerts on or off")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Preference stored"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public void put(@Valid @RequestBody RankingAlertsRequest request) {
        setRankingAlertsUseCase.execute(currentUserId(), request.rankingAlertsEnabled());
    }

    private static String currentUserId() {
        if (!SecurityContextHelper.isAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required: provide a valid Bearer access token");
        }
        return SecurityContextHelper.getAuthenticatedUserId();
    }
}

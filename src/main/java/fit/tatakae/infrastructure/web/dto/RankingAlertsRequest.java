package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "RankingAlertsRequest")
public record RankingAlertsRequest(
        @NotNull(message = "rankingAlertsEnabled is required")
        Boolean rankingAlertsEnabled) {
}

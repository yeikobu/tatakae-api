package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RankingAlertsResponse")
public record RankingAlertsResponse(boolean rankingAlertsEnabled) {
}

package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.application.usecase.SessionTokens;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Session access + refresh tokens")
public record SessionTokenResponse(
        @Schema(description = "HS256 access JWT (Bearer)")
        String accessToken,

        @Schema(description = "Opaque refresh token (rotate on each use)")
        String refreshToken,

        @Schema(description = "Access token lifetime in seconds", example = "900")
        int expiresIn
) {
    public static SessionTokenResponse from(SessionTokens tokens) {
        return new SessionTokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresIn());
    }
}

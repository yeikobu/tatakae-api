package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token request")
public record RefreshTokenRequest(
        @NotBlank
        @Schema(description = "Opaque refresh token issued by /auth/apple or /auth/refresh")
        String refreshToken
) {
}

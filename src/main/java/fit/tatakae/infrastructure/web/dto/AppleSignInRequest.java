package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Sign in with Apple request containing the identity token from Apple")
public record AppleSignInRequest(
        @NotBlank(message = "Identity token is required")
        @Schema(description = "Apple identity token (JWT)", example = "eyJraWQiOiJXNldjT0tC...", required = true)
        String identityToken,

        @Schema(description = "Optional nonce used during Sign in with Apple flow (raw value, not hashed)", 
                example = "a1b2c3d4e5f6", required = false)
        String nonce
) {
}

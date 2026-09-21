package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.application.usecase.SessionTokens;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sign in with Apple response containing athlete info, session tokens, and creation status")
public record AppleSignInResponse(
        @Schema(description = "Unique athlete identifier (UUID)", example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3")
        String userId,

        @Schema(description = "Athlete username/handle", example = "yeikobu")
        String username,

        @Schema(description = "Country code", example = "cl")
        String country,

        @Schema(description = "Privacy level (PUBLIC or PRIVATE)")
        PrivacyLevel privacyLevel,

        @Schema(description = "Gender (MALE, FEMALE, or UNSPECIFIED)")
        Gender gender,

        @Schema(description = "Public URL of the profile avatar, or null", nullable = true)
        String avatarUrl,

        @Schema(description = "Whether this athlete was created in this call (true) or already existed (false)")
        boolean created,

        @Schema(description = "HS256 access JWT for Authorization: Bearer")
        String accessToken,

        @Schema(description = "Opaque refresh token (60 days); rotate via POST /auth/refresh")
        String refreshToken,

        @Schema(description = "Access token lifetime in seconds", example = "900")
        int expiresIn
) {
    public static AppleSignInResponse from(User user, boolean created, SessionTokens tokens) {
        return new AppleSignInResponse(
                user.getUserId(),
                user.getUsername(),
                user.getCountry(),
                user.getPrivacyLevel(),
                user.getGender(),
                user.getAvatarUrl(),
                created,
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn()
        );
    }
}

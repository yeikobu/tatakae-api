package fit.tatakae.application.usecase;

public record SessionTokens(
        String accessToken,
        String refreshToken,
        int expiresIn
) {
}

package fit.tatakae.application.port;

public interface SessionTokenIssuer {

    String createAccessToken(String userId);

    /**
     * @return userId (sub) when the token is a valid access JWT
     */
    String validateAccessToken(String token);

    int getAccessTtlSeconds();

    int getRefreshTtlSeconds();
}

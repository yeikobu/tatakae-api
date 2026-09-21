package fit.tatakae.infrastructure.web.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class SessionJwtProperties {

    /**
     * HS256 secret. Required in all non-test environments.
     */
    private String secret = "";

    private int accessTtlSeconds = 900;

    private int refreshTtlSeconds = 5_184_000;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public void setAccessTtlSeconds(int accessTtlSeconds) {
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public int getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    public void setRefreshTtlSeconds(int refreshTtlSeconds) {
        this.refreshTtlSeconds = refreshTtlSeconds;
    }
}

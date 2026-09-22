package fit.tatakae.infrastructure.web.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fit.tatakae.application.port.SessionTokenIssuer;
import fit.tatakae.infrastructure.web.security.config.SessionJwtProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Component
public class SessionJwtService implements SessionTokenIssuer {

    private static final Logger logger = LoggerFactory.getLogger(SessionJwtService.class);
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    private static final String TEST_FALLBACK_SECRET = "test-only-jwt-secret-change-me-32b-min!";

    private final SessionJwtProperties properties;
    private final Environment environment;
    private byte[] secretBytes;

    public SessionJwtService(SessionJwtProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @PostConstruct
    void validateConfiguration() {
        String secret = properties.getSecret();
        Set<String> profiles = Set.of(environment.getActiveProfiles());
        boolean testLike = profiles.contains("test") || profiles.stream().anyMatch(p -> p.startsWith("test"));

        if (secret == null || secret.isBlank()) {
            if (testLike) {
                secret = TEST_FALLBACK_SECRET;
                properties.setSecret(secret);
                logger.warn("JWT_SECRET unset — using test fallback secret");
            } else {
                throw new IllegalStateException(
                        "JWT_SECRET / app.jwt.secret is required. Set a long random secret (32+ bytes).");
            }
        }

        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
    }

    /** Package-visible for unit tests that construct the service without Spring. */
    void initForTests(String secret) {
        properties.setSecret(secret);
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String createAccessToken(String userId) {
        ensureReady();
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(properties.getAccessTtlSeconds());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .claim(CLAIM_TYPE, TYPE_ACCESS)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secretBytes));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign access token", e);
        }
    }

    @Override
    public String validateAccessToken(String token) {
        ensureReady();
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secretBytes))) {
                throw new InvalidSessionJwtException("Invalid access token signature");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (expiration == null || !expiration.toInstant().isAfter(Instant.now())) {
                throw new InvalidSessionJwtException("Access token has expired");
            }
            String type = claims.getStringClaim(CLAIM_TYPE);
            if (!TYPE_ACCESS.equals(type)) {
                throw new InvalidSessionJwtException("Token is not an access token");
            }
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new InvalidSessionJwtException("Access token missing subject");
            }
            return subject;
        } catch (ParseException e) {
            throw new InvalidSessionJwtException("Invalid access token format", e);
        } catch (JOSEException e) {
            throw new InvalidSessionJwtException("Failed to verify access token", e);
        }
    }

    @Override
    public int getAccessTtlSeconds() {
        return properties.getAccessTtlSeconds();
    }

    @Override
    public int getRefreshTtlSeconds() {
        return properties.getRefreshTtlSeconds();
    }

    private void ensureReady() {
        if (secretBytes == null) {
            validateConfiguration();
        }
    }
}

package fit.tatakae.infrastructure.web.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fit.tatakae.infrastructure.web.security.config.AppleAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Component
public class AppleJwtValidator {

    private static final Logger logger = LoggerFactory.getLogger(AppleJwtValidator.class);
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final AppleAuthProperties appleAuthProperties;
    private JWKSet jwkSet;
    private Instant jwkSetLastFetched;

    public AppleJwtValidator(AppleAuthProperties appleAuthProperties) {
        this.appleAuthProperties = appleAuthProperties;
    }

    public AppleJwtClaims validate(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            validateIssuer(claimsSet);
            validateAudience(claimsSet);
            validateExpiration(claimsSet);
            validateSignature(signedJWT);

            String sub = claimsSet.getSubject();
            String email = claimsSet.getStringClaim("email");

            logger.debug("Apple JWT validated successfully for sub: {}", sub);
            return new AppleJwtClaims(sub, email);

        } catch (ParseException e) {
            logger.error("Failed to parse JWT token", e);
            throw new InvalidAppleJwtException("Invalid JWT format", e);
        }
    }

    private void validateIssuer(JWTClaimsSet claimsSet) {
        String issuer = claimsSet.getIssuer();
        if (!APPLE_ISSUER.equals(issuer)) {
            throw new InvalidAppleJwtException("Invalid issuer: " + issuer);
        }
    }

    private void validateAudience(JWTClaimsSet claimsSet) {
        if (claimsSet.getAudience() == null || claimsSet.getAudience().isEmpty()) {
            throw new InvalidAppleJwtException("Missing audience claim");
        }

        boolean validAudience = claimsSet.getAudience().stream()
                .anyMatch(aud -> appleAuthProperties.getClientIds().contains(aud));

        if (!validAudience) {
            throw new InvalidAppleJwtException("Invalid audience: " + claimsSet.getAudience());
        }
    }

    private void validateExpiration(JWTClaimsSet claimsSet) {
        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime == null) {
            throw new InvalidAppleJwtException("Missing expiration time");
        }

        if (expirationTime.before(new Date())) {
            throw new InvalidAppleJwtException("Token has expired");
        }
    }

    private void validateSignature(SignedJWT signedJWT) {
        try {
            String keyId = signedJWT.getHeader().getKeyID();
            if (keyId == null) {
                throw new InvalidAppleJwtException("Missing key ID in JWT header");
            }

            JWKSet jwkSet = getJwkSet();
            JWK jwk = jwkSet.getKeyByKeyId(keyId);

            if (jwk == null) {
                logger.warn("Key ID {} not found in JWKS, refreshing key set", keyId);
                jwkSet = refreshJwkSet();
                jwk = jwkSet.getKeyByKeyId(keyId);
            }

            if (jwk == null) {
                throw new InvalidAppleJwtException("Public key not found for key ID: " + keyId);
            }

            RSAKey rsaKey = jwk.toRSAKey();
            JWSVerifier verifier = new RSASSAVerifier(rsaKey);

            if (!signedJWT.verify(verifier)) {
                throw new InvalidAppleJwtException("JWT signature verification failed");
            }

        } catch (JOSEException e) {
            logger.error("Error during signature validation", e);
            throw new InvalidAppleJwtException("Failed to validate JWT signature", e);
        }
    }

    private JWKSet getJwkSet() {
        if (jwkSet == null || shouldRefreshJwkSet()) {
            return refreshJwkSet();
        }
        return jwkSet;
    }

    private boolean shouldRefreshJwkSet() {
        if (jwkSetLastFetched == null) {
            return true;
        }
        return Instant.now().isAfter(jwkSetLastFetched.plusSeconds(3600));
    }

    private synchronized JWKSet refreshJwkSet() {
        try {
            logger.info("Fetching Apple JWKS from {}", appleAuthProperties.getJwksUrl());
            jwkSet = JWKSet.load(new URL(appleAuthProperties.getJwksUrl()));
            jwkSetLastFetched = Instant.now();
            logger.info("Apple JWKS refreshed successfully");
            return jwkSet;
        } catch (IOException | ParseException e) {
            logger.error("Failed to fetch or parse Apple JWKS", e);
            throw new InvalidAppleJwtException("Failed to fetch Apple public keys", e);
        }
    }
}

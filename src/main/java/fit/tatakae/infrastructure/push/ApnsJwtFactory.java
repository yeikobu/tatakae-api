package fit.tatakae.infrastructure.push;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class ApnsJwtFactory {

    private static final Duration CACHE = Duration.ofMinutes(50);

    private final String teamId;
    private final String keyId;
    private final ECPrivateKey privateKey;
    private String cachedToken;
    private Instant cachedAt = Instant.EPOCH;

    public ApnsJwtFactory(String teamId, String keyId, ECPrivateKey privateKey) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKey = privateKey;
    }

    public synchronized String bearer(Instant now) {
        if (cachedToken != null && cachedAt.plus(CACHE).isAfter(now)) {
            return cachedToken;
        }
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(Date.from(now))
                    .build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new ECDSASigner(privateKey));
            cachedToken = jwt.serialize();
            cachedAt = now;
            return cachedToken;
        } catch (JOSEException exception) {
            throw new IllegalStateException("Could not sign APNs provider token", exception);
        }
    }
}

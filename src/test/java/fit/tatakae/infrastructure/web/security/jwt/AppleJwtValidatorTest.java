package fit.tatakae.infrastructure.web.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fit.tatakae.infrastructure.web.security.config.AppleAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppleJwtValidatorTest {

    @Mock
    private AppleAuthProperties appleAuthProperties;

    private AppleJwtValidator validator;
    private RSAKey rsaKey;
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String TEST_CLIENT_ID = "com.example.tatakae";

    @BeforeEach
    public void setUp() throws Exception {
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("test-key-id")
                .generate();

        lenient().when(appleAuthProperties.getClientIds()).thenReturn(List.of(TEST_CLIENT_ID, "fit.tatakae.web"));
        lenient().when(appleAuthProperties.getJwksUrl()).thenReturn("https://appleid.apple.com/auth/keys");

        validator = new AppleJwtValidator(appleAuthProperties);
    }

    @Test
    public void shouldRejectTokenWithInvalidIssuer() throws Exception {
        // Arrange
        String token = createToken("https://evil.com", TEST_CLIENT_ID, "test-sub");

        // Act & Assert
        InvalidAppleJwtException exception = assertThrows(InvalidAppleJwtException.class, () -> {
            validator.validate(token);
        });
        assertTrue(exception.getMessage().contains("Invalid issuer"));
    }

    @Test
    public void shouldRejectTokenWithInvalidAudience() throws Exception {
        // Arrange
        String token = createToken(APPLE_ISSUER, "com.evil.app", "test-sub");

        // Act & Assert
        InvalidAppleJwtException exception = assertThrows(InvalidAppleJwtException.class, () -> {
            validator.validate(token);
        });
        assertTrue(exception.getMessage().contains("Invalid audience"));
    }

    @Test
    public void shouldRejectExpiredToken() throws Exception {
        // Arrange
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test-sub")
                .issuer(APPLE_ISSUER)
                .audience(TEST_CLIENT_ID)
                .expirationTime(new Date(System.currentTimeMillis() - 3600000))
                .claim("email", "test@example.com")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claimsSet
        );
        signedJWT.sign(new RSASSASigner(rsaKey));

        // Act & Assert
        InvalidAppleJwtException exception = assertThrows(InvalidAppleJwtException.class, () -> {
            validator.validate(signedJWT.serialize());
        });
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    public void shouldRejectTokenWithMissingExpiration() throws Exception {
        // Arrange
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test-sub")
                .issuer(APPLE_ISSUER)
                .audience(TEST_CLIENT_ID)
                .claim("email", "test@example.com")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claimsSet
        );
        signedJWT.sign(new RSASSASigner(rsaKey));

        // Act & Assert
        InvalidAppleJwtException exception = assertThrows(InvalidAppleJwtException.class, () -> {
            validator.validate(signedJWT.serialize());
        });
        assertTrue(exception.getMessage().contains("Missing expiration time"));
    }

    @Test
    public void shouldRejectTokenWithMissingKeyId() throws Exception {
        // Arrange
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test-sub")
                .issuer(APPLE_ISSUER)
                .audience(TEST_CLIENT_ID)
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim("email", "test@example.com")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                claimsSet
        );
        signedJWT.sign(new RSASSASigner(rsaKey));

        // Act & Assert
        InvalidAppleJwtException exception = assertThrows(InvalidAppleJwtException.class, () -> {
            validator.validate(signedJWT.serialize());
        });
        assertTrue(exception.getMessage().contains("Missing key ID"));
    }

    @Test
    public void shouldRejectMalformedToken() {
        // Act & Assert
        InvalidAppleJwtException exception = assertThrows(InvalidAppleJwtException.class, () -> {
            validator.validate("not.a.valid.jwt");
        });
        assertTrue(exception.getMessage().contains("Invalid JWT format"));
    }

    private String createToken(String issuer, String audience, String subject) throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim("email", "test@example.com")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claimsSet
        );
        signedJWT.sign(new RSASSASigner(rsaKey));

        return signedJWT.serialize();
    }
}

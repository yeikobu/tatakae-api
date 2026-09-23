package fit.tatakae.infrastructure.push;

import com.nimbusds.jwt.SignedJWT;
import fit.tatakae.domain.entity.RankingBoard;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApnsTokenTest {

    @Test
    public void shouldBuildALocalizedAlertForEachBoard() {
        assertTrue(RankingPushPayload.json(RankingBoard.GLOBAL).contains("Someone passed you on the global ranking."));
        assertTrue(RankingPushPayload.json(RankingBoard.COUNTRY).contains("Someone passed you on the local ranking."));
        assertTrue(RankingPushPayload.json(RankingBoard.FRIENDS).contains("A friend passed you on your friends ranking."));
        assertTrue(RankingPushPayload.json(RankingBoard.COUNTRY).contains("\"scope\":\"COUNTRY\""));
        assertTrue(RankingPushPayload.json(RankingBoard.GLOBAL).contains("\"type\":\"ranking\""));
    }

    @Test
    public void shouldSignAProviderTokenFromAnEscapedPem() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair pair = generator.generateKeyPair();
        String body = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(pair.getPrivate().getEncoded());
        String escaped = "\"-----BEGIN PRIVATE KEY-----\\n" + body.replace("\n", "\\n") + "\\n-----END PRIVATE KEY-----\"";

        ApnsJwtFactory factory = new ApnsJwtFactory("TEAMID1234", "KEYID12345", ApnsPrivateKeyParser.parse(escaped));
        Instant now = Instant.parse("2026-09-22T12:00:00Z");
        String first = factory.bearer(now);
        String second = factory.bearer(now.plusSeconds(60));

        SignedJWT jwt = SignedJWT.parse(first);
        assertEquals("TEAMID1234", jwt.getJWTClaimsSet().getIssuer());
        assertEquals("KEYID12345", jwt.getHeader().getKeyID());
        assertEquals(first, second);
    }
}

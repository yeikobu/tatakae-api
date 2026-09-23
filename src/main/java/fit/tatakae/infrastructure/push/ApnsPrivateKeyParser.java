package fit.tatakae.infrastructure.push;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class ApnsPrivateKeyParser {

    private ApnsPrivateKeyParser() {
    }

    public static String readConfigured(String inline, String path) {
        if (inline != null && !inline.isBlank()) {
            return inline;
        }
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("APNs private key is missing");
        }
        try {
            return Files.readString(Path.of(path));
        } catch (IOException exception) {
            throw new IllegalStateException("APNs private key file could not be read", exception);
        }
    }

    public static ECPrivateKey parse(String pemOrEscaped) {
        String pem = pemOrEscaped.replace("\\n", "\n");
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            return (ECPrivateKey) KeyFactory.getInstance("EC")
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("APNs private key could not be read", exception);
        }
    }
}

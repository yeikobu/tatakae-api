package fit.tatakae.infrastructure.storage;

import fit.tatakae.domain.exception.InvalidAvatarException;
import fit.tatakae.domain.repository.AvatarStorage;
import fit.tatakae.infrastructure.web.config.AvatarStorageProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AvatarFileStorage implements AvatarStorage {

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private static final Set<String> ALLOWED = EXTENSION_BY_CONTENT_TYPE.keySet();

    private final AvatarStorageProperties properties;

    public AvatarFileStorage(AvatarStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(String userId, byte[] content, String contentType) {
        if (content == null || content.length == 0) {
            throw new InvalidAvatarException("Avatar file is required");
        }
        if (content.length > properties.getMaxBytes()) {
            throw new InvalidAvatarException("Avatar must be at most " + properties.getMaxBytes() + " bytes");
        }

        String normalized = normalizeContentType(contentType);
        if (!ALLOWED.contains(normalized)) {
            throw new InvalidAvatarException("Avatar must be image/jpeg, image/png or image/webp");
        }

        String extension = EXTENSION_BY_CONTENT_TYPE.get(normalized);
        Path dir = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            deleteExistingForUser(dir, userId);
            Path target = dir.resolve(userId + "." + extension);
            Files.write(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            String base = properties.getPublicBaseUrl().replaceAll("/$", "");
            return base + "/avatars/" + userId + "." + extension;
        } catch (IOException e) {
            throw new InvalidAvatarException("Could not store avatar file");
        }
    }

    @Override
    public void deleteForUser(String userId) {
        Path dir = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(dir)) {
            return;
        }
        deleteExistingForUser(dir, userId);
    }

    private void deleteExistingForUser(Path dir, String userId) {
        for (String ext : EXTENSION_BY_CONTENT_TYPE.values()) {
            Path candidate = dir.resolve(userId + "." + ext);
            try {
                Files.deleteIfExists(candidate);
            } catch (IOException ignored) {
                // best-effort cleanup
            }
        }
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        String normalized = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(normalized)) {
            return "image/jpeg";
        }
        return normalized;
    }

    public Path storageDirectory() {
        return Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
    }
}

package fit.tatakae.infrastructure.storage;

import fit.tatakae.domain.exception.InvalidAvatarException;
import fit.tatakae.infrastructure.web.config.AvatarStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AvatarFileStorageTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    @TempDir
    Path tempDir;

    @Test
    public void shouldStoreTheAvatarAndReturnItsPublicUrl() throws IOException {
        // Arrange
        AvatarFileStorage storage = storageAt(tempDir.resolve("avatars"));

        // Act
        String url = storage.store("user_1", JPEG, "image/jpeg");

        // Assert
        assertEquals("https://api.tatakae.fit/avatars/user_1.jpg", url);
        assertArrayEquals(JPEG, Files.readAllBytes(tempDir.resolve("avatars/user_1.jpg")));
    }

    // An unwritable volume is a server fault: it must not be reported as an invalid file (400).
    @Test
    public void shouldFailAsServerErrorWhenTheDiskRejectsTheWrite() throws IOException {
        // Arrange
        Path notADirectory = Files.writeString(tempDir.resolve("avatars"), "occupied");
        AvatarFileStorage storage = storageAt(notADirectory);

        // Act and Assert
        assertThrows(UncheckedIOException.class, () -> storage.store("user_1", JPEG, "image/jpeg"));
    }

    @Test
    public void shouldRejectAnUnsupportedContentTypeAsInvalidAvatar() {
        // Arrange
        AvatarFileStorage storage = storageAt(tempDir.resolve("avatars"));

        // Act and Assert
        assertThrows(InvalidAvatarException.class, () -> storage.store("user_1", JPEG, "image/gif"));
    }

    private static AvatarFileStorage storageAt(Path dir) {
        AvatarStorageProperties properties = new AvatarStorageProperties();
        properties.setStorageDir(dir.toString());
        properties.setPublicBaseUrl("https://api.tatakae.fit/");
        return new AvatarFileStorage(properties);
    }
}

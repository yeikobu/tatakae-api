package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserIdTest {

    private static final String RAW = "11111111-1111-1111-1111-111111111111";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    public void shouldRejectABlankIdentity(String invalidUserId) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> UserId.of(invalidUserId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"yeikobu", "1234", "11111111-1111-1111-1111", "not-a-uuid"})
    public void shouldRejectAnythingThatIsNotAUuid(String invalidUserId) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> UserId.of(invalidUserId));
    }

    @Test
    public void shouldRejectANullUuid() {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> new UserId(null));
    }

    @Test
    public void shouldParseACanonicalUuid() {
        // Act
        UserId userId = UserId.of(RAW);

        // Assert
        assertEquals(RAW, userId.asString());
        assertEquals(RAW, userId.toString());
        assertEquals(UUID.fromString(RAW), userId.value());
    }

    // The canonical form is lower case, so the same identity written in upper case still matches.
    @Test
    public void shouldNormalizeSurroundingSpacesAndCasing() {
        // Act
        UserId userId = UserId.of("  " + RAW.toUpperCase() + "  ");

        // Assert
        assertEquals(RAW, userId.asString());
        assertEquals(UserId.of(RAW), userId);
        assertEquals(UserId.of(RAW).hashCode(), userId.hashCode());
    }

    @Test
    public void shouldGenerateADifferentIdentityEveryTime() {
        // Act
        UserId first = UserId.generate();
        UserId second = UserId.generate();

        // Assert
        assertNotEquals(first, second);
        assertEquals(first, UserId.of(first.asString()));
    }
}

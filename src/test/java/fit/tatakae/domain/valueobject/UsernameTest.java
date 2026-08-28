package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UsernameTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    public void shouldRejectABlankHandle(String invalidHandle) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> new Username(invalidHandle));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jacob aguilar", "jacob-aguilar", "jacob@fit", "jacob/fit", "ñandu"})
    public void shouldRejectForbiddenCharacters(String invalidHandle) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> new Username(invalidHandle));
    }

    @Test
    public void shouldRejectAHandleLongerThanThirtyCharacters() {
        // Arrange
        String tooLong = "a".repeat(Username.MAX_LENGTH + 1);

        // Act and Assert
        assertThrows(InvalidUserException.class, () -> new Username(tooLong));
    }

    @Test
    public void shouldAcceptAHandleOfExactlyThirtyCharacters() {
        // Arrange
        String limit = "a".repeat(Username.MAX_LENGTH);

        // Act
        Username username = new Username(limit);

        // Assert
        assertEquals(limit, username.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"yeikobu", "jacob.aguilar", "jacob_aguilar", "user_1", "a1"})
    public void shouldAcceptInstagramStyleHandles(String validHandle) {
        // Act
        Username username = new Username(validHandle);

        // Assert
        assertEquals(validHandle, username.value());
    }

    @Test
    public void shouldTrimAndLowercaseTheHandle() {
        // Act
        Username username = new Username("  Jacob.Aguilar  ");

        // Assert
        assertEquals("jacob.aguilar", username.value());
        assertEquals("jacob.aguilar", username.toString());
    }

    @Test
    public void shouldNormalizeAHandleWithoutKeepingTheValueObject() {
        // Act and Assert
        assertEquals("yeikobu", Username.normalize(" YEIKOBU "));
    }

    @Test
    public void shouldTreatTwoHandlesThatDifferOnlyInCaseAsEqual() {
        // Act and Assert
        assertEquals(new Username("Yeikobu"), new Username("yeikobu"));
        assertEquals(new Username("Yeikobu").hashCode(), new Username("yeikobu").hashCode());
    }
}

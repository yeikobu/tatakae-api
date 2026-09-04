package fit.tatakae.domain.entity;

import fit.tatakae.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private static final String IDENTITY = "11111111-1111-1111-1111-111111111111";
    private static final String OTHER_IDENTITY = "22222222-2222-2222-2222-222222222222";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not-a-uuid", "1234"})
    public void shouldThrowExceptionWhenTheIdentityIsNotAUuid(String invalidUserId) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> {
            new User(invalidUserId, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"jacob aguilar", "jacob-aguilar", "jacob@fit"})
    public void shouldThrowExceptionWhenTheHandleIsNotValid(String invalidUsername) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> {
            new User(IDENTITY, invalidUsername, "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        });
    }

    @Test
    public void shouldMintAnIdentityWhenAnAthleteRegisters() {
        // Act
        User user = User.register("yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Assert
        assertNotNull(user.getUserId());
        assertEquals("yeikobu", user.getUsername());
        assertEquals("cl", user.getCountry());
        assertEquals(PrivacyLevel.PUBLIC, user.getPrivacyLevel());
        assertEquals(Gender.MALE, user.getGender());
    }

    @Test
    public void shouldGiveEveryRegisteredAthleteADifferentIdentity() {
        // Act
        User first = User.register("yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        User second = User.register("kenshin", "cl", PrivacyLevel.PUBLIC, Gender.FEMALE);

        // Assert
        assertNotEquals(first.getUserId(), second.getUserId());
        assertNotEquals(first, second);
    }

    @Test
    public void isValidUserTest() {
        // Act
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Assert
        assertEquals(IDENTITY, user.getUserId());
        assertEquals("yeikobu", user.getUsername());
        assertEquals("cl", user.getCountry());
        assertEquals(PrivacyLevel.PUBLIC, user.getPrivacyLevel());
        assertEquals(Gender.MALE, user.getGender());
    }

    @Test
    public void shouldStoreTheHandleInLowerCase() {
        // Act
        User user = new User(IDENTITY, "  Yeikobu  ", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Assert
        assertEquals("yeikobu", user.getUsername());
    }

    // This is the whole point of the UUID: a rename must not create a different athlete.
    @Test
    public void shouldKeepItsIdentityWhenRenamed() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Act
        User renamed = user.updatedTo("kenshin", "us", PrivacyLevel.PRIVATE, Gender.FEMALE);

        // Assert
        assertEquals(IDENTITY, renamed.getUserId());
        assertEquals("kenshin", renamed.getUsername());
        assertEquals("us", renamed.getCountry());
        assertEquals(PrivacyLevel.PRIVATE, renamed.getPrivacyLevel());
        assertEquals(Gender.FEMALE, renamed.getGender());
        assertEquals(user, renamed);
        assertEquals("yeikobu", user.getUsername());
        assertEquals(Gender.MALE, user.getGender());
    }

    @Test
    public void shouldRecognizeItsOwnHandleWhateverTheCasing() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Act and Assert
        assertTrue(user.hasUsername("YEIKOBU"));
        assertFalse(user.hasUsername("kenshin"));
    }

    @Test
    public void shouldConfirmWhenUserPrivacyIsPublic() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Act and Assert
        assertTrue(user.isPublic());
    }

    @Test
    public void shouldDenyWhenUserPrivacyIsPrivate() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PRIVATE, Gender.MALE);

        // Act and Assert
        assertFalse(user.isPublic());
    }

    @Test
    public void shouldConfirmWhenUserIsFromTheGivenCountry() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Act and Assert
        assertTrue(user.isFromCountry("cl"));
        assertFalse(user.isFromCountry("us"));
    }

    @Test
    public void shouldConfirmWhenTheAthleteMatchesTheGivenGender() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.FEMALE);

        // Act and Assert
        assertTrue(user.hasGender(Gender.FEMALE));
        assertFalse(user.hasGender(Gender.MALE));
    }

    @Test
    public void shouldCompareUsersByTheirIdentityOnly() {
        // Arrange
        User user = new User(IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        User sameIdentity = new User(IDENTITY, "kenshin", "us", PrivacyLevel.PRIVATE, Gender.FEMALE);
        User otherIdentity = new User(OTHER_IDENTITY, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Act and Assert
        assertEquals(user, user);
        assertEquals(user, sameIdentity);
        assertEquals(user.hashCode(), sameIdentity.hashCode());
        assertNotEquals(user, otherIdentity);
        assertNotEquals(user, new Object());
    }
}

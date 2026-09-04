package fit.tatakae;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Test fixtures derive a stable identity from the handle, so a test can name an athlete by its
// handle while the code under test still works with the UUID it will see in production.
public final class TestUsers {

    private TestUsers() {
    }

    public static String idOf(String username) {
        return UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public static User user(String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        return new User(idOf(username), username, country, privacyLevel, gender);
    }

    public static User user(String username, String country, PrivacyLevel privacyLevel) {
        return user(username, country, privacyLevel, Gender.MALE);
    }

    public static User user(String username) {
        return user(username, "cl", PrivacyLevel.PUBLIC);
    }
}

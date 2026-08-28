package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InvalidUserException;

import java.util.Locale;
import java.util.regex.Pattern;

// The handle is the identity of an athlete, in the spirit of an Instagram username.
public record Username(String value) {

    public static final int MAX_LENGTH = 30;

    private static final Pattern ALLOWED = Pattern.compile("^[a-z0-9._]{1," + MAX_LENGTH + "}$");

    public Username {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserException("Username cannot be null or empty");
        }

        // Handles are case insensitive: Yeikobu and yeikobu are the same athlete.
        value = value.trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED.matcher(value).matches()) {
            throw new InvalidUserException(
                    "Username must be at most " + MAX_LENGTH
                            + " characters long and use only letters, digits, dots and underscores");
        }
    }

    // Normalizes a raw handle coming from the outside world without keeping the value object around.
    public static String normalize(String rawValue) {
        return new Username(rawValue).value();
    }

    @Override
    public String toString() {
        return value;
    }
}

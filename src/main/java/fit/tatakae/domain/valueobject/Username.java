package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InvalidUserException;

import java.util.Locale;
import java.util.regex.Pattern;

// The handle is the identity of an athlete, in the spirit of an Instagram username.
// The stored spelling keeps the casing the athlete typed; Yeikobu and yeikobu are still the same athlete.
public record Username(String value) {

    public static final int MAX_LENGTH = 30;

    private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z0-9._]{1," + MAX_LENGTH + "}$");

    public Username {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserException("Username cannot be null or empty");
        }

        value = value.trim();

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

    public String canonical() {
        return value.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Username other && canonical().equals(other.canonical());
    }

    @Override
    public int hashCode() {
        return canonical().hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

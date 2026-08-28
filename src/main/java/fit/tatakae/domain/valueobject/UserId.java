package fit.tatakae.domain.valueobject;

import fit.tatakae.domain.exception.InvalidUserException;

import java.util.UUID;

// The opaque, immutable identity of an athlete. It survives every rename, so friendships,
// training sessions and any future report keep pointing at the same person.
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new InvalidUserException("User id cannot be null");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new InvalidUserException("User id cannot be null or empty");
        }
        try {
            return new UserId(UUID.fromString(rawValue.trim()));
        } catch (IllegalArgumentException exception) {
            throw new InvalidUserException("User id " + rawValue + " is not a valid UUID");
        }
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public String toString() {
        return asString();
    }
}

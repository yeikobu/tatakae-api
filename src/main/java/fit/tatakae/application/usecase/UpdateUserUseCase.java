package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.DuplicateUserException;
import fit.tatakae.domain.exception.InvalidUserException;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;
import fit.tatakae.domain.valueobject.Username;

public class UpdateUserUseCase {
    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Renaming is allowed precisely because the identity is a UUID: friendships and sessions keep pointing here.
    // Gender is chosen once in the post-Apple nickname form and cannot change afterwards.
    // Sign in with Apple stores UNSPECIFIED, so that first choice is the one write that must go through.
    public User execute(String userId, String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        String identity = UserId.of(userId).asString();
        String handle = Username.normalize(username);

        User stored = userRepository.findById(identity)
                .orElseThrow(() -> new ResourceNotFoundException("User " + identity + " was not found"));

        boolean choosingForTheFirstTime = stored.getGender() == Gender.UNSPECIFIED;
        if (gender != null && gender != stored.getGender() && !choosingForTheFirstTime) {
            throw new InvalidUserException("gender cannot be changed after registration");
        }
        Gender resolvedGender = choosingForTheFirstTime && gender != null ? gender : stored.getGender();

        boolean handleBelongsToAnotherAthlete = userRepository.findByUsername(handle)
                .filter(owner -> !owner.equals(stored))
                .isPresent();
        if (handleBelongsToAnotherAthlete) {
            throw new DuplicateUserException("Username " + handle + " is already taken");
        }

        return userRepository.save(stored.updatedTo(handle, country, privacyLevel, resolvedGender));
    }
}

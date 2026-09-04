package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.DuplicateUserException;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.Username;

public class RegisterUserUseCase {
    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        String handle = Username.normalize(username);
        if (userRepository.findByUsername(handle).isPresent()) {
            throw new DuplicateUserException("Username " + handle + " is already taken");
        }
        return userRepository.save(User.register(handle, country, privacyLevel, gender));
    }
}

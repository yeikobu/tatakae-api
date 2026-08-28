package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.Username;

// Resolves a public handle into the athlete it currently belongs to, identity included.
public class FindUserByUsernameUseCase {
    private final UserRepository userRepository;

    public FindUserByUsernameUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String username) {
        String handle = Username.normalize(username);
        return userRepository.findByUsername(handle)
                .orElseThrow(() -> new ResourceNotFoundException("Username " + handle + " was not found"));
    }
}

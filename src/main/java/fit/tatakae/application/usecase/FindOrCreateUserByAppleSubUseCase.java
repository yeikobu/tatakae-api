package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.repository.UserRepository;

public class FindOrCreateUserByAppleSubUseCase {

    private final UserRepository userRepository;

    public FindOrCreateUserByAppleSubUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean exists(String appleSub) {
        return userRepository.findByAppleSub(appleSub).isPresent();
    }

    public User execute(String appleSub) {
        return userRepository.findByAppleSub(appleSub)
                .orElseGet(() -> createDefaultUser(appleSub));
    }

    private User createDefaultUser(String appleSub) {
        String defaultUsername = "athlete_" + appleSub.substring(0, Math.min(8, appleSub.length()));
        User newUser = User.registerWithApple(appleSub, defaultUsername, "unknown", PrivacyLevel.PUBLIC, Gender.UNSPECIFIED);
        return userRepository.save(newUser);
    }
}

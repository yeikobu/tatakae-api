package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.RankingAlertPreferenceRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

public class SetRankingAlertsUseCase {

    private final RankingAlertPreferenceRepository preferences;
    private final UserRepository userRepository;

    public SetRankingAlertsUseCase(RankingAlertPreferenceRepository preferences, UserRepository userRepository) {
        this.preferences = preferences;
        this.userRepository = userRepository;
    }

    public void execute(String userId, boolean enabled) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        preferences.setEnabled(identity, enabled);
    }
}

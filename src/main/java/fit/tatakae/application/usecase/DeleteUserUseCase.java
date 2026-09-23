package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.DeviceTokenRepository;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.RankingAlertPreferenceRepository;
import fit.tatakae.domain.repository.RankingPushLogRepository;
import fit.tatakae.domain.repository.SessionRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

public class DeleteUserUseCase {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final SessionRepository sessionRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final RankingPushLogRepository rankingPushLogRepository;
    private final RankingAlertPreferenceRepository rankingAlertPreferenceRepository;

    public DeleteUserUseCase(UserRepository userRepository,
                             FriendshipRepository friendshipRepository,
                             SessionRepository sessionRepository,
                             DeviceTokenRepository deviceTokenRepository,
                             RankingPushLogRepository rankingPushLogRepository,
                             RankingAlertPreferenceRepository rankingAlertPreferenceRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.sessionRepository = sessionRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.rankingPushLogRepository = rankingPushLogRepository;
        this.rankingAlertPreferenceRepository = rankingAlertPreferenceRepository;
    }

    // Everything that points at the athlete goes with it: leaving orphan rows behind would either
    // break referential integrity or resurrect the athlete inside somebody else's ranking.
    public void execute(String userId) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        sessionRepository.deleteAllOf(identity);
        friendshipRepository.deleteAllInvolving(identity);
        deviceTokenRepository.deleteAllOf(identity);
        rankingPushLogRepository.deleteAllOf(identity);
        rankingAlertPreferenceRepository.delete(identity);
        userRepository.delete(identity);
    }
}

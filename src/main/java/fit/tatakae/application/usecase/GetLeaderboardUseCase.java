package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.service.LeaderboardService;
import fit.tatakae.domain.valueobject.UserId;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetLeaderboardUseCase {
    private final LeaderboardService leaderboardService;
    private final FriendshipRepository friendshipRepository;

    public GetLeaderboardUseCase(LeaderboardService leaderboardService, FriendshipRepository friendshipRepository) {
        this.leaderboardService = leaderboardService;
        this.friendshipRepository = friendshipRepository;
    }

    public List<TrainingSession> executeGlobal(Exercise exercise, Gender gender) {
        return leaderboardService.getGlobalRanking(exercise, gender);
    }

    public List<TrainingSession> executeByCountry(Exercise exercise, String country, Gender gender) {
        return leaderboardService.getLocalRanking(exercise, country, gender);
    }

    public List<TrainingSession> executeByFriends(Exercise exercise, String userId, Gender gender) {
        String identity = UserId.of(userId).asString();
        Set<String> friendIds = friendshipRepository.findAcceptedFor(identity).stream()
                .map(friendship -> friendship.friendOf(identity))
                .collect(Collectors.toSet());
        return leaderboardService.getFriendsRanking(exercise, identity, friendIds, gender);
    }
}

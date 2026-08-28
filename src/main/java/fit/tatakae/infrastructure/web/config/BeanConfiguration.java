package fit.tatakae.infrastructure.web.config;

import fit.tatakae.application.usecase.*;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.SessionRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.service.FriendshipService;
import fit.tatakae.domain.service.LeaderboardService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// The domain and application layers know nothing about Spring: their wiring lives here, in infrastructure.
@Configuration
public class BeanConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public LeaderboardService leaderboardService(SessionRepository sessionRepository) {
        return new LeaderboardService(sessionRepository);
    }

    @Bean
    public FriendshipService friendshipService(FriendshipRepository friendshipRepository, Clock clock) {
        return new FriendshipService(friendshipRepository, clock);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
        return new RegisterUserUseCase(userRepository);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository userRepository) {
        return new GetUserUseCase(userRepository);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepository userRepository) {
        return new ListUsersUseCase(userRepository);
    }

    @Bean
    public FindUserByUsernameUseCase findUserByUsernameUseCase(UserRepository userRepository) {
        return new FindUserByUsernameUseCase(userRepository);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository) {
        return new UpdateUserUseCase(userRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }

    @Bean
    public SendFriendRequestUseCase sendFriendRequestUseCase(UserRepository userRepository,
                                                             FriendshipRepository friendshipRepository,
                                                             FriendshipService friendshipService) {
        return new SendFriendRequestUseCase(userRepository, friendshipRepository, friendshipService);
    }

    @Bean
    public RespondFriendRequestUseCase respondFriendRequestUseCase(FriendshipRepository friendshipRepository) {
        return new RespondFriendRequestUseCase(friendshipRepository);
    }

    @Bean
    public GetFriendshipUseCase getFriendshipUseCase(FriendshipRepository friendshipRepository) {
        return new GetFriendshipUseCase(friendshipRepository);
    }

    @Bean
    public RemoveFriendshipUseCase removeFriendshipUseCase(FriendshipRepository friendshipRepository) {
        return new RemoveFriendshipUseCase(friendshipRepository);
    }

    @Bean
    public ListFriendsUseCase listFriendsUseCase(UserRepository userRepository,
                                                 FriendshipRepository friendshipRepository) {
        return new ListFriendsUseCase(userRepository, friendshipRepository);
    }

    @Bean
    public ListFriendRequestsUseCase listFriendRequestsUseCase(UserRepository userRepository,
                                                               FriendshipRepository friendshipRepository) {
        return new ListFriendRequestsUseCase(userRepository, friendshipRepository);
    }

    @Bean
    public RecordTrainingSessionUseCase recordTrainingSessionUseCase(SessionRepository sessionRepository) {
        return new RecordTrainingSessionUseCase(sessionRepository);
    }

    @Bean
    public GetLeaderboardUseCase getLeaderboardUseCase(LeaderboardService leaderboardService,
                                                       FriendshipRepository friendshipRepository) {
        return new GetLeaderboardUseCase(leaderboardService, friendshipRepository);
    }
}

package fit.tatakae.infrastructure.web.config;

import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.application.usecase.*;
import fit.tatakae.domain.repository.AvatarStorage;
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
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository,
                                              FriendshipRepository friendshipRepository,
                                              SessionRepository sessionRepository) {
        return new DeleteUserUseCase(userRepository, friendshipRepository, sessionRepository);
    }

    @Bean
    public SendFriendRequestUseCase sendFriendRequestUseCase(UserRepository userRepository,
                                                             FriendshipRepository friendshipRepository,
                                                             FriendshipService friendshipService,
                                                             UserEventPublisher userEventPublisher) {
        return new SendFriendRequestUseCase(userRepository, friendshipRepository, friendshipService, userEventPublisher);
    }

    @Bean
    public RespondFriendRequestUseCase respondFriendRequestUseCase(FriendshipRepository friendshipRepository,
                                                                   UserRepository userRepository,
                                                                   UserEventPublisher userEventPublisher) {
        return new RespondFriendRequestUseCase(friendshipRepository, userRepository, userEventPublisher);
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
    public RecordTrainingSessionUseCase recordTrainingSessionUseCase(SessionRepository sessionRepository,
                                                                     FriendshipRepository friendshipRepository,
                                                                     UserEventPublisher userEventPublisher) {
        return new RecordTrainingSessionUseCase(sessionRepository, friendshipRepository, userEventPublisher);
    }

    @Bean
    public GetLeaderboardUseCase getLeaderboardUseCase(LeaderboardService leaderboardService,
                                                       FriendshipRepository friendshipRepository) {
        return new GetLeaderboardUseCase(leaderboardService, friendshipRepository);
    }

    @Bean
    public FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase(UserRepository userRepository) {
        return new FindOrCreateUserByAppleSubUseCase(userRepository);
    }

    @Bean
    public UploadAvatarUseCase uploadAvatarUseCase(UserRepository userRepository, AvatarStorage avatarStorage) {
        return new UploadAvatarUseCase(userRepository, avatarStorage);
    }

    @Bean
    public DeleteAvatarUseCase deleteAvatarUseCase(UserRepository userRepository, AvatarStorage avatarStorage) {
        return new DeleteAvatarUseCase(userRepository, avatarStorage);
    }

}

package fit.tatakae.application.usecase;

import fit.tatakae.application.event.LeaderboardUpdateEvent;
import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.SessionRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RecordTrainingSessionUseCase {
    private final SessionRepository sessionRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserEventPublisher userEventPublisher;

    public RecordTrainingSessionUseCase(SessionRepository sessionRepository,
                                        FriendshipRepository friendshipRepository,
                                        UserEventPublisher userEventPublisher) {
        this.sessionRepository = sessionRepository;
        this.friendshipRepository = friendshipRepository;
        this.userEventPublisher = userEventPublisher;
    }

    public TrainingSession execute(User user, Exercise exercise, int reps, Instant start, Instant end, Clock clock) {
        TrainingSession session = new TrainingSession(user, exercise, reps, start, end, clock);
        sessionRepository.save(session);
        notifyLeaderboardSubscribers(user, exercise, reps);
        return session;
    }

    private void notifyLeaderboardSubscribers(User user, Exercise exercise, int reps) {
        String userId = user.getUserId();
        Set<String> recipients = new LinkedHashSet<>();
        recipients.add(userId);

        List<Friendship> friendships = friendshipRepository.findAcceptedFor(userId);
        for (Friendship friendship : friendships) {
            recipients.add(friendship.friendOf(userId));
        }

        // MVP: notify scorer + friends for both GLOBAL and FRIENDS scopes (lean refresh hints).
        userEventPublisher.publishLeaderboardUpdate(
                recipients, new LeaderboardUpdateEvent(exercise, "GLOBAL", user, reps));
        userEventPublisher.publishLeaderboardUpdate(
                recipients, new LeaderboardUpdateEvent(exercise, "FRIENDS", user, reps));
    }
}

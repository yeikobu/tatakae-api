package fit.tatakae.application.event;

import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.User;

public record LeaderboardUpdateEvent(Exercise exercise, String scope, User user, int reps) {
}

package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.User;

/**
 * An accepted friendship together with the athlete on the other end.
 * Carries friendshipId so clients can DELETE /friendships/{id} without a local cache.
 */
public record AcceptedFriend(String friendshipId, User friend) {
}

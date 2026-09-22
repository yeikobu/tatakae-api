package fit.tatakae.application.event;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.User;

public record FriendRequestAcceptedEvent(Friendship friendship, User acceptedBy) {
}

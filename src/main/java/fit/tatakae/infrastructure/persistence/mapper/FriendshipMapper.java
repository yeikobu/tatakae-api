package fit.tatakae.infrastructure.persistence.mapper;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.infrastructure.persistence.entity.FriendshipEntity;

import java.time.Clock;

public final class FriendshipMapper {

    private FriendshipMapper() {
    }

    public static Friendship toDomain(FriendshipEntity entity, Clock clock) {
        return new Friendship(
                entity.getId(),
                entity.getRequesterId(),
                entity.getAddresseeId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt(),
                clock);
    }

    public static FriendshipEntity toEntity(Friendship friendship) {
        return new FriendshipEntity(
                friendship.getId(),
                friendship.getRequesterId(),
                friendship.getAddresseeId(),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getRespondedAt());
    }
}

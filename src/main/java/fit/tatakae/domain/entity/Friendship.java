package fit.tatakae.domain.entity;

import fit.tatakae.domain.exception.InvalidFriendshipException;
import fit.tatakae.domain.exception.InvalidFriendshipTransitionException;
import fit.tatakae.domain.exception.SelfFriendshipException;
import fit.tatakae.domain.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class Friendship {
    private final String id;
    private final String requesterId;
    private final String addresseeId;
    private final Instant createdAt;
    private final Clock clock;
    private FriendshipStatus status;
    private Instant respondedAt;

    public Friendship(String requesterId, String addresseeId, Clock clock) {
        this(UUID.randomUUID().toString(), requesterId, addresseeId, FriendshipStatus.PENDING, clock.instant(), null, clock);
    }

    // Reconstitution constructor: used when the friendship identity already exists (e.g. loaded from a repository).
    public Friendship(String id, String requesterId, String addresseeId, FriendshipStatus status,
                      Instant createdAt, Instant respondedAt, Clock clock) {
        // Both ends are athlete identities, not handles: a rename never breaks the relation.
        this.requesterId = UserId.of(requesterId).asString();
        this.addresseeId = UserId.of(addresseeId).asString();

        if (this.requesterId.equals(this.addresseeId)) {
            throw new SelfFriendshipException("A user cannot befriend itself");
        }

        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
        this.clock = clock;
    }

    public void accept() {
        requirePending("accepted");
        this.status = FriendshipStatus.ACCEPTED;
        this.respondedAt = clock.instant();
    }

    public void reject() {
        requirePending("rejected");
        this.status = FriendshipStatus.REJECTED;
        this.respondedAt = clock.instant();
    }

    // Blocking is the only transition allowed once the request has already been accepted.
    public void block() {
        if (this.status != FriendshipStatus.PENDING && this.status != FriendshipStatus.ACCEPTED) {
            throw new InvalidFriendshipTransitionException("Only a pending or accepted friendship can be blocked, current status is " + this.status);
        }
        this.status = FriendshipStatus.BLOCKED;
        this.respondedAt = clock.instant();
    }

    public boolean involves(String userId) {
        String identity = UserId.of(userId).asString();
        return this.requesterId.equals(identity) || this.addresseeId.equals(identity);
    }

    public boolean isAccepted() {
        return this.status == FriendshipStatus.ACCEPTED;
    }

    public boolean isPendingFor(String userId) {
        return this.status == FriendshipStatus.PENDING && this.addresseeId.equals(UserId.of(userId).asString());
    }

    // Returns the athlete sitting at the other end of the relation.
    public String friendOf(String userId) {
        String identity = UserId.of(userId).asString();
        if (!involves(identity)) {
            throw new InvalidFriendshipException("User " + identity + " is not part of this friendship");
        }
        return this.requesterId.equals(identity) ? this.addresseeId : this.requesterId;
    }

    private void requirePending(String action) {
        if (this.status != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipTransitionException("Only a pending request can be " + action + ", current status is " + this.status);
        }
    }

    // MARK: - Getters
    public String getId() {
        return id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getAddresseeId() {
        return addresseeId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Friendship other)) {
            return false;
        }
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

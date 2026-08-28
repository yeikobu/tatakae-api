package fit.tatakae.infrastructure.persistence.mapper;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.*;
import fit.tatakae.infrastructure.persistence.entity.FriendshipEntity;
import fit.tatakae.infrastructure.persistence.entity.TrainingSessionEntity;
import fit.tatakae.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceMapperTest {

    private static final Instant NOW = Instant.parse("2026-08-28T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    public void shouldMapAUserBackAndForthWithoutLosingData() {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);

        // Act
        UserEntity entity = UserMapper.toEntity(user);
        User roundTrip = UserMapper.toDomain(entity);

        // Assert
        assertEquals("yeikobu", entity.getUsername());
        assertEquals("cl", entity.getCountry());
        assertEquals(PrivacyLevel.PUBLIC, entity.getPrivacyLevel());
        assertEquals(user, roundTrip);
        assertEquals(user.getUsername(), roundTrip.getUsername());
    }

    @Test
    public void shouldMapAPendingFriendshipBackAndForth() {
        // Arrange
        Friendship friendship = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);

        // Act
        FriendshipEntity entity = FriendshipMapper.toEntity(friendship);
        Friendship roundTrip = FriendshipMapper.toDomain(entity, CLOCK);

        // Assert
        assertEquals(friendship.getId(), entity.getId());
        assertEquals(TestUsers.idOf("user_1"), entity.getRequesterId());
        assertEquals(TestUsers.idOf("user_2"), entity.getAddresseeId());
        assertEquals(FriendshipStatus.PENDING, entity.getStatus());
        assertEquals(NOW, entity.getCreatedAt());
        assertNull(entity.getRespondedAt());
        assertEquals(friendship, roundTrip);
        assertEquals(FriendshipStatus.PENDING, roundTrip.getStatus());
    }

    @Test
    public void shouldKeepTheAnswerTimestampOfAnAcceptedFriendship() {
        // Arrange
        Friendship friendship = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        friendship.accept();

        // Act
        Friendship roundTrip = FriendshipMapper.toDomain(FriendshipMapper.toEntity(friendship), CLOCK);

        // Assert
        assertEquals(FriendshipStatus.ACCEPTED, roundTrip.getStatus());
        assertEquals(NOW, roundTrip.getRespondedAt());
    }

    @Test
    public void shouldMapATrainingSessionBackAndForth() {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);
        TrainingSession session = new TrainingSession(user, Exercise.PULL_UP, 20, NOW, NOW.plusSeconds(60), CLOCK);

        // Act
        TrainingSessionEntity entity = TrainingSessionMapper.toEntity(session, UserMapper.toEntity(user));
        TrainingSession roundTrip = TrainingSessionMapper.toDomain(entity, CLOCK);

        // Assert
        assertEquals(session.getId(), entity.getId());
        assertEquals(Exercise.PULL_UP, entity.getExercise());
        assertEquals(20, entity.getReps());
        assertEquals(NOW, entity.getStartedAt());
        assertEquals(NOW.plusSeconds(60), entity.getEndedAt());
        assertEquals(TestUsers.idOf("yeikobu"), entity.getUser().getId());
        assertEquals(session, roundTrip);
        assertEquals(user, roundTrip.getUser());
        assertEquals(20, roundTrip.getReps());
    }
}

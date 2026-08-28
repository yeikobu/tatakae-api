package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaUserRepository.class, PostgresIntegrationTest.TestClockConfiguration.class})
public class JpaUserRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private JpaUserRepository userRepository;

    @Test
    public void shouldStoreAndReadBackAUser() {
        // Arrange
        User user = TestUsers.user("yeikobu", "cl", PrivacyLevel.PUBLIC);

        // Act
        userRepository.save(user);
        Optional<User> stored = userRepository.findByUsername("yeikobu");

        // Assert
        assertTrue(stored.isPresent());
        assertEquals("yeikobu", stored.get().getUsername());
        assertEquals("cl", stored.get().getCountry());
        assertEquals(PrivacyLevel.PUBLIC, stored.get().getPrivacyLevel());
    }

    // The handle is the primary key, so a lookup with different casing still lands on the stored row.
    @Test
    public void shouldFindAUserByItsNormalizedHandle() {
        // Arrange
        userRepository.save(TestUsers.user("KENSHIN", "jp", PrivacyLevel.PRIVATE));

        // Act
        Optional<User> stored = userRepository.findByUsername("kenshin");

        // Assert
        assertTrue(stored.isPresent());
        assertEquals("kenshin", stored.get().getUsername());
        assertEquals(PrivacyLevel.PRIVATE, stored.get().getPrivacyLevel());
    }

    // Saving the same handle twice overwrites the profile instead of creating a second athlete.
    @Test
    public void shouldKeepASingleRowPerHandle() {
        // Arrange
        userRepository.save(TestUsers.user("piccolo", "jp", PrivacyLevel.PUBLIC));

        // Act
        userRepository.save(TestUsers.user("piccolo", "cl", PrivacyLevel.PRIVATE));
        long rows = userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals("piccolo"))
                .count();

        // Assert
        assertEquals(1, rows);
        assertEquals("cl", userRepository.findByUsername("piccolo").orElseThrow().getCountry());
    }

    @Test
    public void shouldReturnEmptyWhenTheHandleIsUnknown() {
        // Act and Assert
        assertTrue(userRepository.findByUsername("nobody").isEmpty());
        assertFalse(userRepository.existsById(TestUsers.idOf("nobody")));
    }

    @Test
    public void shouldListEveryStoredUser() {
        // Arrange
        userRepository.save(TestUsers.user("goku", "jp", PrivacyLevel.PUBLIC));
        userRepository.save(TestUsers.user("gohan", "jp", PrivacyLevel.PUBLIC));

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(user -> user.getUsername().equals("goku")));
    }

    @Test
    public void shouldReportExistenceAndDeleteAUser() {
        // Arrange
        userRepository.save(TestUsers.user("vegeta", "jp", PrivacyLevel.PUBLIC));

        // Act
        boolean existedBefore = userRepository.existsById(TestUsers.idOf("vegeta"));
        userRepository.delete(TestUsers.idOf("vegeta"));

        // Assert
        assertTrue(existedBefore);
        assertFalse(userRepository.existsById(TestUsers.idOf("vegeta")));
    }

    @Test
    public void shouldReadAnAthleteBackByItsIdentity() {
        // Arrange
        User user = TestUsers.user("krillin", "jp", PrivacyLevel.PUBLIC);
        userRepository.save(user);

        // Act
        Optional<User> stored = userRepository.findById(TestUsers.idOf("krillin"));

        // Assert
        assertTrue(stored.isPresent());
        assertEquals(user, stored.get());
        assertEquals("krillin", stored.get().getUsername());
    }

    // The reason the identity is a UUID: a rename updates the row instead of creating a second athlete.
    @Test
    public void shouldKeepTheSameRowWhenTheAthleteIsRenamed() {
        // Arrange
        User user = TestUsers.user("trunks", "jp", PrivacyLevel.PUBLIC);
        userRepository.save(user);

        // Act
        userRepository.save(user.updatedTo("mirai_trunks", "cl", PrivacyLevel.PRIVATE));

        // Assert
        User stored = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals("mirai_trunks", stored.getUsername());
        assertEquals("cl", stored.getCountry());
        assertTrue(userRepository.findByUsername("trunks").isEmpty());
        assertEquals(user.getUserId(), userRepository.findByUsername("mirai_trunks").orElseThrow().getUserId());
    }
}

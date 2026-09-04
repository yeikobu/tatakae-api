package fit.tatakae;

import fit.tatakae.application.usecase.SendFriendRequestUseCase;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.SessionRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.infrastructure.persistence.adapter.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// The whole graph has to wire up: domain ports resolved to their JPA adapters, use cases built by configuration.
// Spring Boot tests ignore spring.profiles.default from application.yaml, so the profile is set here.
@SpringBootTest
@ActiveProfiles("dev")
public class ApplicationContextTest extends PostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SendFriendRequestUseCase sendFriendRequestUseCase;

    @Test
    public void shouldWireEveryDomainPortToItsJpaAdapter() {
        // Act and Assert
        assertNotNull(sendFriendRequestUseCase);
        assertTrue(userRepository.getClass().getName().contains("JpaUserRepository"));
        assertTrue(friendshipRepository.getClass().getName().contains("JpaFriendshipRepository"));
        assertTrue(sessionRepository.getClass().getName().contains("JpaSessionRepository"));
    }
}

package fit.tatakae.infrastructure.web.security.jwt;

import fit.tatakae.infrastructure.web.security.config.SessionJwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

public class SessionJwtServiceTest {

    private SessionJwtService service;

    @BeforeEach
    void setUp() {
        SessionJwtProperties props = new SessionJwtProperties();
        props.setSecret("test-only-jwt-secret-change-me-32b-min!");
        props.setAccessTtlSeconds(900);
        props.setRefreshTtlSeconds(5184000);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("test");
        service = new SessionJwtService(props, env);
        service.initForTests(props.getSecret());
    }

    @Test
    void shouldRoundTripAccessToken() {
        String token = service.createAccessToken("user-1");
        assertEquals("user-1", service.validateAccessToken(token));
        assertEquals(900, service.getAccessTtlSeconds());
    }

    @Test
    void shouldRejectGarbageToken() {
        assertThrows(InvalidSessionJwtException.class, () -> service.validateAccessToken("not.a.jwt"));
    }
}

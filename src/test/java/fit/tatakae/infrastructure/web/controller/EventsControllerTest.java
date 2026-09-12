package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.infrastructure.sse.SseHub;
import fit.tatakae.infrastructure.web.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventsControllerTest {

    private SseHub sseHub;
    private EventsController controller;

    @BeforeEach
    public void setUp() {
        sseHub = mock(SseHub.class);
        controller = new EventsController(sseHub);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldSubscribeAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedUser("user-123", "apple-sub"));
        SseEmitter emitter = new SseEmitter();
        when(sseHub.subscribe("user-123")).thenReturn(emitter);

        SseEmitter result = controller.subscribe();

        assertSame(emitter, result);
        verify(sseHub).subscribe("user-123");
    }

    @Test
    public void shouldFailWhenUnauthenticated() {
        assertThrows(IllegalStateException.class, () -> controller.subscribe());
        verifyNoInteractions(sseHub);
    }
}

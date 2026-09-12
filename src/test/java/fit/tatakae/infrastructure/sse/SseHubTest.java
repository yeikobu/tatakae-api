package fit.tatakae.infrastructure.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SseHubTest {

    private SseHub hub;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        hub = new SseHub(objectMapper);
    }

    @AfterEach
    public void tearDown() {
        hub.shutdown();
    }

    @Test
    public void shouldRegisterEmitterOnSubscribe() {
        SseEmitter emitter = hub.subscribe("user-a");

        assertNotNull(emitter);
        assertEquals(1, hub.countEmitters("user-a"));
        assertEquals(1, hub.connectedUserCount());
    }

    @Test
    public void shouldAllowMultipleEmittersForSameUser() {
        hub.subscribe("user-a");
        hub.subscribe("user-a");

        assertEquals(2, hub.countEmitters("user-a"));
        assertEquals(1, hub.connectedUserCount());
    }

    @Test
    public void shouldSendEventToSubscribedUser() {
        hub.subscribe("user-a");
        assertDoesNotThrow(() -> hub.sendToUser("user-a", "friend_request", Map.of("id", "1")));
    }

    @Test
    public void shouldIgnoreSendWhenUserHasNoEmitters() {
        assertDoesNotThrow(() -> hub.sendToUser("ghost", "friend_request", Map.of("id", "1")));
        assertEquals(0, hub.countEmitters("ghost"));
    }

    @Test
    public void shouldBroadcastToMultipleUsers() {
        hub.subscribe("user-a");
        hub.subscribe("user-b");

        assertDoesNotThrow(() -> hub.sendToUsers(Arrays.asList("user-a", "user-b", null), "leaderboard_update", Map.of("ok", true)));
    }

    @Test
    public void shouldNoOpWhenRecipientCollectionIsNullOrEmpty() {
        assertDoesNotThrow(() -> hub.sendToUsers(null, "leaderboard_update", Map.of()));
        assertDoesNotThrow(() -> hub.sendToUsers(List.of(), "leaderboard_update", Map.of()));
    }

    @Test
    public void shouldRemoveEmitterExplicitly() {
        SseEmitter emitter = hub.subscribe("user-a");
        hub.removeEmitter("user-a", emitter);

        assertEquals(0, hub.countEmitters("user-a"));
        assertEquals(0, hub.connectedUserCount());
    }

    @Test
    public void shouldNoOpRemoveWhenUserUnknown() {
        SseEmitter emitter = new SseEmitter();
        assertDoesNotThrow(() -> hub.removeEmitter("ghost", emitter));
    }

    @Test
    public void shouldKeepUserEntryUntilLastEmitterRemoved() {
        SseEmitter first = hub.subscribe("user-a");
        SseEmitter second = hub.subscribe("user-a");
        hub.removeEmitter("user-a", first);
        assertEquals(1, hub.countEmitters("user-a"));
        hub.removeEmitter("user-a", second);
        assertEquals(0, hub.countEmitters("user-a"));
    }

    @Test
    public void shouldSkipSendWhenPayloadCannotBeSerialized() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {});
        SseHub failingHub = new SseHub(failingMapper);
        try {
            failingHub.subscribe("user-a");
            assertDoesNotThrow(() -> failingHub.sendToUser("user-a", "friend_request", Map.of("id", "1")));
        } finally {
            failingHub.shutdown();
        }
    }

    @Test
    public void shouldSendHeartbeatsWithoutThrowing() {
        hub.subscribe("user-a");
        assertDoesNotThrow(hub::sendHeartbeats);
    }

    @Test
    public void shouldShutdownAndClearEmitters() {
        hub.subscribe("user-a");
        hub.shutdown();
        assertEquals(0, hub.connectedUserCount());
    }

    @Test
    public void shouldReportZeroEmittersForUnknownUser() {
        assertEquals(0, hub.countEmitters("unknown"));
    }
}

package fit.tatakae.infrastructure.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory registry of SSE emitters keyed by authenticated user id.
 * Thread-safe for concurrent subscribe / broadcast / disconnect.
 */
@Component
public class SseHub {

    private static final Logger logger = LoggerFactory.getLogger(SseHub.class);

    /** 30 minutes; clients should reconnect after timeout. */
    static final long EMITTER_TIMEOUT_MS = 30L * 60L * 1000L;

    static final long HEARTBEAT_INTERVAL_MS = 20_000L;

    private final ConcurrentHashMap<String, CopyOnWriteArraySet<SseEmitter>> emittersByUser =
            new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService heartbeatScheduler;

    public SseHub(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "sse-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
        this.heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeats,
                HEARTBEAT_INTERVAL_MS,
                HEARTBEAT_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userId, ignored -> new CopyOnWriteArraySet<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"ok\":true}", MediaType.APPLICATION_JSON));
        } catch (IOException exception) {
            cleanup.run();
        }

        logger.debug("SSE subscribed userId={} activeEmitters={}", userId, countEmitters(userId));
        return emitter;
    }

    public void sendToUser(String userId, String eventName, Object payload) {
        Set<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            logger.error("Failed to serialize SSE payload for event {}", eventName, exception);
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(json, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException exception) {
                removeEmitter(userId, emitter);
            }
        }
    }

    public void sendToUsers(Collection<String> userIds, String eventName, Object payload) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (String userId : userIds) {
            if (userId != null) {
                sendToUser(userId, eventName, payload);
            }
        }
    }

    int countEmitters(String userId) {
        Set<SseEmitter> emitters = emittersByUser.get(userId);
        return emitters == null ? 0 : emitters.size();
    }

    int connectedUserCount() {
        return emittersByUser.size();
    }

    void removeEmitter(String userId, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId, emitters);
        }
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // already completed / timed out
        }
        logger.debug("SSE removed emitter userId={} remaining={}", userId, countEmitters(userId));
    }

    void sendHeartbeats() {
        emittersByUser.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("keepalive"));
                } catch (IOException | IllegalStateException exception) {
                    removeEmitter(userId, emitter);
                }
            }
        });
    }

    @PreDestroy
    void shutdown() {
        heartbeatScheduler.shutdownNow();
        emittersByUser.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                    // best effort on shutdown
                }
            }
        });
        emittersByUser.clear();
    }
}

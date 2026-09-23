package fit.tatakae.application.usecase;

import fit.tatakae.application.port.ApnsSendResult;
import fit.tatakae.application.port.ApnsSender;
import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.entity.RankingBoard;
import fit.tatakae.domain.repository.DeviceTokenRepository;
import fit.tatakae.domain.repository.RankingAlertPreferenceRepository;
import fit.tatakae.domain.repository.RankingPushLogRepository;
import fit.tatakae.domain.service.RankingOvertake;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeliverRankingPushesUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");
    private static final String USER = "11111111-1111-1111-1111-111111111111";

    @Test
    public void shouldSendAndRememberTheBoard() {
        Harness harness = new Harness();
        harness.devices.add(new DeviceToken("a".repeat(64), USER, false));

        harness.useCase.execute(List.of(new RankingOvertake(USER, RankingBoard.GLOBAL)), NOW);

        assertEquals(List.of(RankingBoard.GLOBAL), harness.sent);
        assertEquals(NOW, harness.log.get(USER + ":GLOBAL"));
    }

    @Test
    public void shouldSkipABoardNotifiedInsideTheWindow() {
        Harness harness = new Harness();
        harness.devices.add(new DeviceToken("a".repeat(64), USER, false));
        harness.log.put(USER + ":GLOBAL", NOW.minus(DeliverRankingPushesUseCase.WINDOW).plusSeconds(1));

        harness.useCase.execute(List.of(new RankingOvertake(USER, RankingBoard.GLOBAL)), NOW);

        assertTrue(harness.sent.isEmpty());
    }

    @Test
    public void shouldSendAgainOnceTheWindowHasElapsed() {
        Harness harness = new Harness();
        harness.devices.add(new DeviceToken("a".repeat(64), USER, false));
        harness.log.put(USER + ":FRIENDS", NOW.minus(DeliverRankingPushesUseCase.WINDOW));

        harness.useCase.execute(List.of(new RankingOvertake(USER, RankingBoard.FRIENDS)), NOW);

        assertEquals(List.of(RankingBoard.FRIENDS), harness.sent);
    }

    @Test
    public void shouldThrottleEachBoardOnItsOwn() {
        Harness harness = new Harness();
        harness.devices.add(new DeviceToken("a".repeat(64), USER, false));
        harness.log.put(USER + ":GLOBAL", NOW.minusSeconds(60));

        harness.useCase.execute(List.of(
                new RankingOvertake(USER, RankingBoard.GLOBAL),
                new RankingOvertake(USER, RankingBoard.COUNTRY)
        ), NOW);

        assertEquals(List.of(RankingBoard.COUNTRY), harness.sent);
    }

    @Test
    public void shouldNotSendWhenAlertsAreOff() {
        Harness harness = new Harness();
        harness.enabled = false;
        harness.devices.add(new DeviceToken("a".repeat(64), USER, false));

        harness.useCase.execute(List.of(new RankingOvertake(USER, RankingBoard.GLOBAL)), NOW);

        assertTrue(harness.sent.isEmpty());
        assertTrue(harness.log.isEmpty());
    }

    @Test
    public void shouldDropAnUnregisteredTokenAndNotMarkAMissedSend() {
        Harness harness = new Harness();
        harness.devices.add(new DeviceToken("b".repeat(64), USER, true));
        harness.result = ApnsSendResult.UNREGISTERED;

        harness.useCase.execute(List.of(new RankingOvertake(USER, RankingBoard.GLOBAL)), NOW);

        assertTrue(harness.devices.isEmpty());
        assertTrue(harness.log.isEmpty());
    }

    @Test
    public void shouldNotMarkTheBoardWhenApnsIsDisabled() {
        Harness harness = new Harness();
        harness.devices.add(new DeviceToken("a".repeat(64), USER, false));
        harness.result = ApnsSendResult.DISABLED;

        harness.useCase.execute(List.of(new RankingOvertake(USER, RankingBoard.COUNTRY)), NOW);

        assertTrue(harness.log.isEmpty());
    }

    private static final class Harness {
        private final List<DeviceToken> devices = new ArrayList<>();
        private final Map<String, Instant> log = new HashMap<>();
        private final List<RankingBoard> sent = new ArrayList<>();
        private boolean enabled = true;
        private ApnsSendResult result = ApnsSendResult.SENT;
        private final DeliverRankingPushesUseCase useCase;

        private Harness() {
            RankingAlertPreferenceRepository preferences = new RankingAlertPreferenceRepository() {
                @Override
                public boolean isEnabled(String userId) {
                    return enabled;
                }

                @Override
                public void setEnabled(String userId, boolean value) {
                    enabled = value;
                }

                @Override
                public void delete(String userId) {
                }
            };
            RankingPushLogRepository pushLog = new RankingPushLogRepository() {
                @Override
                public Optional<Instant> lastSentAt(String userId, RankingBoard board) {
                    return Optional.ofNullable(log.get(userId + ":" + board.name()));
                }

                @Override
                public void markSent(String userId, RankingBoard board, Instant sentAt) {
                    log.put(userId + ":" + board.name(), sentAt);
                }

                @Override
                public void deleteAllOf(String userId) {
                    log.keySet().removeIf(key -> key.startsWith(userId + ":"));
                }
            };
            DeviceTokenRepository tokens = new DeviceTokenRepository() {
                @Override
                public void upsert(String token, String userId, boolean sandbox, Instant updatedAt) {
                }

                @Override
                public List<DeviceToken> findByUserId(String userId) {
                    return List.copyOf(devices);
                }

                @Override
                public void delete(String token) {
                    devices.removeIf(device -> device.token().equals(token));
                }

                @Override
                public void deleteAllOf(String userId) {
                    devices.removeIf(device -> device.userId().equals(userId));
                }
            };
            ApnsSender sender = (device, board) -> {
                if (result == ApnsSendResult.SENT) {
                    sent.add(board);
                }
                return result;
            };
            useCase = new DeliverRankingPushesUseCase(preferences, pushLog, tokens, sender);
        }
    }
}

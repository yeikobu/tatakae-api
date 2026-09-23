package fit.tatakae.application.usecase;

import fit.tatakae.application.port.ApnsSender;
import fit.tatakae.application.port.ApnsSendResult;
import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.entity.RankingBoard;
import fit.tatakae.domain.repository.DeviceTokenRepository;
import fit.tatakae.domain.repository.RankingAlertPreferenceRepository;
import fit.tatakae.domain.repository.RankingPushLogRepository;
import fit.tatakae.domain.service.RankingOvertake;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

public class DeliverRankingPushesUseCase {

    static final Duration WINDOW = Duration.ofHours(6);

    private final RankingAlertPreferenceRepository preferences;
    private final RankingPushLogRepository pushLog;
    private final DeviceTokenRepository deviceTokens;
    private final ApnsSender apnsSender;

    public DeliverRankingPushesUseCase(RankingAlertPreferenceRepository preferences,
                                       RankingPushLogRepository pushLog,
                                       DeviceTokenRepository deviceTokens,
                                       ApnsSender apnsSender) {
        this.preferences = preferences;
        this.pushLog = pushLog;
        this.deviceTokens = deviceTokens;
        this.apnsSender = apnsSender;
    }

    public void execute(List<RankingOvertake> overtakes, Instant now) {
        for (RankingOvertake overtake : new LinkedHashSet<>(overtakes)) {
            deliver(overtake, now);
        }
    }

    private void deliver(RankingOvertake overtake, Instant now) {
        String userId = overtake.userId();
        RankingBoard board = overtake.board();
        if (!preferences.isEnabled(userId) || sentRecently(userId, board, now)) {
            return;
        }
        boolean delivered = false;
        for (DeviceToken device : deviceTokens.findByUserId(userId)) {
            ApnsSendResult result = apnsSender.send(device, board);
            if (result == ApnsSendResult.UNREGISTERED) {
                deviceTokens.delete(device.token());
            } else if (result == ApnsSendResult.SENT) {
                delivered = true;
            }
        }
        // A missed send stays eligible. Marking it would hide the next pass for six hours.
        if (delivered) {
            pushLog.markSent(userId, board, now);
        }
    }

    private boolean sentRecently(String userId, RankingBoard board, Instant now) {
        return pushLog.lastSentAt(userId, board)
                .map(sentAt -> now.isBefore(sentAt.plus(WINDOW)))
                .orElse(false);
    }
}

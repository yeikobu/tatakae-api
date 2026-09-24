package fit.tatakae.infrastructure.push;

import fit.tatakae.application.port.ApnsSendResult;
import fit.tatakae.application.port.ApnsSender;
import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Component
public class FriendRequestPushDelivery {

    private static final Logger log = LoggerFactory.getLogger(FriendRequestPushDelivery.class);

    private final DeviceTokenRepository deviceTokens;
    private final ApnsSender apnsSender;

    public FriendRequestPushDelivery(DeviceTokenRepository deviceTokens, ApnsSender apnsSender) {
        this.deviceTokens = deviceTokens;
        this.apnsSender = apnsSender;
    }

    @Transactional
    public void deliver(String addresseeId, String requesterUsername) {
        deliverTo(addresseeId, "Friend request", device -> apnsSender.sendFriendRequest(device, requesterUsername));
    }

    @Transactional
    public void deliverAccepted(String requesterId, String accepterUsername) {
        deliverTo(requesterId, "Friend accepted", device -> apnsSender.sendFriendRequestAccepted(device, accepterUsername));
    }

    private void deliverTo(String userId, String label, Function<DeviceToken, ApnsSendResult> send) {
        List<DeviceToken> tokens = deviceTokens.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.info("{} push skipped for {}: no device token", label, userId);
            return;
        }
        for (DeviceToken device : tokens) {
            ApnsSendResult result = send.apply(device);
            log.info("{} push for {} {} -> {}", label, userId, device.sandbox() ? "sandbox" : "production", result);
            if (result == ApnsSendResult.UNREGISTERED) {
                deviceTokens.delete(device.token());
            }
        }
    }
}

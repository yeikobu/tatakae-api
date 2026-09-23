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
        List<DeviceToken> tokens = deviceTokens.findByUserId(addresseeId);
        if (tokens.isEmpty()) {
            log.info("Friend request push skipped for {}: no device token", addresseeId);
            return;
        }
        for (DeviceToken device : tokens) {
            ApnsSendResult result = apnsSender.sendFriendRequest(device, requesterUsername);
            log.info("Friend request push for {} {} -> {}", addresseeId, device.sandbox() ? "sandbox" : "production", result);
            if (result == ApnsSendResult.UNREGISTERED) {
                deviceTokens.delete(device.token());
            }
        }
    }
}

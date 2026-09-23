package fit.tatakae.application.usecase;

import fit.tatakae.domain.repository.DeviceTokenRepository;
import fit.tatakae.domain.valueobject.UserId;

public class UnregisterDeviceTokenUseCase {

    private final DeviceTokenRepository deviceTokens;

    public UnregisterDeviceTokenUseCase(DeviceTokenRepository deviceTokens) {
        this.deviceTokens = deviceTokens;
    }

    public void execute(String userId, String token) {
        String identity = UserId.of(userId).asString();
        String normalized;
        try {
            normalized = RegisterDeviceTokenUseCase.normalize(token);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        deviceTokens.findByUserId(identity).stream()
                .filter(device -> device.token().equals(normalized))
                .findFirst()
                .ifPresent(device -> deviceTokens.delete(normalized));
    }
}

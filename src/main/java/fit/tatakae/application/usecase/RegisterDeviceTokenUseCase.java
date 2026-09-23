package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.DeviceTokenRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

import java.time.Clock;
import java.util.regex.Pattern;

public class RegisterDeviceTokenUseCase {

    private static final Pattern HEX_TOKEN = Pattern.compile("[0-9a-f]{64}");

    private final DeviceTokenRepository deviceTokens;
    private final UserRepository userRepository;
    private final Clock clock;

    public RegisterDeviceTokenUseCase(DeviceTokenRepository deviceTokens,
                                      UserRepository userRepository,
                                      Clock clock) {
        this.deviceTokens = deviceTokens;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public void execute(String userId, String token, boolean sandbox) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        String normalized = normalize(token);
        deviceTokens.upsert(normalized, identity, sandbox, clock.instant());
    }

    static String normalize(String token) {
        if (token == null) {
            throw new IllegalArgumentException("device token is required");
        }
        String normalized = token.trim().toLowerCase();
        if (!HEX_TOKEN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("device token must be 64 hex characters");
        }
        return normalized;
    }
}

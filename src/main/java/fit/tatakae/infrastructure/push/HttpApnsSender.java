package fit.tatakae.infrastructure.push;

import fit.tatakae.application.port.ApnsSendResult;
import fit.tatakae.application.port.ApnsSender;
import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.entity.RankingBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Component
public class HttpApnsSender implements ApnsSender {

    private static final Logger log = LoggerFactory.getLogger(HttpApnsSender.class);

    private final ApnsProperties properties;
    private final ApnsJwtFactory productionJwt;
    private final ApnsJwtFactory sandboxJwt;
    private final HttpClient httpClient;

    @Autowired
    public HttpApnsSender(ApnsProperties properties) {
        this(properties, HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build());
    }

    HttpApnsSender(ApnsProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.productionJwt = factory(
                properties.isProductionConfigured(),
                properties.getKeyId(),
                properties.getPrivateKey(),
                properties.getPrivateKeyPath());
        this.sandboxJwt = factory(
                properties.isSandboxConfigured(),
                properties.getSandboxKeyId(),
                properties.getSandboxPrivateKey(),
                properties.getSandboxPrivateKeyPath());
        if (productionJwt == null && sandboxJwt == null) {
            log.info("APNs is not configured; ranking pushes will be skipped");
        }
    }

    private ApnsJwtFactory factory(boolean configured, String keyId, String privateKey, String privateKeyPath) {
        if (!configured) {
            return null;
        }
        String pem = ApnsPrivateKeyParser.readConfigured(privateKey, privateKeyPath);
        return new ApnsJwtFactory(properties.getTeamId(), keyId, ApnsPrivateKeyParser.parse(pem));
    }

    @Override
    public ApnsSendResult send(DeviceToken device, RankingBoard board) {
        ApnsJwtFactory jwtFactory = device.sandbox() ? sandboxJwt : productionJwt;
        if (jwtFactory == null) {
            log.warn("Ranking push skipped: no {} APNs key", device.sandbox() ? "sandbox" : "production");
            return ApnsSendResult.DISABLED;
        }
        String host = device.sandbox() ? "api.sandbox.push.apple.com" : "api.push.apple.com";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + host + "/3/device/" + device.token()))
                .timeout(Duration.ofSeconds(10))
                .header("authorization", "bearer " + jwtFactory.bearer(Instant.now()))
                .header("apns-topic", properties.bundleIdFor(device.sandbox()))
                .header("apns-push-type", "alert")
                .header("apns-priority", "10")
                .POST(HttpRequest.BodyPublishers.ofString(RankingPushPayload.json(board)))
                .build();
        return dispatch(request, board.name());
    }

    @Override
    public ApnsSendResult sendFriendRequest(DeviceToken device, String requesterUsername) {
        ApnsJwtFactory jwtFactory = device.sandbox() ? sandboxJwt : productionJwt;
        if (jwtFactory == null) {
            log.warn("Friend request push skipped: no {} APNs key", device.sandbox() ? "sandbox" : "production");
            return ApnsSendResult.DISABLED;
        }
        String host = device.sandbox() ? "api.sandbox.push.apple.com" : "api.push.apple.com";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + host + "/3/device/" + device.token()))
                .timeout(Duration.ofSeconds(10))
                .header("authorization", "bearer " + jwtFactory.bearer(Instant.now()))
                .header("apns-topic", properties.bundleIdFor(device.sandbox()))
                .header("apns-push-type", "alert")
                .header("apns-priority", "10")
                .POST(HttpRequest.BodyPublishers.ofString(RankingPushPayload.friendRequestJson(requesterUsername)))
                .build();
        return dispatch(request, "friend_request");
    }

    private ApnsSendResult dispatch(HttpRequest request, String label) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return map(response.statusCode(), response.body(), label);
        } catch (IOException exception) {
            log.warn("APNs request failed for {}: {}", label, exception.toString());
            return ApnsSendResult.FAILED;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("APNs request interrupted for {}", label);
            return ApnsSendResult.FAILED;
        }
    }

    private static ApnsSendResult map(int status, String body, String label) {
        if (status == 200) {
            return ApnsSendResult.SENT;
        }
        if (status == 410 || (status == 400 && body != null && (body.contains("BadDeviceToken") || body.contains("Unregistered")))) {
            return ApnsSendResult.UNREGISTERED;
        }
        log.warn("APNs rejected {} with HTTP {} {}", label, status, body == null ? "" : body);
        return ApnsSendResult.FAILED;
    }
}

package fit.tatakae.infrastructure.push;

import fit.tatakae.application.port.FriendRequestPushNotifier;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AsyncFriendRequestPushNotifier implements FriendRequestPushNotifier {

    private static final Logger log = LoggerFactory.getLogger(AsyncFriendRequestPushNotifier.class);

    private final FriendRequestPushDelivery delivery;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "friend-request-push");
        thread.setDaemon(true);
        return thread;
    });

    public AsyncFriendRequestPushNotifier(FriendRequestPushDelivery delivery) {
        this.delivery = delivery;
    }

    @Override
    public void notifyAddressee(String addresseeId, String requesterUsername) {
        executor.execute(() -> {
            try {
                delivery.deliver(addresseeId, requesterUsername);
            } catch (RuntimeException exception) {
                log.warn("Friend request push was not sent: {}", exception.toString());
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}

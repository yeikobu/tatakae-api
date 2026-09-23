package fit.tatakae.infrastructure.push;

import fit.tatakae.application.port.RankingPushNotifier;
import fit.tatakae.application.usecase.DeliverRankingPushesUseCase;
import fit.tatakae.domain.service.RankingOvertake;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AsyncRankingPushNotifier implements RankingPushNotifier {

    private static final Logger log = LoggerFactory.getLogger(AsyncRankingPushNotifier.class);

    private final TransactionalRankingPushDelivery delivery;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ranking-push");
        thread.setDaemon(true);
        return thread;
    });

    public AsyncRankingPushNotifier(TransactionalRankingPushDelivery delivery) {
        this.delivery = delivery;
    }

    @Override
    public void notifyOvertaken(List<RankingOvertake> overtakes) {
        if (overtakes.isEmpty()) {
            return;
        }
        List<RankingOvertake> copy = List.copyOf(overtakes);
        executor.execute(() -> {
            try {
                delivery.deliver(copy);
            } catch (RuntimeException exception) {
                log.warn("Ranking push was not sent: {}", exception.toString());
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}

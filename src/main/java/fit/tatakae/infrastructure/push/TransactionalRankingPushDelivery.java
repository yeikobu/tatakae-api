package fit.tatakae.infrastructure.push;

import fit.tatakae.application.usecase.DeliverRankingPushesUseCase;
import fit.tatakae.domain.service.RankingOvertake;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Component
public class TransactionalRankingPushDelivery {

    private final DeliverRankingPushesUseCase useCase;
    private final Clock clock;

    public TransactionalRankingPushDelivery(DeliverRankingPushesUseCase useCase, Clock clock) {
        this.useCase = useCase;
        this.clock = clock;
    }

    @Transactional
    public void deliver(List<RankingOvertake> overtakes) {
        useCase.execute(overtakes, clock.instant());
    }
}

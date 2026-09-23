package fit.tatakae.application.port;

import fit.tatakae.domain.service.RankingOvertake;

import java.util.List;

public interface RankingPushNotifier {
    void notifyOvertaken(List<RankingOvertake> overtakes);
}

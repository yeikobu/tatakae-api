package fit.tatakae.application.usecase;

import fit.tatakae.domain.repository.RankingAlertPreferenceRepository;
import fit.tatakae.domain.valueobject.UserId;

public class GetRankingAlertsUseCase {

    private final RankingAlertPreferenceRepository preferences;

    public GetRankingAlertsUseCase(RankingAlertPreferenceRepository preferences) {
        this.preferences = preferences;
    }

    public boolean execute(String userId) {
        return preferences.isEnabled(UserId.of(userId).asString());
    }
}

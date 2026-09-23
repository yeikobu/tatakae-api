package fit.tatakae.domain.repository;

public interface RankingAlertPreferenceRepository {
    /**
     * Athletes with no stored choice still receive ranking pushes.
     */
    boolean isEnabled(String userId);

    void setEnabled(String userId, boolean enabled);

    void delete(String userId);
}

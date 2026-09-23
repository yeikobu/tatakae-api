package fit.tatakae.domain.service;

import fit.tatakae.domain.entity.RankingBoard;
import fit.tatakae.domain.entity.TrainingSession;
import fit.tatakae.domain.entity.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Who just lost a place because of one new mark.
 * Same ordering the boards already use: more reps, and on a tie the older session.
 */
public final class RankingOvertakeDetector {

    private RankingOvertakeDetector() {
    }

    public static List<RankingOvertake> detect(TrainingSession fresh,
                                               List<TrainingSession> existing,
                                               Set<String> acceptedFriendIds) {
        Map<String, TrainingSession> bestByUser = new LinkedHashMap<>();
        for (TrainingSession session : existing) {
            if (!session.isForExercise(fresh.getExercise())) {
                continue;
            }
            String userId = session.getUser().getUserId();
            TrainingSession current = bestByUser.get(userId);
            bestByUser.put(userId, current == null ? session : better(current, session));
        }

        String scorerId = fresh.getUser().getUserId();
        TrainingSession previous = bestByUser.get(scorerId);
        List<RankingOvertake> overtakes = new ArrayList<>();
        for (TrainingSession rival : bestByUser.values()) {
            if (rival.getUser().getUserId().equals(scorerId) || !justPassed(previous, fresh, rival)) {
                continue;
            }
            if (bothPublic(fresh, rival)) {
                overtakes.add(new RankingOvertake(rival.getUser().getUserId(), RankingBoard.GLOBAL));
                if (sameRankedCountry(fresh.getUser(), rival.getUser())) {
                    overtakes.add(new RankingOvertake(rival.getUser().getUserId(), RankingBoard.COUNTRY));
                }
            }
            if (acceptedFriendIds.contains(rival.getUser().getUserId())) {
                overtakes.add(new RankingOvertake(rival.getUser().getUserId(), RankingBoard.FRIENDS));
            }
        }
        return List.copyOf(overtakes);
    }

    // A missing or placeholder country is not a local board. The app refuses to ask for one.
    static boolean isRankedCountry(String country) {
        if (country == null) {
            return false;
        }
        String trimmed = country.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "unknown", "null", "n/a" -> false;
            default -> true;
        };
    }

    private static boolean justPassed(TrainingSession previous, TrainingSession fresh, TrainingSession rival) {
        if (!fresh.outperforms(rival)) {
            return false;
        }
        return previous == null || !previous.outperforms(rival);
    }

    private static TrainingSession better(TrainingSession first, TrainingSession second) {
        return first.outperforms(second) ? first : second;
    }

    private static boolean bothPublic(TrainingSession fresh, TrainingSession rival) {
        return fresh.getUser().isPublic() && rival.getUser().isPublic();
    }

    private static boolean sameRankedCountry(User scorer, User rival) {
        if (!isRankedCountry(scorer.getCountry()) || !isRankedCountry(rival.getCountry())) {
            return false;
        }
        return scorer.isFromCountry(rival.getCountry());
    }
}

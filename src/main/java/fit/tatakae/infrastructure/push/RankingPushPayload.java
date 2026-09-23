package fit.tatakae.infrastructure.push;

import fit.tatakae.domain.entity.RankingBoard;

public final class RankingPushPayload {

    private RankingPushPayload() {
    }

    public static String locKey(RankingBoard board) {
        return switch (board) {
            case GLOBAL -> "Someone passed you on the global ranking.";
            case COUNTRY -> "Someone passed you on the local ranking.";
            case FRIENDS -> "A friend passed you on your friends ranking.";
        };
    }

    public static String json(RankingBoard board) {
        return alert(locKey(board), "\"type\":\"ranking\",\"scope\":\"" + board.name() + "\"");
    }

    public static String friendRequestJson(String requesterUsername) {
        return "{\"aps\":{\"alert\":{\"loc-key\":\"%@ sent you a friend request.\",\"loc-args\":["
                + jsonString(requesterUsername) + "]},\"sound\":\"default\"},\"type\":\"friend_request\"}";
    }

    private static String alert(String locKey, String extra) {
        return "{\"aps\":{\"alert\":{\"loc-key\":\"" + locKey + "\"},\"sound\":\"default\"}," + extra + "}";
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

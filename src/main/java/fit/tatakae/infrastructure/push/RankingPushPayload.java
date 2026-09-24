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
        return withUsername("%@ sent you a friend request.", requesterUsername, "friend_request");
    }

    public static String friendRequestAcceptedJson(String accepterUsername) {
        return withUsername("%@ accepted your friend request.", accepterUsername, "friend_accepted");
    }

    private static String withUsername(String locKey, String username, String type) {
        return "{\"aps\":{\"alert\":{\"loc-key\":\"" + locKey + "\",\"loc-args\":["
                + jsonString(username) + "]},\"sound\":\"default\"},\"type\":\"" + type + "\"}";
    }

    private static String alert(String locKey, String extra) {
        return "{\"aps\":{\"alert\":{\"loc-key\":\"" + locKey + "\"},\"sound\":\"default\"}," + extra + "}";
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

package fit.tatakae.application.port;

public interface FriendRequestPushNotifier {
    void notifyAddressee(String addresseeId, String requesterUsername);
}

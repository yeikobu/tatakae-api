package fit.tatakae.application.port;

import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.entity.RankingBoard;

public interface ApnsSender {
    ApnsSendResult send(DeviceToken device, RankingBoard board);

    default ApnsSendResult sendFriendRequest(DeviceToken device, String requesterUsername) {
        return ApnsSendResult.DISABLED;
    }

    default ApnsSendResult sendFriendRequestAccepted(DeviceToken device, String accepterUsername) {
        return ApnsSendResult.DISABLED;
    }
}

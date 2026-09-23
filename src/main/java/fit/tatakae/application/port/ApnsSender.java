package fit.tatakae.application.port;

import fit.tatakae.domain.entity.DeviceToken;
import fit.tatakae.domain.entity.RankingBoard;

public interface ApnsSender {
    ApnsSendResult send(DeviceToken device, RankingBoard board);
}

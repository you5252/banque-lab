package com.banquelab.account.application.port.out;

import com.banquelab.account.domain.model.TransferCompleted;

public interface PublishTransferEventPort {
    void publish(TransferCompleted event);
}

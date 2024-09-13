package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {
    private String recipientPubKey;
    private String businessAcctSecretKeyBase58;
    private String mintPubKey;
    private long amount;
    private int decimals;
}

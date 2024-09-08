package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequest {
    public String recipientPubKey;
    public String businessAcctSecretKeyBase58;
    public String mintPubKey;
    public long amount;
    public int decimals;
}

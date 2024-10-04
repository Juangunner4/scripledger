package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionRequest {
    private String transaction;
    private String userAcctSecretKeyBase58;

}

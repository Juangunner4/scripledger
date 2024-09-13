package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MintTokenResponse {
    private String mintPubKey;
    private String initialSupplyTxnHash;
}

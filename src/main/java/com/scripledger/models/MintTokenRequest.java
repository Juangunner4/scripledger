package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MintTokenRequest {
    private String businessAcctSecretKeyBase58;
    private long initialSupply;
    private int decimals;
    private String tokenName;
    private String logoLink;
    private String unit;
}


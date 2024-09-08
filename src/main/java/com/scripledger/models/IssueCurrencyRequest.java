package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCurrencyRequest {
    public String businessAcctSecretKeyBase58;
    public long initialSupply;
    public int decimals;
}


package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCurrencyResponse {
    public String mintPubKey;
    public String initialSupplyTxnHash;
}

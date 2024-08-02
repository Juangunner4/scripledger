package com.scripledger.models;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MintTokensRequest {
    private String brandId;
    private String ownerPublicKeyStr;
    private Long amount;
    private String tokenName;
}

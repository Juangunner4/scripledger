package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GiftCardRequest {
    private String cardSerial;
    private String brandName;
    private String tokenId;
}
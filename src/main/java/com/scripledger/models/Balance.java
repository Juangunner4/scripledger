package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Balance {
    private String tokenName;
    private Long amount;
}
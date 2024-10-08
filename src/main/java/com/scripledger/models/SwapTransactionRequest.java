package com.scripledger.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwapTransactionRequest {
    private String userPubKey;
    private String sourceMintPubKey;
    private String destinationMintPubKey;
    private String tokenSwapProgramPubKey;
    private String amountIn;
}

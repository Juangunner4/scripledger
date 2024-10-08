package com.scripledger.collections;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

@MongoEntity(collection = "tokenSwap")
@Getter
@Setter
public class TokenSwap {
    private String sourceMintPubKey;
    private String destinationMintPubKey;
    private String tokenSwapProgramPubKey;
}

package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

@MongoEntity(collection="tokens")
@Getter
@Setter
public class Token {
    private String mintPublicKey;
    private Balance balance;
}

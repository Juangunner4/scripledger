package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@MongoEntity(collection="tokens")
@Getter
@Setter
public class Token {
    private ObjectId tokenId;
    private String publicKey;
    private Balance balance;
}

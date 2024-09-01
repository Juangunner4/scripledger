package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@MongoEntity(collection="tokens")
@Getter
@Setter
public class Token {
    private ObjectId id;
    private String ownerPublicKey;
    private String mintPublicKey;
}

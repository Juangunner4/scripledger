package com.scripledger.collections;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@MongoEntity(collection="tokens")
@Getter
@Setter
public class Token {
    private ObjectId id;
    private String tokenName;
    private String logoLink;
    private String unit;
    private String ownerPublicKey;
    private String mintPublicKey;
}

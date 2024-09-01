package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection="brands")
@Getter
@Setter
public class Brand {
    private ObjectId id;
    private String brandName;
    private String url;
    private String ownerPublicKey;
    private List<Token> tokens;
}

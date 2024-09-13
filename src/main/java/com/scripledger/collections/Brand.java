package com.scripledger.collections;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.smallrye.common.constraint.NotNull;
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
    @NotNull
    private String ownerPublicKey;
    private List<Token> tokens;

    public Brand() {

    }
}

package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@MongoEntity(collection="brands")
@Getter
@Setter
public class Brand {
    private String brandName;
    private String url;
    private List<Token> tokens;
}

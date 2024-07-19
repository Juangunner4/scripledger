package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Getter;
import lombok.Setter;

@MongoEntity(collection="accounts")
@Getter
@Setter
public class AdminActionRequest extends ReactivePanacheMongoEntityBase {
    private String accountId;
    private String actionType;
    private String category;
    private String note;

    public AdminActionRequest() {

    }

}
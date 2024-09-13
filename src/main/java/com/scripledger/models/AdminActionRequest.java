package com.scripledger.models;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminActionRequest extends ReactivePanacheMongoEntityBase {
    private String userPubKey;
    private String businessAcctSecretKeyBase58;
    private String mintPubKey;
    private String actionType;

}
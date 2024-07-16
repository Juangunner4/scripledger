package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@MongoEntity(collection="accounts")
@Getter
@Setter
public class UserAccount extends ReactivePanacheMongoEntityBase {
    private String username;
    private String publicKey;
    private String kycStatus;
    private Date firstTxnTimestamp;
    private List<Balance> balances;
    private Map<String, Map<String, String>> thirdPartyAccountProfile;

    public UserAccount() {

    }

}
package com.scripledger.collections;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

@MongoEntity(collection="accounts")
@Getter
@Setter
public class UserAccount extends ReactivePanacheMongoEntityBase {
    private ObjectId id;
    private String username;
    private String email;
    private String phoneNumber;
    private String accountPublicKey;
    private String kycStatus;
    private Date firstTxnTimestamp;
    private Map<String, Map<String, String>> thirdPartyAccountProfile;
    private ObjectId alternateAccountId;
    private String customerProfile;
    private String actionType;
    private String category;
    private String note;

    public UserAccount() {

    }

}
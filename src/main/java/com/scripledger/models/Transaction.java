package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;


import java.util.Date;

@MongoEntity(collection="transactions")
@Getter
@Setter
public class Transaction {
    private ObjectId id;
    private String transactionId;
    private String tokenId;
    private String senderPublicKey;
    private String recipientPublicKey;
    private double amount;
    private Date timestamp;
    private String transactionLabel;
    private String note;
    private BlockchainDetails blockchainDetails;

    @Getter
    @Setter
    public static class BlockchainDetails {
        private String transactionHash;
    }
}

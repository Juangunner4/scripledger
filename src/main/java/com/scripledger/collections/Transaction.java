package com.scripledger.collections;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.smallrye.common.constraint.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;

@MongoEntity(collection="transactions")
@Getter
@Setter
public class Transaction {
    private ObjectId id;
    private String senderPubKey;
    private String recipientPubKey;
    private String mintPubKey;
    private Date timestamp;
    @NotNull
    private String transactionHash;
    private String transactionType; // UserTransfer, GiftCardRedemption, Refund...

}

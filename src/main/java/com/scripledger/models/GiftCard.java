package com.scripledger.models;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Getter;
import lombok.Setter;

@MongoEntity(collection="giftcards")
@Getter
@Setter
public class GiftCard extends ReactivePanacheMongoEntityBase {
    private String cardSerial;
    private String brandName;
    private String tokenId;
    private Balance balance;
    private String blockchain;
    private String publicKey;
    private String secretKey;
    private String publicQR;
    private String secretQR;
    private String status; // e.g., active, redeemed, expired

    public GiftCard() {
    }
}
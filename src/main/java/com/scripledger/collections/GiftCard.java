package com.scripledger.collections;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@MongoEntity(collection="giftcards")
@Getter
@Setter
public class GiftCard extends ReactivePanacheMongoEntityBase {
    private String cardSerial;
    private String brandName;
    private String tokenId;
    private String blockchain;
    private String publicKey;
    private String secretKey;
    private String publicQR;
    private String secretQR;
    private String status; // e.g., active, redeemed, expired
    private Date timestamp;

    public GiftCard() {
    }
}
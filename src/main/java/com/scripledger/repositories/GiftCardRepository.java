package com.scripledger.repositories;

import com.scripledger.models.GiftCard;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GiftCardRepository implements ReactivePanacheMongoRepository<GiftCard> {
}
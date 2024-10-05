package com.scripledger.repositories;

import com.scripledger.collections.GiftCard;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GiftCardRepository implements ReactivePanacheMongoRepository<GiftCard> {

    public Uni<GiftCard> findBySerial(String cardSerial) {
        return find("cardSerial", cardSerial).firstResult();
    }

    public Uni<GiftCard> findByPublicKey(String publicKey) {
        return find("publicKey", publicKey).firstResult();
    }
}

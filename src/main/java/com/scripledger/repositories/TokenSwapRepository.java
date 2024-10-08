package com.scripledger.repositories;

import com.scripledger.collections.TokenSwap;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenSwapRepository implements ReactivePanacheMongoRepository<TokenSwap> {
    public Uni<TokenSwap> findByMintKeys(String sourceMintPubKey, String destinationMintPubKey) {
        return find("sourceMintPubKey = ?1 and destinationMintPubKey = ?2", sourceMintPubKey, destinationMintPubKey).firstResult();
    }
}

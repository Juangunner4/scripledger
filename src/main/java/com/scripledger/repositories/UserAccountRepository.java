package com.scripledger.repositories;

import com.scripledger.models.UserAccount;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserAccountRepository implements ReactivePanacheMongoRepository<UserAccount> {
    public Uni<UserAccount> findByPublicKey(String publicKey) {
        return find("publicKey", publicKey).firstResult();
    }
}
package com.scripledger.repositories;

import com.scripledger.collections.Token;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenRepository implements ReactivePanacheMongoRepository<Token> {
}

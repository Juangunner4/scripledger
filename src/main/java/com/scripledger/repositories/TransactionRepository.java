package com.scripledger.repositories;

import com.scripledger.models.Transaction;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository implements ReactivePanacheMongoRepository<Transaction> {

}

package com.scripledger.services;

import com.scripledger.models.Transaction;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    public Uni<Transaction> createTransaction(Transaction transaction) {
        return Uni.createFrom().item(() -> {
            transactionRepository.persist(transaction);
            return transaction;
        });
    }

    public Uni<Transaction> getTransaction(String transactionId) {
        return Uni.createFrom().item(() ->
                transactionRepository.find("transactionId", transactionId).firstResult()
        );
    }
}

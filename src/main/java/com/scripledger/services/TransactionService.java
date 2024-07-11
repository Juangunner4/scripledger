package com.scripledger.services;

import com.scripledger.models.Transaction;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    public Uni<Transaction> createTransaction(Transaction transaction) {
        LOGGER.info("Service: Persisting transaction: " + transaction);
        return Uni.createFrom().item(() -> {
            transactionRepository.persist(transaction);
            LOGGER.info("Service: Transaction persisted: " + transaction);
            return transaction;
        }).onFailure().invoke(ex -> LOGGER.error("Failed to persist transaction", ex));
    }

    public Uni<Transaction> getTransaction(String transactionId) {
        LOGGER.info("Service: Retrieving transaction with ID: " + transactionId);
        return Uni.createFrom().item(() -> {
            Transaction transaction = transactionRepository.find("transactionId", transactionId).firstResult();
            LOGGER.info("Service: Transaction retrieved: " + transaction);
            return transaction;
        }).onFailure().invoke(ex -> LOGGER.error("Failed to retrieve transaction", ex));
    }
}

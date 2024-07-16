package com.scripledger.services;

import com.scripledger.models.Transaction;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    public Uni<Transaction> createTransaction(Transaction transaction) {
        LOGGER.info("Service: Persisting transaction: " + transaction);
        return transactionRepository.persist(transaction);
    }

    public Uni<Transaction> getTransaction(String transactionId) {
        ObjectId objectId = new ObjectId(transactionId);
        LOGGER.info("Service: Retrieving transaction with ID: " + transactionId);
        return transactionRepository.findById(objectId);
    }
}

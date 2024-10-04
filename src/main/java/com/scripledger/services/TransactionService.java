package com.scripledger.services;

import com.scripledger.collections.Transaction;
import com.scripledger.collections.UserAccount;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    UserAccountService userAccountService;

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    public Uni<Transaction> storeTransaction(Transaction transaction) {
        return checkForUserAccount(transaction.getSenderPubKey()).flatMap(userAccount -> {
            transaction.setTransactionHash(transaction.getTransactionHash());
            transaction.setSenderPubKey(userAccount.getAccountPublicKey());
            transaction.setTimestamp(new Date());
            LOGGER.info("Service: Persisting transaction: " + transaction.getTransactionHash());
            return transactionRepository.persist(transaction);
        });
    }

    public Uni<List<Transaction>> getAllTransactionsForUser(String publicKey){
        return checkForUserAccount(publicKey).flatMap(user -> transactionRepository.find("publicKey", user.getAccountPublicKey()).list());
    }

    public Uni<Transaction> getTransaction(String transactionId) {
        ObjectId objectId = new ObjectId(transactionId);
        LOGGER.info("Service: Retrieving transaction with ID: " + transactionId);
        return transactionRepository.findById(objectId);
    }

    private Uni<UserAccount> checkForUserAccount(String publicKey) {
        return userAccountService.getAccountByPublicKey(publicKey)
                .flatMap(userAccount -> {
                    if (userAccount == null) {
                        LOGGER.warn("No user account found with publicKey: " + publicKey);
                        return Uni.createFrom().failure(new RuntimeException("The publicKey " + publicKey + " is not in our system. We cannot fetch transactions for this user."));
                    }
                    return Uni.createFrom().item(userAccount);
                });
    }

}

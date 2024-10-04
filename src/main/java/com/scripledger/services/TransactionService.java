package com.scripledger.services;

import com.scripledger.collections.Transaction;
import com.scripledger.collections.UserAccount;
import com.scripledger.models.TransactionRequest;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.SystemProgram;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    UserAccountService userAccountService;

    @Inject
    SolanaService solanaService;

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    public Uni<Transaction> storeTransaction(Transaction transaction) {
        return checkForUserAccount(transaction.getSenderPubKey()).flatMap(userAccount -> {
            transaction.setSenderPubKey(userAccount.getAccountPublicKey());
            transaction.setTimestamp(new Date());
            LOGGER.info("Service: Persisting transaction: " + transaction.getTransactionHash());
            return transactionRepository.persist(transaction);
        });
    }

    public Uni<Transaction> signTransaction(TransactionRequest transactionRequest) {
        Account userAccount = solanaService.extractAccountFromPrivateKey(transactionRequest.getUserAcctSecretKeyBase58());

        return checkForUserAccount(userAccount.getPublicKey().toBase58())
                .onItem().transformToUni(account -> {
                    try {
                        Account owner = solanaService.getOwnerAccount();

                        org.p2p.solanaj.core.Transaction transaction = new org.p2p.solanaj.core.Transaction();

                        TransactionInstruction instruction = SystemProgram.transfer(
                                userAccount.getPublicKey(),
                                owner.getPublicKey(),
                                1000
                        );
                        transaction.addInstruction(instruction);

                        List<Account> signers = Arrays.asList(owner, userAccount);

                        return solanaService.signAndSendTransaction(transaction, signers)
                                .onItem().transformToUni(signature -> {
                                    Transaction transactionRecord = new Transaction();
                                    transactionRecord.setTransactionHash(signature);
                                    transactionRecord.setSenderPubKey(String.valueOf(account.getAccountPublicKey()));
                                    transactionRecord.setTimestamp(new Date());
                                    transactionRecord.setTransactionType("signTransaction");
                                    return storeTransaction(transactionRecord);
                                });
                    } catch (Exception e) {
                        LOGGER.error("Error signing transaction", e);
                        return Uni.createFrom().failure(new RuntimeException("Transaction failed", e));
                    }
                })
                .onFailure().invoke(th -> LOGGER.error("Failed to sign transaction: " + th.getMessage()));
    }

    public Uni<List<Transaction>> getAllTransactionsForUser(String publicKey) {
        return checkForUserAccount(publicKey).flatMap(user ->
                transactionRepository.find("publicKey", user.getAccountPublicKey()).list()
        );
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
                        return Uni.createFrom().failure(new RuntimeException("The publicKey " + publicKey + " is not in our system. We cannot proceed with the transaction."));
                    }
                    return Uni.createFrom().item(userAccount);
                });
    }
}

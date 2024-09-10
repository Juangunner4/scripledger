package com.scripledger.services;

import com.scripledger.collections.Transaction;
import com.scripledger.config.NodeClient;
import com.scripledger.models.*;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Date;

@ApplicationScoped
public class NodeService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    @RestClient
    NodeClient nodeClient;

    private static final Logger LOGGER = Logger.getLogger(UserAccountService.class);

    public Uni<IssueCurrencyResponse> issueBusinessCurrency(IssueCurrencyRequest request) {
        LOGGER.info("Issuing business currency with Initial Supply: " + request.getInitialSupply() + " and Decimals: " + request.getDecimals());
        return nodeClient.issueBusinessCurrency(request)
                .onItem().invoke(response -> {
                    Transaction transaction = new Transaction();
                    transaction.setPublicKey(response.getMintPubKey());
                    transaction.setTransactionHash(response.getInitialSupplyTxnHash());
                    transaction.setTransactionType("issueBusinessCurrency");
                    transaction.setTimestamp(new Date());
                    LOGGER.info("Business mintPubKey: " + response.getMintPubKey());
                    transactionRepository.persist(transaction);
                })
                .onFailure().invoke(th -> LOGGER.error("Error when processing Node.js response: " + th.getMessage()));
    }

    public Uni<TransactionResponse> transactionFromBusinessAccount(TransactionRequest request) {
        LOGGER.info("");
        return nodeClient.transactionFromBusinessAccount(request)
                .onItem().invoke(response -> {
                    Transaction transaction = new Transaction();
                    transaction.setPublicKey(request.getMintPubKey());
                    transaction.setTransactionHash(response.getTransactionHash());
                    transaction.setTransactionType("transactionFromBusinessAccount");
                    transaction.setTimestamp(new Date());
                    LOGGER.info("transaction from Business account to recipient: " + request.getRecipientPubKey());
                    transactionRepository.persist(transaction);
                });
    }

    public Uni<AdminActionResponse> adminActions(AdminActionRequest request) {
        LOGGER.info("");
        return nodeClient.adminActions(request)
                .onItem().invoke(response -> {
                    Transaction transaction = new Transaction();
                    transaction.setPublicKey(request.getAccountPublicKey());
                    transaction.setTransactionHash(response.getTransactionHash());
                    transaction.setTransactionType("AdminAction"+request.getActionType());
                    transaction.setTimestamp(new Date());
                    LOGGER.info("admin Action : " + request.getActionType());
                    transactionRepository.persist(transaction);
                });
    }
}
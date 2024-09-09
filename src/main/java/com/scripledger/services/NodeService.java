package com.scripledger.services;

import com.scripledger.config.NodeClient;
import com.scripledger.models.IssueCurrencyRequest;
import com.scripledger.models.IssueCurrencyResponse;
import com.scripledger.collections.Transaction;
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
                });
    }
}
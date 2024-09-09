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

import java.util.Date;

@ApplicationScoped
public class NodeService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    @RestClient
    NodeClient nodeClient;

    public Uni<IssueCurrencyResponse> issueBusinessCurrency(IssueCurrencyRequest request) {
        return nodeClient.issueBusinessCurrency(request)
                .onItem().invoke(response -> {
                    // Create a new Transaction object
                    Transaction transaction = new Transaction();
                    transaction.setPublicKey(response.getMintPubKey());
                    transaction.setTransactionHash(response.getInitialSupplyTxnHash());
                    transaction.setTransactionType("issueBusinessCurrency");
                    transaction.setTimestamp(new Date());

                    // Save the transaction to MongoDB
                    transactionRepository.persist(transaction);
                });
    }
}
package com.scripledger.services;

import com.scripledger.collections.Transaction;
import com.scripledger.config.NodeClient;
import com.scripledger.models.*;
import com.scripledger.repositories.TransactionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bitcoinj.core.Base58;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;

import java.util.Date;

@ApplicationScoped
public class NodeService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    @RestClient
    NodeClient nodeClient;

    @Inject
    UserAccountService userAccountService;

    @Inject
    TokenService tokenService;

    @Inject
    BrandsService brandsService;

    private static final Logger LOGGER = Logger.getLogger(UserAccountService.class);

    public Uni<MintTokenResponse> mintBusinessTokens(MintTokenRequest request) {
        LOGGER.info("Issuing business currency with Initial Supply: " + request.getInitialSupply() + " and Decimals: " + request.getDecimals());

        try {
            String publicKey = extractPublicKeyFromPrivateKey(request.getBusinessAcctSecretKeyBase58());

            return userAccountService.getAccountByPublicKey(publicKey)
                    .onItem().transformToUni(userAccount -> {
                        if (userAccount != null) {
                            LOGGER.info("User found with public key: " + publicKey);

                            return nodeClient.issueBusinessCurrency(request)
                                    .onItem().transformToUni(response -> {
                                        LOGGER.info("Business mintPubKey: " + response.getMintPubKey());
                                        Transaction transaction = new Transaction();
                                        transaction.setPublicKey(response.getMintPubKey());
                                        transaction.setTransactionHash(response.getInitialSupplyTxnHash());
                                        transaction.setTransactionType("issueBusinessCurrency");
                                        transaction.setTimestamp(new Date());

                                        return transactionRepository.persist(transaction)
                                                .onItem().invoke(() -> LOGGER.info("Transaction persisted to the DB"))
                                                .onFailure().invoke(th -> LOGGER.error("Failed to persist transaction: " + th.getMessage()))
                                                .onItem().transformToUni(v -> tokenService.createToken(publicKey, response.getMintPubKey())
                                                        .onItem().invoke(() -> LOGGER.info("Token created with mintPubKey: " + response.getMintPubKey()))
                                                        .onItem().transform(token -> {
                                                            brandsService.updateBrandWithToken(token);
                                                            return response;
                                                        }));
                                    })
                                    .onFailure().invoke(th -> LOGGER.error("Error when processing Node.js response: " + th.getMessage()));

                        } else {
                            LOGGER.error("User with public key: " + publicKey + " not found in the system.");
                            return Uni.createFrom().failure(new RuntimeException("User not found"));
                        }
                    })
                    .onFailure().invoke(th -> LOGGER.error("Error when checking user account: " + th.getMessage()));

        } catch (Exception e) {
            LOGGER.error("Error extracting public key: " + e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }


    public Uni<TransactionResponse> transferFromBusiness(TransferRequest request) {
        LOGGER.info("Processing transaction from business account to recipient: " + request.getRecipientPubKey());

        try {
            String publicKey = extractPublicKeyFromPrivateKey(request.getBusinessAcctSecretKeyBase58());

            return userAccountService.getAccountByPublicKey(publicKey)
                    .onItem().transformToUni(userAccount -> {
                        if (userAccount != null) {
                            return nodeClient.transactionFromBusinessAccount(request)
                                    .onItem().invoke(response -> {
                                        Transaction transaction = new Transaction();
                                        transaction.setPublicKey(publicKey);
                                        transaction.setTransactionHash(response.getTransactionHash());
                                        transaction.setTransactionType("transactionFromBusinessAccount");
                                        transaction.setTimestamp(new Date());
                                        LOGGER.info("Transaction from business account to recipient: " + transaction.getTransactionHash());
                                        transactionRepository.persist(transaction);
                                    });
                        } else {
                            LOGGER.error("User with public key: " + publicKey + " not found in the system.");
                            return Uni.createFrom().failure(new RuntimeException("User not found"));
                        }
                    })
                    .onFailure().invoke(th -> LOGGER.error("Error when checking user account: " + th.getMessage()));

        } catch (Exception e) {
            LOGGER.error("Error extracting public key: " + e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }


    public Uni<AdminActionResponse> adminActions(AdminActionRequest request) {
        LOGGER.info("Processing admin action for account: " + request.getUserPubKey());

        try {
            String publicKey = extractPublicKeyFromPrivateKey(request.getBusinessAcctSecretKeyBase58());

            return userAccountService.getAccountByPublicKey(publicKey)
                    .onItem().transformToUni(userAccount -> {
                        if (userAccount != null) {
                            return nodeClient.adminActions(request)
                                    .onItem().invoke(response -> {
                                        Transaction transaction = new Transaction();
                                        transaction.setPublicKey(publicKey);
                                        transaction.setTransactionHash(response.getTransactionHash());
                                        transaction.setTransactionType("AdminAction" + request.getActionType());
                                        transaction.setTimestamp(new Date());
                                        LOGGER.info("Transaction Admin action: " + transaction.getTransactionHash());
                                        transactionRepository.persist(transaction);
                                    });
                        } else {
                            LOGGER.error("User with public key: " + publicKey + " not found in the system.");
                            return Uni.createFrom().failure(new RuntimeException("User not found"));
                        }
                    })
                    .onFailure().invoke(th -> LOGGER.error("Error when checking user account: " + th.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Error extracting public key: " + e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }

    private String extractPublicKeyFromPrivateKey(String base58PrivateKey) {
        byte[] secretKeyBytes = Base58.decode(base58PrivateKey);
        Account account = new Account(secretKeyBytes);
        return account.getPublicKey().toBase58();
    }
}
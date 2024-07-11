package com.scripledger.services;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SolanaService {

    private static final Logger LOGGER = Logger.getLogger(SolanaService.class);
    private static final String SOLANA_RPC_URL = "https://api.devnet.solana.com";
    private RpcClient client;

    public SolanaService() {
        this.client = new RpcClient(SOLANA_RPC_URL);
    }

    public Account createAccount() {
        Account newAccount = new Account();
        LOGGER.info("Created new account with public key: " + newAccount.getPublicKey().toString());
        return newAccount;
    }

    // Converts a public key string into a PublicKey object
    public PublicKey getPublicKey(String publicKeyStr) {
        try {
            return new PublicKey(publicKeyStr);
        } catch (Exception e) {
            LOGGER.error("Failed to create public key", e);
            return null;
        }
    }

    // Sends a transaction to the Solana blockchain using a provided signer account
    public String sendTransaction(Transaction transaction, Account signer) {
        try {
            String signature = client.getApi().sendTransaction(transaction, signer);
            LOGGER.info("Transaction sent with signature: " + signature);
            return signature;
        } catch (RpcException e) {
            LOGGER.error("Failed to send transaction", e);
            return null;
        }
    }
}

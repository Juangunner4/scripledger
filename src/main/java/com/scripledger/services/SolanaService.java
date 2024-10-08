package com.scripledger.services;

import com.scripledger.util.FileUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bitcoinj.core.Base58;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class SolanaService {
    private static final Logger LOGGER = Logger.getLogger(SolanaService.class);
    private static final String SOLANA_RPC_URL = "https://api.devnet.solana.com";
    private final RpcClient client;

    @Inject
    @ConfigProperty(name = "solana.privateOwnerKeyFile")
    String privateOwnerKeyFile;

    public SolanaService() {
        this.client = new RpcClient(SOLANA_RPC_URL);
    }

    public Uni<String> signAndSendTransaction(Transaction transaction, List<Account> signers) {
        return Uni.createFrom().item(() -> {
            try {
                String signature = client.getApi().sendTransaction(transaction, signers);
                LOGGER.info("Transaction sent with signature: " + signature);
                return signature;
            } catch (RpcException e) {
                LOGGER.error("Failed to send transaction", e);
                throw new RuntimeException("Failed to send transaction", e);
            }
        });
    }

    public Account getOwnerAccount() throws IOException {
        return new Account(Base58.decode(retrievePrivateKey(privateOwnerKeyFile)));
    }

    public Account extractAccountFromPrivateKey(String base58PrivateKey) {
        byte[] secretKeyBytes = Base58.decode(base58PrivateKey);
        return new Account(secretKeyBytes);
    }

    public Transaction deserializeTransaction(String base64Transaction) {
        try {
            byte[] transactionBytes = Base64.getDecoder().decode(base64Transaction);

            Transaction transaction = Transaction.deserialize(transactionBytes);

            LOGGER.info("Transaction deserialized successfully.");
            return transaction;
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize transaction", e);
            throw new RuntimeException("Failed to deserialize transaction", e);
        }
    }


    private String retrievePrivateKey(String privateKeyFilePath) throws IOException {
        return FileUtil.readFromFile(privateKeyFilePath);
    }
}

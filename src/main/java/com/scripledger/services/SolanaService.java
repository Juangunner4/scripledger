package com.scripledger.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class SolanaService {
    private static final Logger LOGGER = Logger.getLogger(SolanaService.class);
    private static final String SOLANA_RPC_URL = "https://api.devnet.solana.com";
    private final RpcClient client;

    public SolanaService() {
        this.client = new RpcClient(SOLANA_RPC_URL);
    }
    public Uni<String> signTransaction(String ownerPublicKeyStr) {
        return Uni.createFrom().item(() -> {
            try {

                Transaction transaction = new Transaction();

                TransactionInstruction addOwnerSignatureInstruction = new TransactionInstruction(null, null, null);

                transaction.addInstruction(addOwnerSignatureInstruction);

                LOGGER.info("Transaction instructions added:");
                logTransactionInstruction(addOwnerSignatureInstruction);

                List<Account> signers = Arrays.asList(null);
                String result = sendTransaction(transaction, signers).toString();
                LOGGER.info(result);

                return sendTransaction(transaction, signers).toString();
            } catch (Exception e) {
                LOGGER.error("Failed to sign transaction", e);
                throw new RuntimeException("Failed to sign", e);
            }
        });
    }



    private Uni<String> sendTransaction(Transaction transaction, List<Account> signers) {
        Uni<String> uni = Uni.createFrom().item(() -> {
            try {
                String signature = client.getApi().sendTransaction(transaction, signers);
                LOGGER.info("Transaction sent with signature: " + signature);
                return signature;
            } catch (RpcException e) {
                LOGGER.error("Failed to send transaction", e);
                throw new RuntimeException("Failed to send transaction", e);
            }
        });

        uni.subscribe().with(
                signature -> LOGGER.info("Transaction successful with signature: " + signature),
                failure -> {
                    LOGGER.error("Transaction failed", failure);
                    throw new RuntimeException("Transaction failed", failure);
                }
        );

        return uni;
    }

    private void logTransactionInstruction(TransactionInstruction instruction) {
        LOGGER.info("Instruction program id: " + instruction.getProgramId().toString());
        instruction.getKeys().forEach(key -> {
            LOGGER.info("AccountMeta public key: " + key.getPublicKey().toString());
            LOGGER.info("Is signer: " + key.isSigner());
            LOGGER.info("Is writable: " + key.isWritable());
        });
    }

}

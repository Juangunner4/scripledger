package com.scripledger.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@ApplicationScoped
public class SolanaService {

    private static final Logger LOGGER = Logger.getLogger(SolanaService.class);
    private static final String SOLANA_RPC_URL = "https://api.devnet.solana.com";
    private RpcClient client;

    public SolanaService() {
        this.client = new RpcClient(SOLANA_RPC_URL);
    }

    public Account createAccount() {
        try {
            Account newAccount = new Account();
            String publicKey = newAccount.getPublicKey().toBase58();
            // Securely store the private key (example: save to a secure vault or encrypted storage)
            String privateKey = newAccount.getSecretKey().toString();
            // Example of secure storage (implementation depends on your secure storage solution)
            secureStorePrivateKey(publicKey, privateKey);
            return newAccount;
        } catch (Exception e) {
            return createAccount();
        }
    }


    private void secureStorePrivateKey(String publicKey, String privateKey) {
        // Implement secure storage logic here (e.g., save to a secure vault or encrypted database)
    }

    public PublicKey getPublicKey(String publicKeyStr) {
        try {
            return new PublicKey(publicKeyStr);
        } catch (Exception e) {
            LOGGER.error("Failed to create public key", e);
            return null;
        }
    }

    public Uni<String> sendTransaction(Transaction transaction, Account signer) {
        return Uni.createFrom().item(() -> {
            try {
                String signature = client.getApi().sendTransaction(transaction, signer);
                LOGGER.info("Transaction sent with signature: " + signature);
                return signature;
            } catch (RpcException e) {
                LOGGER.error("Failed to send transaction", e);
                return null;
            }
        });
    }

    public Uni<String> transferSol(Account sender, String recipientPublicKeyStr, long lamports) {
        try {
            PublicKey recipientPublicKey = new PublicKey(recipientPublicKeyStr);
            Transaction transaction = new Transaction();

            transaction.addInstruction(
                    SystemProgram.transfer(
                            sender.getPublicKey(),
                            recipientPublicKey,
                            lamports
                    )
            );

            return sendTransaction(transaction, sender);
        } catch (Exception e) {
            LOGGER.error("Failed to transfer SOL", e);
            return Uni.createFrom().failure(e);
        }
    }

    public Uni<String> transferToken(Account sender, String senderTokenAddressStr, String recipientTokenAddressStr, long amount, String tokenMintAddressStr) {
        try {
            PublicKey senderTokenAddress = new PublicKey(senderTokenAddressStr);
            PublicKey recipientTokenAddress = new PublicKey(recipientTokenAddressStr);
            PublicKey tokenMintAddress = new PublicKey(tokenMintAddressStr);

            Transaction transaction = new Transaction();

            byte[] data = ByteBuffer.allocate(9)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .put((byte) 3) // Transfer instruction index for the Token Program
                    .putLong(amount)
                    .array();

            AccountMeta senderAccountMeta = new AccountMeta(senderTokenAddress, true, true);
            AccountMeta recipientAccountMeta = new AccountMeta(recipientTokenAddress, false, true);
            AccountMeta mintAccountMeta = new AccountMeta(tokenMintAddress, false, false);
            AccountMeta ownerAccountMeta = new AccountMeta(sender.getPublicKey(), true, false);

            TransactionInstruction transferInstruction = new TransactionInstruction(
                    TokenProgram.PROGRAM_ID,
                    Arrays.asList(senderAccountMeta, recipientAccountMeta, mintAccountMeta, ownerAccountMeta),
                    data
            );

            transaction.addInstruction(transferInstruction);

            return sendTransaction(transaction, sender);
        } catch (Exception e) {
            LOGGER.error("Failed to transfer tokens", e);
            return Uni.createFrom().failure(e);
        }
    }

    public Uni<String> createBrandToken(String brandTokenMintAddress, String adminPublicKeyStr, long initialSupply) {
        return Uni.createFrom().item(() -> {
            try {
                Account adminAccount = new Account(); // Admin account should be securely managed
                PublicKey adminPublicKey = new PublicKey(adminPublicKeyStr);
                PublicKey mintPublicKey = new PublicKey(brandTokenMintAddress);

                Transaction transaction = new Transaction();

                byte[] data = ByteBuffer.allocate(9)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .put((byte) 7) // Mint instruction index for the Token Program
                        .putLong(initialSupply)
                        .array();

                AccountMeta mintAccountMeta = new AccountMeta(mintPublicKey, false, true);
                AccountMeta adminAccountMeta = new AccountMeta(adminPublicKey, true, true);

                TransactionInstruction mintInstruction = new TransactionInstruction(
                        TokenProgram.PROGRAM_ID,
                        Arrays.asList(mintAccountMeta, adminAccountMeta),
                        data
                );

                transaction.addInstruction(mintInstruction);

                return sendTransaction(transaction, adminAccount).await().indefinitely();
            } catch (Exception e) {
                LOGGER.error("Failed to create brand tokens", e);
                throw new RuntimeException(e);
            }
        });
    }

    public Uni<String> distributeBrandTokens(Account adminAccount, String brandTokenMintAddress, String userPublicKeyStr, long amount) {
        try {
            PublicKey userPublicKey = new PublicKey(userPublicKeyStr);
            PublicKey tokenMintAddress = new PublicKey(brandTokenMintAddress);

            return transferToken(adminAccount, adminAccount.getPublicKey().toString(), userPublicKeyStr, amount, brandTokenMintAddress);
        } catch (Exception e) {
            LOGGER.error("Failed to distribute brand tokens", e);
            return Uni.createFrom().failure(e);
        }
    }

    public static class TokenProgram {
        public static final PublicKey PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    }
}

package com.scripledger.services;

import com.scripledger.util.FileUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bitcoinj.core.Base58;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@ApplicationScoped
public class SolanaService {

    @Inject
    @ConfigProperty(name = "solana.privateKeyFile")
    String privateKeyFilePath;

    private static final Logger LOGGER = Logger.getLogger(SolanaService.class);
    private static final String SOLANA_RPC_URL = "https://api.devnet.solana.com";
    private final RpcClient client;

    public SolanaService() {
        this.client = new RpcClient(SOLANA_RPC_URL);
    }

    public Account createAccount() {
        try {
            Account newAccount = new Account();
            String publicKey = newAccount.getPublicKey().toBase58();
            String privateKey = Base58.encode(newAccount.getSecretKey());
            FileUtil.createFileIfNotExists(privateKeyFilePath);
            FileUtil.writeToFile(privateKeyFilePath, privateKey);
            secureStorePrivateKey(publicKey, privateKey);
            return newAccount;
        } catch (Exception e) {
            LOGGER.error("Failed to create account", e);
            throw new RuntimeException("Failed to create account", e);
        }
    }

    public void executeCreateBrandToken(String ownerPublicKeyStr, Long initialSupply) {
        createBrandToken(ownerPublicKeyStr, initialSupply)
                .subscribe().with(
                        signature -> {
                            System.out.println("Transaction completed with signature: " + signature);
                        },
                        failure -> {
                            System.err.println("Transaction failed with error: " + failure.getMessage());
                        }
                );
    }

    private void secureStorePrivateKey(String publicKey, String privateKey) {
        // Implement secure storage logic here (e.g., save to a secure vault or encrypted database)
    }

    private String retrievePrivateKey() throws IOException {
        return FileUtil.readFromFile(privateKeyFilePath);
    }

    public PublicKey getPublicKey(String publicKeyStr) {
        try {
            return new PublicKey(publicKeyStr);
        } catch (Exception e) {
            LOGGER.error("Failed to create public key", e);
            throw new RuntimeException("Invalid public key: " + publicKeyStr, e);
        }
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
            return Uni.createFrom().failure(new RuntimeException("Failed to transfer SOL", e));
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
            return Uni.createFrom().failure(new RuntimeException("Failed to transfer tokens", e));
        }
    }

    public Uni<Uni<String>> createBrandToken(String ownerPublicKeyStr, Long initialSupply) {
        return Uni.createFrom().item(() -> {
            try {
                PublicKey ownerPublicKey = new PublicKey(ownerPublicKeyStr);

                // Log owner account public key
                System.out.println("Owner account public key: " + ownerPublicKey);

                Account mintAccount = new Account();
                PublicKey mintPublicKey = mintAccount.getPublicKey();

                // Log mint account public key
                System.out.println("Mint account public key: " + mintPublicKey.toString());

                Transaction transaction = new Transaction();

                long lamportsForRentExemption = getMinimumBalanceForRentExemption();

                TransactionInstruction createAccountInstruction = SystemProgram.createAccount(
                        ownerPublicKey,
                        mintPublicKey,
                        lamportsForRentExemption,
                        TokenProgram.MINT_LAYOUT_SIZE,
                        TokenProgram.PROGRAM_ID
                );

                byte[] data = ByteBuffer.allocate(9)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .put((byte) 0) // InitializeMint instruction index for the Token Program
                        .putLong(initialSupply)
                        .array();

                AccountMeta mintAccountMeta = new AccountMeta(mintPublicKey, false, true);
                AccountMeta ownerAccountMeta = new AccountMeta(ownerPublicKey, true, true);

                TransactionInstruction mintInstruction = new TransactionInstruction(
                        TokenProgram.PROGRAM_ID,
                        Arrays.asList(mintAccountMeta, ownerAccountMeta),
                        data
                );

                transaction.addInstruction(createAccountInstruction);
                transaction.addInstruction(mintInstruction);

                // Log details of the transaction instructions
                System.out.println("Transaction instructions added:");
                logTransactionInstruction(createAccountInstruction);
                logTransactionInstruction(mintInstruction);

                return sendTransaction(transaction, mintAccount)
                        .onItem().transform(signature -> {
                            LOGGER.info("Transaction successful with signature: " + signature);
                            return signature;
                        })
                        .onFailure().invoke(e -> {
                            LOGGER.error("Transaction failed", e);
                        });
            } catch (Exception e) {
                LOGGER.error("Failed to create brand tokens", e);
                throw new RuntimeException("Failed to create brand tokens", e);
            }
        });
    }

    public Uni<String> sendTransaction(Transaction transaction, Account signer) {
        Uni<String> uni = Uni.createFrom().item(() -> {
            try {
                String signature = client.getApi().sendTransaction(transaction, signer);
                LOGGER.info("Transaction sent with signature: " + signature);
                return signature;
            } catch (RpcException e) {
                LOGGER.error("Failed to send transaction", e);
                throw new RuntimeException("Failed to send transaction", e);
            }
        });

        // Force subscription and handle errors
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
        System.out.println("Instruction program id: " + instruction.getProgramId().toString());
        instruction.getKeys().forEach(key -> {
            System.out.println("AccountMeta public key: " + key.getPublicKey().toString());
            System.out.println("Is signer: " + key.isSigner());
            System.out.println("Is writable: " + key.isWritable());
        });
    }

    private long getMinimumBalanceForRentExemption() {
        // Implement this method to return the minimum balance required for rent exemption.
        return 2039280; // Example value, replace with actual calculation
    }


    public Uni<String> distributeBrandTokens(Account adminAccount, String brandTokenMintAddress, String userPublicKeyStr, long amount) {
        try {
            PublicKey userPublicKey = new PublicKey(userPublicKeyStr);
            PublicKey tokenMintAddress = new PublicKey(brandTokenMintAddress);

            return transferToken(adminAccount, adminAccount.getPublicKey().toString(), userPublicKeyStr, amount, brandTokenMintAddress);
        } catch (Exception e) {
            LOGGER.error("Failed to distribute brand tokens", e);
            return Uni.createFrom().failure(new RuntimeException("Failed to distribute brand tokens", e));
        }
    }

    public static class TokenProgram {
        public static final int MINT_LAYOUT_SIZE = 82;
        public static final PublicKey PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    }
}

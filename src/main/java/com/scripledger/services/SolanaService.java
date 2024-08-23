package com.scripledger.services;

import com.scripledger.util.FileUtil;
import com.scripledger.util.TokenProgramUtil;
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
import java.util.List;

@ApplicationScoped
public class SolanaService {
    @Inject
    @ConfigProperty(name = "solana.privateOwnerKeyFile")
    String privateOwnerKeyFile;

    @Inject
    @ConfigProperty(name = "solana.privateTokenKeyFile")
    String privateTokenKeyFile;

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
            FileUtil.createFileIfNotExists(privateOwnerKeyFile);
            FileUtil.writeToFile(privateOwnerKeyFile, privateKey);
            secureStorePrivateKey(publicKey, privateKey);
            return newAccount;
        } catch (Exception e) {
            LOGGER.error("Failed to create account", e);
            throw new RuntimeException("Failed to create account", e);
        }
    }

    public void executeCreateBrandToken(String ownerPublicKeyStr) {
        createBrandTokenAccount(ownerPublicKeyStr)
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

    private String retrievePrivateKey(String privateKeyFilePath) throws IOException {
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

            return sendTransaction(transaction, (List<Account>) sender);
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
                    TokenProgramUtil.PROGRAM_ID,
                    Arrays.asList(senderAccountMeta, recipientAccountMeta, mintAccountMeta, ownerAccountMeta),
                    data
            );

            transaction.addInstruction(transferInstruction);

            return sendTransaction(transaction, (List<Account>) sender);
        } catch (Exception e) {
            LOGGER.error("Failed to transfer tokens", e);
            return Uni.createFrom().failure(new RuntimeException("Failed to transfer tokens", e));
        }
    }

    public Uni<String> createBrandTokenAccount(String ownerPublicKeyStr) {
        return Uni.createFrom().item(() -> {
            try {

                Account owner = new Account(Base58.decode(retrievePrivateKey(privateOwnerKeyFile))); // Load fee payer account
                PublicKey ownerPublicKey = new PublicKey(ownerPublicKeyStr);

                // Log owner account public key
                LOGGER.info("Owner account public key: " + ownerPublicKey);

                Account tokenAccount = new Account();
                String publicKey = tokenAccount.getPublicKey().toBase58();
                String privateKey = Base58.encode(tokenAccount.getSecretKey());
                FileUtil.createFileIfNotExists(privateTokenKeyFile);
                FileUtil.writeToFile(privateTokenKeyFile, privateKey);
                secureStorePrivateKey(publicKey, privateKey);
                PublicKey tokenPublicKey = tokenAccount.getPublicKey();

                // Log token-2022 account public key
                LOGGER.info("Token 2022 account public key: " + tokenPublicKey.toString());
                Transaction transaction = new Transaction();


                TransactionInstruction createAccountInstruction = SystemProgram.createAccount(
                        ownerPublicKey,
                        tokenPublicKey,
                        TokenProgramUtil.LAMPORTSFORRENTEXEMPTION(),
                        TokenProgramUtil.MINT_LAYOUT_SIZE,
                        TokenProgramUtil.PROGRAM_ID
                );

                transaction.addInstruction(createAccountInstruction);

                // Log details of the transaction instructions
                LOGGER.info("Transaction instructions added:");
                logTransactionInstruction(createAccountInstruction);

                List<Account> signers = Arrays.asList(owner, tokenAccount);
                sendTransaction(transaction, signers).toString();
                return tokenPublicKey.toString();
            } catch (Exception e) {
                LOGGER.error("Failed to create brand tokens", e);
                throw new RuntimeException("Failed to create brand tokens", e);
            }
        });
    }


    public Uni<String> mintTokens(String recipientTokenPublicKeyStr, Long amount) {
        return Uni.createFrom().item(() -> {
            try {
                Account owner = new Account(Base58.decode(retrievePrivateKey(privateOwnerKeyFile)));// Load fee payer account
                PublicKey ownerPublicKey = owner.getPublicKey();

                Account mintAccount = new Account();
                PublicKey mintPublicKey = mintAccount.getPublicKey();
                LOGGER.info("Mint account public key: " + mintAccount.getPublicKey());

                PublicKey recipientTokenPublicKey = new PublicKey(recipientTokenPublicKeyStr);
                LOGGER.info("Recipient account public key: " + recipientTokenPublicKeyStr);

                var result = client.getApi().getAccountInfo(ownerPublicKey);
                LOGGER.info("Account Info: " + result.getValue());

                Transaction transaction = new Transaction();

                TransactionInstruction initializeMintInstruction = TokenProgramUtil.mintTo(
                        mintPublicKey,
                        recipientTokenPublicKey,
                        ownerPublicKey,
                        amount);

                transaction.addInstruction(initializeMintInstruction);

                // Log details of the transaction instructions
                LOGGER.info("Transaction instructions added:");
                logTransactionInstruction(initializeMintInstruction);

                List<Account> signers = List.of(owner);

                return sendTransaction(transaction, signers).toString();
            } catch (Exception e) {
                LOGGER.error("Failed to mint tokens", e);
                throw new RuntimeException("Failed to mint tokens", e);
            }
        });
    }

    public Uni<String> sendTransaction(Transaction transaction, List<Account> signers) {
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
        LOGGER.info("Instruction program id: " + instruction.getProgramId().toString());
        instruction.getKeys().forEach(key -> {
            LOGGER.info("AccountMeta public key: " + key.getPublicKey().toString());
            LOGGER.info("Is signer: " + key.isSigner());
            LOGGER.info("Is writable: " + key.isWritable());
        });
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

}

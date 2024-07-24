package com.scripledger.services;

import com.scripledger.models.AdminActionRequest;
import com.scripledger.models.UserAccount;
import com.scripledger.repositories.UserAccountRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;

import java.util.Objects;

@ApplicationScoped
public class UserAccountService {

    @Inject
    UserAccountRepository userAccountRepository;

    @Inject
    SolanaService solanaService;

    private static final Logger LOGGER = Logger.getLogger(UserAccountService.class);

    public Uni<UserAccount> updateAccount(ObjectId accountId) {
        return userAccountRepository.findById(accountId)
                .flatMap(userAccount -> {
                    if (userAccount == null) {
                        return Uni.createFrom().nullItem();
                    }
                    return userAccountRepository.update(userAccount)
                            .map(updatedAccount -> {
                                LOGGER.info("Updated account with ID: " + updatedAccount.getPublicKey());
                                return updatedAccount;
                            });
                });
    }

    public Uni<UserAccount> createAccount(String username) {
        LOGGER.info("Service: Creating account for username: " + username);

        return checkIfUserExists(username)
                .flatMap(exists -> {
                    if (exists) {
                        return Uni.createFrom().failure(new RuntimeException("User with username " + username + " already exists"));
                    } else {
                        return createNewAccount(username);
                    }
                }).onFailure().invoke(ex -> LOGGER.error("Failed to create account", ex));
    }

    public Uni<UserAccount> getAccountById(String accountId) {
        LOGGER.info("Retrieving account with accountId: " + accountId);
        ObjectId objectId = new ObjectId(accountId);
        return userAccountRepository.findById(objectId);
    }

    public Uni<Response> adminAction(ObjectId accountId, AdminActionRequest request) {
        return userAccountRepository.findById(accountId)
                .flatMap(userAccount -> {
                    if (userAccount == null) {
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).entity("Account not found").build());
                    }

                    Uni<String> resultUni;

                    // Perform the admin action based on the action type
                    if ("freeze".equalsIgnoreCase(request.getActionType())) {
                        LOGGER.info("Freezing account with ID: " + accountId);
                        userAccount.setActionType("freeze");
                        userAccount.setCategory(request.getCategory());
                        userAccount.setNote(request.getNote());
                        resultUni = Uni.createFrom().item("Account frozen successfully");
                    } else if ("revoke".equalsIgnoreCase(request.getActionType())) {
                        LOGGER.info("Revoking account with ID: " + accountId);
                        userAccount.setActionType("revoke");
                        userAccount.setCategory(request.getCategory());
                        userAccount.setNote(request.getNote());
                        // Example: use solanaService to transfer tokens if needed
                        resultUni = solanaService.transferToken(
                                new Account(request.getAccountPublicKey().getBytes()),  // Assuming the request has the private key for the admin account
                                request.getAccountPublicKey(),
                                request.getTokenId(),
                                (long) request.getBalance().getAmount(),
                                "brandTokenMintAddress"  // Replace with actual token mint address
                        );
                    } else {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Invalid action type").build());
                    }

                    return resultUni.flatMap(result -> userAccountRepository.update(userAccount)
                            .map(updatedAccount -> Response.ok(updatedAccount).build()));
                });
    }

    private Uni<Boolean> checkIfUserExists(String username) {
      return userAccountRepository.find("username", username).firstResult().map(Objects::nonNull);
    }

    private Uni<UserAccount> createNewAccount(String username) {
        Account solanaAccount = solanaService.createAccount();
        String publicKey = solanaAccount.getPublicKey().toString();

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(username);
        userAccount.setPublicKey(publicKey);
        userAccount.setKycStatus("pending");

        return userAccountRepository.persist(userAccount);
    }
}

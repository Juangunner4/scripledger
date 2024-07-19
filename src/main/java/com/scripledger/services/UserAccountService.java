package com.scripledger.services;

import com.scripledger.models.AdminActionRequest;
import com.scripledger.models.Balance;
import com.scripledger.models.UserAccount;
import com.scripledger.repositories.UserAccountRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class UserAccountService {

    @Inject
    UserAccountRepository userAccountRepository;

    @Inject
    SolanaService solanaService;

    private static final Logger LOGGER = Logger.getLogger(UserAccountService.class);

    public Uni<UserAccount> updateAccount(ObjectId accountId, String accountPublicKey, String alternateAccountId, String customerProfile) {
        return userAccountRepository.findById(accountId)
                .flatMap(userAccount -> {
                    if (userAccount == null) {
                        return Uni.createFrom().nullItem();
                    }
                    userAccount.setId(accountId);
                    ObjectId objectId = new ObjectId(alternateAccountId);
                    userAccount.setPublicKey(accountPublicKey);
                    userAccount.setAlternateAccountId(objectId);
                    userAccount.setCustomerProfile(customerProfile);
                    return userAccountRepository.update(userAccount)
                            .map(updatedAccount -> {
                                LOGGER.info("Updated account with ID: " + updatedAccount.getPublicKey());
                                return updatedAccount;
                            });
                });
    }

    public Uni<UserAccount> createAccount(String username) {
        LOGGER.info("Creating account for username: " + username);

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

                    // Perform the admin action based on the action type
                    if ("freeze".equalsIgnoreCase(request.getActionType())) {
                        // Implement freeze logic (e.g., marking the account as frozen in the database)
                        LOGGER.info("Freezing account with ID: " + accountId);
                        // Example: set a status field or similar
                    } else if ("revoke".equalsIgnoreCase(request.getActionType())) {
                        // Implement revoke logic (e.g., transferring tokens back to an admin account)
                        LOGGER.info("Revoking account with ID: " + accountId);
                        // Example: use solanaService to transfer tokens
                    } else {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Invalid action type").build());
                    }

                    // Update user account and return success response
                    return userAccountRepository.update(userAccount)
                            .map(updatedAccount -> Response.ok(updatedAccount).build());
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
        userAccount.setFirstTxnTimestamp(new Date());
        Balance userBalance = new Balance();
        List<Balance> balances = new ArrayList<>();
        userBalance.setTokenName("Target");
        userBalance.setAmount(1.0);
        balances.add(userBalance);
        userAccount.setBalances(balances);

        return userAccountRepository.persist(userAccount);
    }
}

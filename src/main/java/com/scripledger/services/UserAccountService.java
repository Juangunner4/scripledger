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

    public Uni<UserAccount> createAccount(UserAccount userAccount) {
        LOGGER.info("Service: Creating account for username: " + userAccount.getUsername());
        return isUserRegistered(userAccount, createNewAccount(userAccount), "Failed to create account");
    }

    public Uni<UserAccount> registerUser(UserAccount userAccount) {
        LOGGER.info("Service: Registering account for username: " + userAccount.getUsername());
        return isUserRegistered(userAccount, registerNewAccount(userAccount), "Failed to register account");
    }

    public Uni<UserAccount> getAccountById(String accountId) {
        LOGGER.info("Retrieving account with accountId: " + accountId);
        ObjectId objectId = new ObjectId(accountId);
        return userAccountRepository.findById(objectId);
    }

    public Uni<UserAccount> getAccountByUsername(String username) {
        LOGGER.info("Retrieving account with username: " + username);
        return userAccountRepository.find("username", username).firstResult();
    }

    public Uni<UserAccount> getAccountByPublicKey(String publicKey) {
        LOGGER.info("Retrieving account with publicKey: " + publicKey);
        return userAccountRepository.find("publicKey", publicKey).firstResult();
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
                                "brandTokenMintAddress"  // Replace with actual token mint address
                        );
                    } else {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Invalid action type").build());
                    }

                    return resultUni.flatMap(result -> userAccountRepository.update(userAccount)
                            .map(updatedAccount -> Response.ok(updatedAccount).build()));
                });
    }
    private Uni<UserAccount> isUserRegistered(UserAccount userAccount, Uni<UserAccount> userAccountUni, String Failed_to_create_account) {
        return checkIfUserExists(userAccount.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Uni.createFrom().failure(new RuntimeException("User with username " + userAccount.getUsername() + " already exists"));
                    } else {
                        return userAccountUni;
                    }
                }).onFailure().invoke(ex -> LOGGER.error(Failed_to_create_account, ex));
    }
    private Uni<Boolean> checkIfUserExists(String username) {
      return userAccountRepository.find("username", username).firstResult().map(Objects::nonNull);
    }

    private Uni<UserAccount> createNewAccount(UserAccount userAccount) {
        Account solanaAccount = solanaService.createAccount();
        String publicKey = solanaAccount.getPublicKey().toString();

        UserAccount account = new UserAccount();
        account.setUsername(userAccount.getUsername());
        account.setPublicKey(publicKey);

        return userAccountRepository.persist(account);
    }

    private Uni<UserAccount> registerNewAccount(UserAccount userAccount) {

        UserAccount account = new UserAccount();
        account.setUsername(userAccount.getUsername());
        account.setPublicKey(userAccount.getPublicKey());
        account.setEmail(userAccount.getEmail());
        account.setUsername(userAccount.getUsername());

        return userAccountRepository.persist(account);
    }
}

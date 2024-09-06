package com.scripledger.services;

import com.scripledger.models.UserAccount;
import com.scripledger.repositories.UserAccountRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.Objects;

@ApplicationScoped
public class UserAccountService {

    @Inject
    UserAccountRepository userAccountRepository;

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

    private Uni<UserAccount> registerNewAccount(UserAccount userAccount) {

        UserAccount account = new UserAccount();
        account.setUsername(userAccount.getUsername());
        account.setPublicKey(userAccount.getPublicKey());
        account.setEmail(userAccount.getEmail());
        account.setUsername(userAccount.getUsername());

        return userAccountRepository.persist(account);
    }
}

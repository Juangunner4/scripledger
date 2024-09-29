package com.scripledger.services;

import com.scripledger.collections.UserAccount;
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

    public Uni<UserAccount> updateAccount(UserAccount userAccount) {
        return userAccountRepository.findById(userAccount.getId())
                .flatMap(account -> {
                    if (account == null) {
                        return Uni.createFrom().nullItem();
                    }
                    return userAccountRepository.update(userAccount)
                            .map(updatedAccount -> {
                                LOGGER.debug("Updated account with ID: " + updatedAccount.getId());
                                return updatedAccount;
                            });
                });
    }


    public Uni<UserAccount> registerUser(UserAccount userAccount) {
        return isUserRegistered(userAccount, registerNewAccount(userAccount), "Failed to register account");
    }

    public Uni<UserAccount> getAccountById(String accountId) {
        ObjectId objectId = new ObjectId(accountId);
        return userAccountRepository.findById(objectId);
    }

    public Uni<UserAccount> getAccountByUsername(String username) {
        return userAccountRepository.find("username", username).firstResult();
    }

    public Uni<UserAccount> getAccountByPublicKey(String publicKey) {
        return userAccountRepository.find("accountPublicKey", publicKey).firstResult();
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
        return userAccountRepository.persist(userAccount);
    }
}

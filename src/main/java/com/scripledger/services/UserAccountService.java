package com.scripledger.services;

import com.scripledger.models.Balance;
import com.scripledger.models.UserAccount;
import com.scripledger.repositories.UserAccountRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class UserAccountService {

    @Inject
    UserAccountRepository userAccountRepository;

    @Inject
    SolanaService solanaService;

    private static final Logger LOGGER = Logger.getLogger(UserAccountService.class);

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

    public Uni<UserAccount> getAccountByPublicKey(String publicKey) {
        LOGGER.info("Retrieving account with publicKey: " + publicKey);
        return userAccountRepository.findByPublicKey(publicKey);
    }

    private Uni<Boolean> checkIfUserExists(String username) {
      return userAccountRepository.find("username", username).firstResult().map(userAccount -> userAccount != null);
    }

    private Uni<UserAccount> createNewAccount(String username) {
        Account solanaAccount = solanaService.createAccount();
        String publicKey = solanaAccount.getPublicKey().toString();

        UserAccount userAccount = new UserAccount();
        userAccount.setAccountId(new ObjectId());
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

        return userAccountRepository.persist(userAccount).replaceWith(userAccount);
    }
}

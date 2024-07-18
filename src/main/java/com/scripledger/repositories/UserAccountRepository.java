package com.scripledger.repositories;

import com.scripledger.models.UserAccount;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserAccountRepository implements ReactivePanacheMongoRepository<UserAccount> {

}
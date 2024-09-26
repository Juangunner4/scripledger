package com.scripledger.services;

import com.scripledger.collections.Token;
import com.scripledger.models.MintTokenRequest;
import com.scripledger.repositories.TokenRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TokenService {

    private static final Logger LOGGER = Logger.getLogger(TokenService.class);

    @Inject
    TokenRepository tokenRepository;


    public Uni<Token> createToken(String ownerPublicKey, String mintPublicKey, MintTokenRequest mintTokenRequest) {
        LOGGER.info("Creating new token for ownerPublicKey: " + ownerPublicKey);
        Token token = new Token();
        token.setId(new ObjectId());
        token.setOwnerPublicKey(ownerPublicKey);
        token.setMintPublicKey(mintPublicKey);
        token.setTokenName(mintTokenRequest.getTokenName());
        token.setUnit(mintTokenRequest.getUnit());
        token.setLogoLink(mintTokenRequest.getLogoLink());
        return tokenRepository.persist(token)
                .onItem().invoke(t -> LOGGER.info("Token created with ID: " + t.getId()));
    }

    public Uni<Token> getTokenByMintPublicKey(String mintPubKey) {
        LOGGER.info("Service: Retrieving Brand with ownerPublicKey: " + mintPubKey);
        return tokenRepository.find("mintPublicKey", mintPubKey)
                .firstResult()
                .onItem().invoke(token -> {
                    if (token != null) {
                        LOGGER.info("Token found: " + token.getMintPublicKey());
                    } else {
                        LOGGER.warn("No Token found with mintPublicKey: " + mintPubKey);
                    }
                });
    }

    public Uni<Token> getTokenByName(String tokenName) {
        LOGGER.info("Service: Retrieving Brand with tokenName: " + tokenName);
        return tokenRepository.find("tokenName", tokenName)
                .firstResult()
                .onItem().invoke(token -> {
                    if (token != null) {
                        LOGGER.info("Token found: " + token.getMintPublicKey());
                    } else {
                        LOGGER.warn("No Token found with tokenName: " + tokenName);
                    }
                });
    }


}

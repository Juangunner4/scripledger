package com.scripledger.resources;

import com.scripledger.collections.Token;
import com.scripledger.services.TokenService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/token")
public class TokenResource {

    @Inject
    TokenService tokenService;

    private static final Logger LOGGER = Logger.getLogger(TokenResource.class);

    @GET
    @Path("/tokenName/{tokenName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Token> getTokenByName(@PathParam("tokenName") String tokenName) {
        LOGGER.info("Fetching token with tokenName: " + tokenName);
        return tokenService.getTokenByName(tokenName)
                .onItem().invoke(token -> LOGGER.info("Token fetched: " + token))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/mintPubKey/{mintPubKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Token> getTokenByMintPubKey(@PathParam("mintPubKey") String mintPubKey) {
        LOGGER.info("Fetching token with mintPubKey: " + mintPubKey);
        return tokenService.getTokenByMintPublicKey(mintPubKey)
                .onItem().invoke(token -> LOGGER.info("Token fetched: " + token))
                .onFailure().invoke(Throwable::printStackTrace);
    }
}

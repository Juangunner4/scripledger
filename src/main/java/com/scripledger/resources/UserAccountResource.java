package com.scripledger.resources;

import com.scripledger.services.UserAccountService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/accounts")
public class UserAccountResource {

    @Inject
    UserAccountService userAccountService;

    private static final Logger LOGGER = Logger.getLogger(UserAccountResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createAccount(@QueryParam("username") String username) {
        LOGGER.info("Creating account for username: " + username);
        return userAccountService.createAccount(username)
                .onItem().transform(account -> Response.ok(account).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to create account", throwable);
                    if (throwable.getMessage().contains("already exists")) {
                        return Response.status(Response.Status.CONFLICT).entity(throwable.getMessage()).build();
                    }
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }

    @GET
    @Path("/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAccountByPublicKey(@PathParam("publicKey") String publicKey) {
        LOGGER.info("Fetching account with publicKey: " + publicKey);
        return userAccountService.getAccountByPublicKey(publicKey)
                .onItem().transform(account -> account != null ? Response.ok(account).build() : Response.status(Response.Status.NOT_FOUND).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to fetch account", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }
}

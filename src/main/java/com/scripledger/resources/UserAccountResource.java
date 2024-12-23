package com.scripledger.resources;

import com.scripledger.collections.UserAccount;
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
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> registerUser(UserAccount userAccount) {
        LOGGER.info("Registering account for username: " + userAccount.getUsername());
        return userAccountService.registerUser(userAccount)
                .onItem().transform(account -> Response.ok(account).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to register user", throwable);
                    if (throwable.getMessage().contains("already exists")) {
                        return Response.status(Response.Status.CONFLICT).entity(throwable.getMessage()).build();
                    }
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }
    @GET
    @Path("/id/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAccountById(@PathParam("accountId") String accountId) {
        LOGGER.info("Fetching account with accountId: " + accountId);
        return userAccountService.getAccountById(accountId)
                .onItem().transform(account -> account != null ? Response.ok(account).build() : Response.status(Response.Status.NOT_FOUND).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to fetch account", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }

    @GET
    @Path("/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAccountByUsername(@PathParam("username") String username) {
        LOGGER.info("Fetching account with username: " + username);
        return userAccountService.getAccountByUsername(username)
                .onItem().transform(account -> account != null ? Response.ok(account).build() : Response.status(Response.Status.NOT_FOUND).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to fetch account", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }

    @GET
    @Path("/publicKey/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAccountByPubicKey(@PathParam("publicKey") String publicKey) {
        LOGGER.info("Fetching account with publicKey: " + publicKey);
        return userAccountService.getAccountByPublicKey(publicKey)
                .onItem().transform(account -> account != null ? Response.ok(account).build() : Response.status(Response.Status.NOT_FOUND).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to fetch account", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateAccount(UserAccount userAccount) {
        LOGGER.info("Updating account with accountId: " + userAccount.getId());
        return userAccountService.updateAccount(userAccount)
                .map(updatedAccount -> {
                    if (updatedAccount != null) {
                        return Response.ok(updatedAccount).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("Account not found").build();
                    }
                });
    }
}

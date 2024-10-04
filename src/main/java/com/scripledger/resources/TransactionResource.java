package com.scripledger.resources;

import com.scripledger.collections.Transaction;
import com.scripledger.models.TransactionRequest;
import com.scripledger.services.TransactionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/transaction")
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class);


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Transaction> signTransaction(TransactionRequest transactionRequest) {
        LOGGER.info("Received transaction: " + transactionRequest.getTransaction());
        return transactionService.signTransaction(transactionRequest)
                .onItem().invoke(trans -> LOGGER.info("Transaction created: " + trans.getTransactionHash()))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Transaction> getTransaction(@PathParam("transactionId") String transactionId) {
        LOGGER.info("Fetching transaction with ID: " + transactionId);
        return transactionService.getTransaction(transactionId)
                .onItem().invoke(trans -> LOGGER.info("Transaction fetched: " + trans.getTransactionHash()))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/user/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Transaction>> getAllTransactionsForUser(@PathParam("publicKey") String publicKey) {
        LOGGER.info("Fetching transactions for publicKey: " + publicKey);
        return transactionService.getAllTransactionsForUser(publicKey)
                .onItem().invoke(() -> {})
                .onFailure().invoke(Throwable::printStackTrace);
    }
}

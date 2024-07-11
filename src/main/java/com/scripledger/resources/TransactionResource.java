package com.scripledger.resources;

import com.scripledger.models.Transaction;
import com.scripledger.services.TransactionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/transactions")
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class);


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Transaction> createTransaction(Transaction transaction) {
        LOGGER.info("Received transaction: " + transaction);
        return transactionService.createTransaction(transaction)
                .onItem().invoke(trans -> LOGGER.info("Transaction created: " + trans))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Transaction> getTransaction(@PathParam("transactionId") String transactionId) {
        LOGGER.info("Fetching transaction with ID: " + transactionId);
        return transactionService.getTransaction(transactionId)
                .onItem().invoke(trans -> LOGGER.info("Transaction fetched: " + trans))
                .onFailure().invoke(Throwable::printStackTrace);
    }
}

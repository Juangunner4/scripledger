package com.scripledger.resources;

import com.scripledger.models.Transaction;
import com.scripledger.services.TransactionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/transactions")

public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Transaction> createTransaction(Transaction transaction) {
        return transactionService.createTransaction(transaction);
    }

    @GET
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Transaction> getTransaction(@PathParam("transactionId") String transactionId) {
        return transactionService.getTransaction(transactionId);
    }
}

package com.scripledger.config;

import com.scripledger.models.*;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
@RegisterRestClient(configKey = "node-api")
public interface NodeClient {


    @POST
    @Path("/issue_business_currency")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<IssueCurrencyResponse> issueBusinessCurrency(IssueCurrencyRequest request);

    @POST
    @Path("/transaction_from_business_account")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TransactionResponse> transactionFromBusinessAccount(TransactionRequest request);

    @POST
    @Path("/admin_actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<AdminActionResponse> adminActions(AdminActionRequest request);
}
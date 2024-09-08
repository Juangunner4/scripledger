package com.scripledger.resources;

import com.scripledger.config.NodeClient;
import com.scripledger.models.AdminActionRequest;
import com.scripledger.models.IssueCurrencyRequest;
import com.scripledger.models.TransactionRequest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/token")
public class NodeClientResource {
    @Inject
    @RestClient
    NodeClient nodeClient;

    // Issue Business Currency Endpoint
    @POST
    @Path("/issue_business_currency")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> issueBusinessCurrency(IssueCurrencyRequest request) {
        return nodeClient.issueBusinessCurrency(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }

    // Transaction from Business Account Endpoint
    @POST
    @Path("/transaction_from_business_account")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> transactionFromBusinessAccount(TransactionRequest request) {
        return nodeClient.transactionFromBusinessAccount(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }

    // Admin Actions Endpoint
    @POST
    @Path("/admin_actions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> adminActions(AdminActionRequest request) {
        return nodeClient.adminActions(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }
}

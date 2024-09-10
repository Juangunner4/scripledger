package com.scripledger.resources;

import com.scripledger.models.AdminActionRequest;
import com.scripledger.models.IssueCurrencyRequest;
import com.scripledger.models.TransactionRequest;
import com.scripledger.services.NodeService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/token")
public class NodeClientResource {
    @Inject
    NodeService nodeService;


    @POST
    @Path("/issue_business_currency")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> issueBusinessCurrency(IssueCurrencyRequest request) {
        return nodeService.issueBusinessCurrency(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }

    @POST
    @Path("/transaction_from_business_account")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> transactionFromBusinessAccount(TransactionRequest request) {
        return nodeService.transactionFromBusinessAccount(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }

    // Admin Actions Endpoint
    @POST
    @Path("/admin_actions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> adminActions(AdminActionRequest request) {
        return nodeService.adminActions(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }
}

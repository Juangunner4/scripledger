package com.scripledger.resources;

import com.scripledger.models.AdminActionRequest;
import com.scripledger.models.MintTokenRequest;
import com.scripledger.models.TransferRequest;
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
    @Path("/mint")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> mintTokensForBusiness(MintTokenRequest request) {
        return nodeService.mintBusinessTokens(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> transferTokenFromBusiness(TransferRequest request) {
        return nodeService.transferFromBusiness(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }

    // Admin Actions Endpoint
    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> adminActions(AdminActionRequest request) {
        return nodeService.adminActions(request)
                .onItem().transform(response -> Response.ok(response).build())
                .onFailure().recoverWithItem(th -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(th.getMessage()).build());
    }
}

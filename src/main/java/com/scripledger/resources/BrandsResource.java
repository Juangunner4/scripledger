package com.scripledger.resources;

import com.scripledger.models.Brand;
import com.scripledger.models.MintTokensRequest;
import com.scripledger.models.Token;
import com.scripledger.services.BrandsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/brands")
public class BrandsResource {

    @Inject
    BrandsService brandsService;

    private static final Logger LOGGER = Logger.getLogger(BrandsResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Brand> createBrand(Brand brand) {
        LOGGER.info("Creating brand: " + brand.getBrandName());
        return brandsService.createBrand(brand)
                .onItem().invoke(createdBrand -> LOGGER.info("Brand created: " + createdBrand.getBrandName()))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/{brandId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Brand> getTransaction(@PathParam("brandId") String brandId) {
        LOGGER.info("Fetching brand with ID: " + brandId);
        return brandsService.getBrand(brandId)
                .onItem().invoke(brand -> LOGGER.info("Brand fetched: " + brand))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/{brandId}/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Token>> getTokens(@PathParam("brandId") String brandId) {
        LOGGER.info("Fetching brand tokens with ID: " + brandId);
        return brandsService.getTokenList(brandId)
                .onItem().invoke(tokenList -> LOGGER.info("Brand tokens fetched: " + tokenList))
                .onFailure().invoke(Throwable::printStackTrace);
    }


    @POST
    @Path("/mintTokens")
    public Uni<Response> mintTokens(MintTokensRequest request) {
        return brandsService.mintTokens(request)
                .map(txSignature -> Response.ok(txSignature).build())
                .onFailure().recoverWithItem(err -> Response.status(Response.Status.BAD_REQUEST).entity(err.getMessage()).build());
    }
}

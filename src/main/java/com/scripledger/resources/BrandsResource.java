package com.scripledger.resources;

import com.scripledger.collections.Brand;
import com.scripledger.collections.Token;
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
    public Uni<Response> registerBrand(Brand brand) {
        LOGGER.info("Creating a new brand: " + brand.getBrandName());
        return brandsService.createBrand(brand)
                .onItem().transform(registeredBrand ->  Response.ok(registeredBrand).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to register brand", throwable);
                    if (throwable.getMessage().contains("already exists")) {
                        return Response.status(Response.Status.CONFLICT).entity(throwable.getMessage()).build();
                    }
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }

    @GET
    @Path("/{brandId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Brand> getBrandById(@PathParam("brandId") String brandId) {
        LOGGER.info("Fetching brand with ID: " + brandId);
        return brandsService.getBrandById(brandId)
                .onItem().invoke(brand -> LOGGER.info("Brand fetched: " + brand))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Brand> getBrandByPublicKey(@PathParam("publicKey") String publicKey) {
        LOGGER.info("Fetching brand with ID: " + publicKey);
        return brandsService.getBrandByOwnerPublicKey(publicKey)
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

    @GET
    @Path("/{brandName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAccountByUsername(@PathParam("brandName") String brandName) {
        LOGGER.info("Fetching Brand with brandName: " + brandName);
        return brandsService.getBrandByBrandName(brandName)
                .onItem().transform(account -> account != null ? Response.ok(account).build() : Response.status(Response.Status.NOT_FOUND).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error("Failed to fetch Brand", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
                });
    }


}

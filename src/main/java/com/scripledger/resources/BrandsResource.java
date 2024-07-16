package com.scripledger.resources;

import com.scripledger.models.Brand;
import com.scripledger.models.Token;
import com.scripledger.services.BrandsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
        LOGGER.info("Creating brand: " + brand);
        LOGGER.info("Received transaction: " + brand);
        return brandsService.createBrand(brand)
                .onItem().invoke(createdBrand -> LOGGER.info("Brand created: " + createdBrand))
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
}

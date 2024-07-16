package com.scripledger.services;

import com.scripledger.models.Brand;
import com.scripledger.models.Token;
import com.scripledger.repositories.BrandsRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class BrandsService {

    @Inject
    BrandsRepository brandsRepository;

    private static final Logger LOGGER = Logger.getLogger(BrandsService.class);

    public Uni<Brand> createBrand(Brand brand) {
        LOGGER.info("Service: Persisting brand: " + brand);
        return checkIfBrandExists(brand).flatMap(exists -> {
            if (exists) {
                return Uni.createFrom().failure(new RuntimeException("Brand with brand " + brand.getBrandName() + " already exists"));
            } else {
                return createNewBrand(brand);
            }
        }).onFailure().invoke(ex -> LOGGER.error("Failed to create account", ex));
    }

    public Uni<Brand> getBrand(String brandId) {
        ObjectId objectId = new ObjectId(brandId);
        LOGGER.info("Service: Retrieving brand with ID: " + brandId);
        return brandsRepository.findById(objectId);
    }

    public Uni<List<Token>> getTokenList(String brandId) {
        ObjectId objectId = new ObjectId(brandId);
        LOGGER.info("Service: Retrieving brand tokens with ID: " + brandId);
        return brandsRepository.findById(objectId).map(Brand::getTokens);
    }


    private Uni<Boolean> checkIfBrandExists(Brand brand) {
        return brandsRepository.find("brandName", brand.getBrandName()).firstResult().map(Objects::nonNull);
    }

    private Uni<Brand> createNewBrand(Brand brand) {
        return brandsRepository.persist(brand);
    }


}

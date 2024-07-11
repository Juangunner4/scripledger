package com.scripledger.services;

import com.scripledger.models.Brand;
import com.scripledger.repositories.BrandsRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BrandsService {

    @Inject
    BrandsRepository brandsRepository;

    private static final Logger LOGGER = Logger.getLogger(BrandsService.class);

    public Uni<Brand> createBrand(Brand brand) {
        LOGGER.info("Service: Persisting brand: " + brand);
        return brandsRepository.persist(brand);
    }

    public Uni<Brand> getBrand(String brandId) {
        LOGGER.info("Service: Retrieving brand with ID: " + brandId);
        return brandsRepository.find("brandId", brandId).firstResult();
    }

    private Uni<Boolean> checkIfBrandExists(Brand brand) {

    }

    private Uni<Brand> createNewBrand(Brand brand) {

    }


}

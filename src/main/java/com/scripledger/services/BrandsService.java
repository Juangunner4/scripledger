package com.scripledger.services;

import com.scripledger.collections.Brand;
import com.scripledger.collections.Token;
import com.scripledger.repositories.BrandsRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class BrandsService {

    @Inject
    BrandsRepository brandsRepository;


    private static final Logger LOGGER = Logger.getLogger(BrandsService.class);

    public Uni<Brand> createBrand(Brand brand) {
        return isBrandRegistered(brand, registerNewBrand(brand),"Failed to create brand"  );
    }

    public Uni<Brand> getBrandById(String brandId) {
        ObjectId objectId = new ObjectId(brandId);
        LOGGER.info("Service: Retrieving brand with ID: " + brandId);
        return brandsRepository.findById(objectId);
    }

    public Uni<Brand> getBrandByOwnerPublicKey(String publicKey) {
        LOGGER.info("Service: Retrieving Brand with ownerPublicKey: " + publicKey);
        return brandsRepository.find("ownerPublicKey", publicKey)
                .firstResult()
                .onItem().invoke(brand -> {
                    if (brand != null) {
                        LOGGER.info("Brand found: " + brand.getBrandName());
                    } else {
                        LOGGER.warn("No Brand found with ownerPublicKey: " + publicKey);
                    }
                });
    }
    public Uni<List<Brand>> getBrandsByMintPublicKeys(List<String> mintPubKeys) {
        LOGGER.info("Fetching brands associated with mintPublicKeys: " + mintPubKeys);

        Document query = new Document("tokens.mintPublicKey", new Document("$in", mintPubKeys));

        LOGGER.info("Generated MongoDB query: " + query.toJson());

        return brandsRepository.find(query).list()
                .onItem().invoke(brands -> {
                    if (!brands.isEmpty()) {
                        LOGGER.info("Brands fetched: " + brands.stream()
                                .map(Brand::getBrandName)
                                .collect(Collectors.joining(", ")));
                    } else {
                        LOGGER.warn("No brands found for provided mintPublicKeys: " + mintPubKeys);
                    }
                })
                .onFailure().invoke(Throwable::printStackTrace);
    }

    public Uni<Brand> getBrandByBrandName(String brandName) {
        LOGGER.info("Service: Retrieving brand with brandName: " + brandName);
        return brandsRepository.find("brandName", brandName)
                .firstResult()
                .onItem().invoke(brand -> {
                    if (brand != null) {
                        LOGGER.info("Brand found: " + brand.getBrandName());
                    } else {
                        LOGGER.warn("No Brand found with brandName: " + brandName);
                    }
                });
    }

    public Uni<List<Token>> getTokenList(String brandId) {
        ObjectId objectId = new ObjectId(brandId);
        LOGGER.info("Service: Retrieving brand tokens with ID: " + brandId);
        return brandsRepository.findById(objectId).map(Brand::getTokens);
    }

    public void updateBrandWithToken(Token token) {
        var publicKey = String.valueOf(token.getOwnerPublicKey());
        LOGGER.info("Updating brand for ownerPublicKey: " + publicKey);

        getBrandByOwnerPublicKey(publicKey).subscribe().with(ownerBrand -> {
            if (ownerBrand != null) {
                if (ownerBrand.getTokens() == null) {
                    LOGGER.info("Adding First Token to brand " + token.getMintPublicKey());
                    List<Token> tokenList = new ArrayList<>();
                    tokenList.add(token);
                    ownerBrand.setTokens(tokenList);
                } else {
                    LOGGER.info("Adding New Token to brand " + token.getMintPublicKey());
                    ownerBrand.getTokens().add(token);
                }
                brandsRepository.update(ownerBrand)
                        .subscribe().with(
                                updatedBrand -> LOGGER.info("Brand successfully updated with new token."),
                                failure -> LOGGER.error("Error updating brand: " + failure.getMessage())
                        );
            } else {
                LOGGER.error("No brand found for ownerPublicKey: " + publicKey);
            }
        }, failure -> LOGGER.error("Error fetching brand for publicKey: " + publicKey + " - " + failure.getMessage()));
    }

    private Uni<Brand> isBrandRegistered(Brand brand, Uni<Brand> brandUni, String Failed_to_create_brand) {
        return checkIfBrandExists(brand.getBrandName())
                .flatMap(exists -> {
                    if (exists) {
                        return Uni.createFrom().failure(new RuntimeException("User with brandName " + brand.getBrandName() + " already exists"));
                    } else {
                        return brandUni;
                    }
                }).onFailure().invoke(ex -> LOGGER.error(Failed_to_create_brand, ex));
    }

    private Uni<Boolean> checkIfBrandExists(String username) {
        return brandsRepository.find("brandName", username).firstResult().map(Objects::nonNull);
    }

    private Uni<Brand> registerNewBrand(Brand brand) {
       return brandsRepository.persist(brand);
    }
}

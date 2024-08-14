package com.scripledger.services;

import com.scripledger.models.Balance;
import com.scripledger.models.Brand;
import com.scripledger.models.MintTokensRequest;
import com.scripledger.models.Token;
import com.scripledger.repositories.BrandsRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class BrandsService {

    @Inject
    BrandsRepository brandsRepository;

    @Inject
    SolanaService solanaService;

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

    public Uni<Uni<String>> mintTokens(MintTokensRequest request) {
        ObjectId objectId = new ObjectId(request.getBrandId());
        return brandsRepository.findById(objectId).flatMap(brand -> {

            if (brand == null) {
                return Uni.createFrom().failure(new RuntimeException("Brand not found"));
            }
            return solanaService.mintTokens(request.getTokenAccountPublicKeyStr(), request.getOwnerPublicKeyStr(), request.getAmount())
                    .flatMap(txSignature -> {
                        List<Token> tokenList = new ArrayList<>();
                        Token token = new Token();
                        token.setPublicKey(brand.getBrandTokenMintAddress());

                        Balance balance = new Balance();
                        balance.setTokenName(request.getTokenName());
                        balance.setAmount(request.getAmount());
                        tokenList.add(token);
                        brand.setTokens(tokenList);
                        return brandsRepository.update(brand)
                                .map(updated -> txSignature);
                    });
        }).onFailure().invoke(ex -> LOGGER.error("Failed to mint tokens", ex));
    }




    private Uni<Boolean> checkIfBrandExists(Brand brand) {
        return brandsRepository.find("brandName", brand.getBrandName()).firstResult().map(Objects::nonNull);
    }

    private Uni<Brand> createNewBrand(Brand brand) {
        String publicKey = brand.getOwnerPublicKey();

        return solanaService.createBrandTokenAccount(publicKey, 100000000L)
                .flatMap(txSignature -> brandsRepository.persist(brand)
                        .map(persistedBrand -> {
                            persistedBrand.setOwnerPublicKey(publicKey);
                            return persistedBrand;
                        }));
    }


}

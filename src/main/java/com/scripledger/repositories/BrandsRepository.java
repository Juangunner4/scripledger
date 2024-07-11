package com.scripledger.repositories;

import com.scripledger.models.Brand;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BrandsRepository implements ReactivePanacheMongoRepository<Brand> {

}
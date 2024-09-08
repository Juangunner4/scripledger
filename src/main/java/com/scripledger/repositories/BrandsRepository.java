package com.scripledger.repositories;

import com.scripledger.collections.Brand;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BrandsRepository implements ReactivePanacheMongoRepository<Brand> {

    public Uni<Brand> update(Brand brand) {
        return this.persistOrUpdate(brand);
    }

}
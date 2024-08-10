package com.scripledger.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MongoConfig {

    private static final Logger LOG = Logger.getLogger(MongoConfig.class);

    private MongoClient mongoClient;

    void onStart(@Observes StartupEvent ev) {
        String connectionString = System.getenv("MONGODB_CONNECTION_STRING");

        LOG.info("MongoDB Connection String: " + connectionString);

        mongoClient = MongoClients.create(connectionString);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}


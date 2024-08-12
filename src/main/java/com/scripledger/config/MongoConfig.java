package com.scripledger.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@ApplicationScoped
public class MongoConfig {

    private static final Logger LOG = Logger.getLogger(MongoConfig.class);

    private MongoClient mongoClient;

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    String connectionString;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("MongoDB Connection String: " + connectionString);

        if (connectionString == null || connectionString.isEmpty()) {
            LOG.error("MongoDB Connection String is not set!");
            throw new IllegalArgumentException("MongoDB Connection String is not set");
        }

        try {
            mongoClient = MongoClients.create(connectionString);
        } catch (Exception e) {
            LOG.error("Failed to create MongoClient", e);
            throw e;
        }
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}

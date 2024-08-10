package com.scripledger.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MongoConfig {

    private static final Logger LOG = Logger.getLogger(MongoConfig.class);

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    String connectionString;

    private MongoClient mongoClient;

    void onStart(@Observes StartupEvent ev) {
        String username = System.getProperty("mongodb.username");
        String password = System.getProperty("mongodb.password");

        LOG.info("MongoDB Username: " + username);
        LOG.info("MongoDB Password: " + password);

        if (username != null && password != null) {
            connectionString = connectionString
                    .replace("<username>", username)
                    .replace("<password>", password);
        }

        LOG.info("MongoDB Connection String: " + connectionString);

        mongoClient = MongoClients.create(connectionString);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}


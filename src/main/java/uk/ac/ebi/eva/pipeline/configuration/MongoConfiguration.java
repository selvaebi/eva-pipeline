/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.pipeline.configuration;

import java.net.UnknownHostException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;

import uk.ac.ebi.eva.pipeline.parameters.MongoConnection;
import uk.ac.ebi.eva.utils.MongoDBHelper;

/**
 * Utility class dealing with MongoDB connections using pipeline options
 */
@Configuration
public class MongoConfiguration {

    @Autowired
    private MongoConnection mongoConnection;

    @Autowired
    private MongoMappingContext mongoMappingContext;

    @Bean
    public MongoMappingContext mongoMappingContext() {
        return new MongoMappingContext();
    }

    public MongoOperations getMongoOperations(String database)
            throws UnknownHostException {
        MongoClient mongoClient = getMongoClient();
        MongoDbFactory mongoFactory = getMongoDbFactory(mongoClient, database);
        MongoTemplate mongoTemplate = new MongoTemplate(mongoFactory, getMappingMongoConverter(mongoFactory));
        return mongoTemplate;
    }

    private MongoDbFactory getMongoDbFactory(MongoClient client, String database) {
        return new SimpleMongoDbFactory(client, database);
    }

    private MongoClient getMongoClient() throws UnknownHostException {
        String authenticationDatabase = null;
        String user = null;
        String password = null;
        MongoClient mongoClient;
        
        // The Mongo API is not happy to deal with empty strings for authentication DB, user and password
        if (mongoConnection.getAuthenticationDatabase() != null && !mongoConnection.getAuthenticationDatabase().trim()
                .isEmpty()) {
            authenticationDatabase = mongoConnection.getAuthenticationDatabase();
        }
        if (mongoConnection.getUser() != null && !mongoConnection.getUser().trim().isEmpty()) {
            user = mongoConnection.getUser();
        }
        if (mongoConnection.getPassword() != null && !mongoConnection.getPassword().trim().isEmpty()) {
            password = mongoConnection.getPassword();
        }
        
        if (user == null || password == null) {
            mongoClient = new MongoClient(MongoDBHelper.parseServerAddresses(mongoConnection.getHosts()));
        } else {
            mongoClient = new MongoClient(
                    MongoDBHelper.parseServerAddresses(mongoConnection.getHosts()),
                    Collections.singletonList(MongoCredential.createCredential(mongoConnection.getUser(),
                            authenticationDatabase, mongoConnection.getPassword().toCharArray())));
        }
        mongoClient.setReadPreference(mongoConnection.getReadPreference());

        return mongoClient;
    }

    private MappingMongoConverter getMappingMongoConverter(MongoDbFactory mongoFactory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);

        // Customization: replace dots with pound sign
        mongoConverter.setMapKeyDotReplacement("£");

        return mongoConverter;
    }

}

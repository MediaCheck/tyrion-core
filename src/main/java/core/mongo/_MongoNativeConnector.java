package core.mongo;


import com.google.inject.Singleton;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.typesafe.config.Config;
import com.google.inject.Inject;
import core.common.ServerMode;

import java.util.HashMap;

@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class _MongoNativeConnector {

    private Config config;
    private String mode;
    private String url;

    private MongoClient mongoClient;
    private HashMap<String, MongoDatabase> databases = new HashMap<>();
    private MongoDatabase main_database;


    @Inject
    public _MongoNativeConnector(Config config) {

        // SET Values
        this.mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();
        this.config = config;
        this.url = config.getString("MongoDB." + mode + ".url"); // Cluster

        mongoClient = MongoClients.create(url);
        main_database = this.getDatabase(config.getString("MongoDB." + mode + ".main_database_name"));

    }

    public MongoDatabase getDatabase(String database_name) {

        if(!databases.containsKey(database_name)) {
            MongoDatabase database = mongoClient.getDatabase(database_name);
            databases.put(database_name, database);
        }

        return databases.get(database_name);
    }

    public _MongoNativeCollection getDatabaseCollection(String database_name, String collection_name) {

        MongoDatabase database = getDatabase(database_name);
        return  new _MongoNativeCollection(database, collection_name);
    }

    public _MongoNativeCollection getCollection(String collection_name) {
        return  new _MongoNativeCollection( this.main_database, collection_name);
    }

}

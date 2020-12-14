package core.mongo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.common.ServerMode;
import xyz.morphia.Datastore;
import xyz.morphia.Morphia;
import xyz.morphia.annotations.Entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Singleton
public class _MongoDBConnector {

    private static final Logger logger = LoggerFactory.getLogger(_MongoDBConnector.class);

    private Config config;
    private String mode;
    private Morphia morphia;

    private final String url; // Main Database URL
    private final String name; // Main Database Name;

    private HashMap<String, MongoClient> databases = new HashMap<>(); // < URL < Name , DB > >


    @Inject
    @SuppressWarnings("unchecked")
    public _MongoDBConnector(Config config, ObjectCreator objectCreator) {

        this.config = config;

        this.morphia = new Morphia();
        this.morphia.getMapper().getOptions().setObjectFactory(objectCreator);

        // SET Values
        this.mode = config.getEnum(ServerMode.class, "server.mode").name().toLowerCase();
        this.name = config.getString("MongoDB." + mode + ".main_database_name");
        this.url = config.getString("MongoDB." + mode + ".url"); // Cluster


        // CONNECT TO MONGO CLUSTER
        MongoClientOptions.Builder options_builder = new MongoClientOptions.Builder();
        options_builder.maxConnectionIdleTime(1000 * 60 * 60 * 24);
        MongoClient mongoClient = new MongoClient(new MongoClientURI(this.url, options_builder));

        this.databases.put(url, mongoClient);

        // TRY TO CONNECT
        try {
            this.databases.get(url).getAddress();
        } catch (Exception e) {
            logger.error("constructor - Mongo is down", e);
            this.databases.get(url).close();
            return;
        }

        List<String> packages = new ArrayList<>();

        try {
            packages = config.getStringList("mongo.models");
        } catch (Exception e) {
            logger.warn("constructor - error loading mongo models configuration", e);
        }

        ConfigurationBuilder builder = new ConfigurationBuilder().setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());

        packages.forEach(pack -> builder.addUrls(ClasspathHelper.forPackage(pack)));

        Reflections reflections = new Reflections(builder);

        reflections.getTypesAnnotatedWith(Entity.class).forEach(cls -> {
            try {

                Class<? extends BaseMongoModel> model = (Class<? extends BaseMongoModel>) cls; // Cast to model
                Entity annotation = model.getAnnotation(Entity.class);

                String collection_name = getProperName(annotation, model);
                ConnectionConfig connection_config = getConnectionConfig(model);

                if(!databases.containsKey(connection_config.database_url)) {
                    MongoClientOptions.Builder optionsBuilder = new MongoClientOptions.Builder();
                    optionsBuilder.maxConnectionIdleTime(1000 * 60 * 60 * 24);
                    MongoClient client = new MongoClient(new MongoClientURI(connection_config.database_url, optionsBuilder));
                    databases.put(connection_config.database_url,client);
                }

                if (!this.databases.get(this.url).getDatabase(this.getConnectionConfig(model).database_name).listCollectionNames().into(new ArrayList<>()).contains(collection_name)) {
                    logger.error("constructor - {} {} Collection:: {}  - not exist. System will create that! ", model.getSimpleName(),  model.getCanonicalName(), collection_name);
                    this.databases.get(this.url).getDatabase(this.getConnectionConfig(model).database_name).createCollection(collection_name);
                }

                for (Field field : model.getFields()) {
                    if (field.isAnnotationPresent(InjectStore.class) && field.get(null) instanceof CacheMongoFinder) {

                        if(!model.getCanonicalName().contains("mongo") && !model.getSimpleName().contains("ModelMongo")) {
                            logger.error("constructor - {} Collection:: {}  - Class must start with ModelMongo prefix ", model.getSimpleName(), collection_name);
                            return;
                        }

                        // Mongo ORM zástupný onbjekt pro lepší práci s databází
                        this.setFiledForCacheMongoFinder(field, connection_config.database_name);
                    }
                }

            } catch (Exception e) {
                logger.error("constructor", e);
            }
        });
    }


    private void setFiledForCacheMongoFinder(Field field, String database_name) throws IllegalAccessException {
        Datastore datastore = this.morphia.createDatastore(this.databases.get(this.url), database_name);
        ((CacheMongoFinder)field.get(null)).setDatastore(datastore);
    }

    /**
     * Get proper name of Class
     * @param annotation
     * @return
     */
    private String getProperName( Entity annotation,  Class<? extends BaseMongoModel> model ) {
        String value = annotation.value();
        if(value.equals(".")) {
            value = model.getSimpleName();
        }

        return value;
    }

    /**
     * Get Database name
     * @param model Class for parsing
     * @return ConnectionConfig - name of database
     */
    private ConnectionConfig getConnectionConfig(Class<? extends BaseMongoModel> model) {

        _MongoCollectionConfig collectionConfig = model.getAnnotation(_MongoCollectionConfig.class);

        ConnectionConfig connection = new ConnectionConfig();

        // If database Name is missing - set Defailt Database
        if(collectionConfig == null) {
            //System.out.println("getConnectionConfig:: _MongoCollectionConfig anotation is missing Return default");
            connection.database_url  = this.config.getString("MongoDB." + mode + ".url");
            connection.database_name = this.config.getString("MongoDB." + mode + ".main_database_name");
            return connection;
        }


        // If DatabaseName is not missing, but URL is missing, set defailt URL but diferent Database
        if(collectionConfig.database_name().length() > 0 && collectionConfig.database_url().equals("")) {

           // System.out.println("getConnectionConfig:: _MongoCollectionConfig Database Name" + collectionConfig.database_name() + " url is null");

            connection.database_url  = this.config.getString("MongoDB." + mode + ".url");
            connection.database_name = collectionConfig.database_name();
            return connection;
        }


        // If DatabaseName is not missing, but URL is missing, set defailt URL but diferent Database
        if(collectionConfig.database_name().length() > 0 && collectionConfig.database_url().length() > 0) {

           // System.out.println("getConnectionConfig:: _MongoCollectionConfig Database Name" + collectionConfig.database_name() + " there is a url:: " + collectionConfig.database_url());

            connection.database_url  = collectionConfig.database_name();
            connection.database_name = collectionConfig.database_url();
            return connection;
        }

        throw new IllegalStateException("_MongoDBConnector getDatabaseName Error, unsupported configuration");

    }

    private class ConnectionConfig {
        public String database_name;
        public String database_url;
    }

    public MongoClient getMainMongoClient() {

        return this.databases.get(this.url);
    }

    /**
     * Return Main Database for Tyrion
     * @return
     */
    public MongoDatabase getDatabase() {
        return this.databases.get(this.url).getDatabase(this.name);
    }

    /**
     * Return  Database by name in Tyrion Main Connection URL
     * @return
     */
    public MongoDatabase getDatabase(String name) {
        return this.databases.get(this.url).getDatabase(name);
    }

    /**
     * Return Database on Specific connetion URL
     * @param name
     * @param url
     * @return
     */
    public MongoDatabase getDatabase(String url, String name) {
        return this.databases.get(url).getDatabase(name);
    }
}

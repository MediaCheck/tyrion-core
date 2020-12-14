package core.mongo;

import com.mongodb.*;
import core.cache.ModelCache;
import core.exceptions.BadRequestException;
import core.exceptions.NotFoundException;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morphia.Datastore;
import xyz.morphia.Key;
import xyz.morphia.aggregation.AggregationPipeline;
import xyz.morphia.query.Query;

import java.util.List;

public class CacheMongoFinder<T extends BaseMongoModel> implements ModelCache<ObjectId, T> {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(CacheMongoFinder.class);

/* VALUE  -------------------------------------------------------------------------------------------------------------*/

    private Datastore datastore; // Main Data Store

    /**
     * The entity bean type.
     */
    private final Class<T> entityType;

    private Cache<ObjectId, T> cache;
    private Cache<Integer, ObjectId> queryCache;

/* CONSTRUCTOR  -------------------------------------------------------------------------------------------------------*/

    public CacheMongoFinder(Class<T > cls) {
        this.entityType = cls;
    }

    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public T byId(String id) throws NotFoundException, BadRequestException {
        try {
            return this.byId(new ObjectId(id));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Id is not UUID or Object id Format");
        }
    }

    /**
     * Retrieves an entity by ID.
     * <p>
     */
    public T byId(ObjectId id) throws NotFoundException  {

        if (cache.containsKey(id)) {
            logger.debug("byId - ({}) id: {} get from cache", this.entityType.getSimpleName(), id);
            return cache.get(id);
        }

        logger.debug("byId - ({}) id: {} get from db", this.entityType.getSimpleName(), id);

        T entity = bySingleArgument("id", id);

        if (entity == null) {
            logger.debug("byId - ({}) id: {} not found", this.entityType.getSimpleName(), id);
            throw new NotFoundException(this.entityType);
        }

        cache.put(id, entity);

        return entity;
    }

    /**
     * Easy Query to find something by single key and value, tipicaly its possible to use that
     * for Name, Email, ID etc.
     * Be aware that ObjectId is converted to String!
     * @param key       -  it must be a String value without spaces or diacritics! We used Snake case conventions!
     * @param value     -  Object like boolean, long, integer etc.
     * @return
     */
    public T bySingleArgument(String key, Object value) {
        try {

            Query<T> query = this.datastore.find(entityType);

            query.field(key).equal(value);
            query.or(
                    query.criteria("deleted").equal(false),
                    query.criteria("deleted").doesNotExist()
            );
            query.order("created");

            return query.get();

        } catch (Exception e) {
            return null;
        }
    }

/* DATASTORE Override  -------------------------------------------------------------------------------------------------*/

    /**
     * Saves an entity (Object) and updates the @Id field
     *
     * @param entity the entity to save
     * @param <T>    the type of the entity
     * @return the keys of the entity
     */
    public <T> Key<T> save(T entity) {
        return this.datastore.save(entity);
    }

    /**
     * Deletes the given entity (by @Id)
     *
     * @param entity the entity to delete
     * @param <T>    the type to delete
     * @return results of the delete
     */
    public <T> WriteResult delete(T entity) {
        return this.datastore.delete(entity);
    }

    /**
     * Returns a new query bound to the collection (a specific {@link DBCollection})
     * @return the query
     */
    public Query<T> query() {
        Query<T> query = this.datastore.createQuery(entityType);
        query.or(
                query.criteria("deleted").equal(false),
                query.criteria("deleted").doesNotExist()
        );

        return query.disableValidation();
    }

    /**
     * Select documents in collection and get a cursor to the selected documents.
     *
     * @param query the selection criteria using query operators. Omit the query parameter or pass an empty document to return all documents
     *              in the collection.
     * @return A cursor to the documents that match the query criteria
     * @mongodb.driver.manual tutorial/query-documents/ Querying
     */
    public DBCursor find(final DBObject query){
        return this.datastore.getCollection(entityType).find(query);
    }

    public AggregationPipeline createAggregation() {
        return this.datastore.createAggregation(entityType);
    }

    /**
     * Find all instances by type
     * @return
     */
    public List<T> all(){
        return this.datastore.find(entityType).asList();
    }

    /**
     * @return the mapped collection for the collection
     */
    public DBCollection getCollection() {
        return this.datastore.getCollection(entityType);
    }



/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public void setCache(Cache<ObjectId, T> cache) {
        this.cache = cache;
    }

    public Cache<ObjectId, T> getCache() {
        return this.cache;
    }

    public void setQueryCache(Cache<Integer, ObjectId> cache) {
        this.queryCache = cache;
    }

    public Cache<Integer, ObjectId> getQueryCache() {
        return this.queryCache;
    }

    public Class<T> getEntity_type() {
        return this.entityType;
    }

    public void cache(ObjectId key, T value) {
        logger.trace("cache - ({}) caching by key: {}", this.entityType.getSimpleName(), key);
        cache.put(key, value);
    }

    public void evict(ObjectId key) {
        logger.trace("evict - ({}) removing by key: {}", this.entityType.getSimpleName(), key);
        cache.remove(key);
    }
}

package core.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.cache.InjectCache;
import core.common.JsonSerializable;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.annotations.Property;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Indexes({
        @Index(
                fields = {
                        // In this case, we have more anotation types with same name
                        @xyz.morphia.annotations.Field("id"),
                }
        )
})
public abstract class BaseMongoModel implements JsonSerializable {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(BaseMongoModel.class);

/* COMMON VALUES -------------------------------------------------------------------------------------------------------*/

    // Public
    @Id
    @Property("id")
    @JsonProperty()
    @JsonIgnore
    public ObjectId id;

    @Property("created")
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public LocalDateTime created;

    @Property("updated")
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public LocalDateTime updated;

    @JsonIgnore
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public LocalDateTime removed;

    @JsonIgnore
    public boolean deleted; // Default value is false in save()

    @JsonIgnore
    public UUID author_id; // Default value is false in save()

    @JsonIgnore @Property("server_version") private String tyrion_version;      // Special Value
    @JsonIgnore                             private String tyrion_mode;         // Special Value
    @JsonIgnore                             private String tyrion_cluster_id;   // Special Value


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, value = " ID", readOnly = true, dataType = "String", example = "5b508290-a026-410c-bdbc-6cdf99f48043")
    @JsonProperty()
    public String id(){
        return id.toString();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public ObjectId getObjectId() {
        return id;
    }

    /**
     * Converts this model to JSON
     * @return JSON representation of this model
     */
    public ObjectNode json() {
        return (ObjectNode) Json.toJson(this);
    }

    /**
     * Converts this model to JSON and than stringify
     * @return string from JSON representation
     */
    public String string() {
        return json().toString();
    }

    /**
     * Converts this model to printable string
     * @return formatted string
     */
    @JsonIgnore
    public String prettyPrint() {
        return this.getClass() + ":\n" + Json.prettyPrint(json());
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @JsonIgnore public void save() {

        // Someone call Save when object is already created in database

        if( this.id != null) {
            this.update();
            return;
        }

        this.id = new ObjectId();

        // Set Time
        if (this.created == null) {
            this.created = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (this.updated == null) {
            this.updated = LocalDateTime.now(ZoneOffset.UTC);
        }
        // new Thread(this::cache).start(); // Caches the object

        this.tyrion_version     = "deprecated"; // TODO should not be used
        this.tyrion_mode        = "deprecated"; // TODO should not be used
        this.tyrion_cluster_id  = "deprecated"; // TODO should not be used

        getFinder().save(this);
        new Thread(this::cache).start(); // Caches the object
    }

    @JsonIgnore public void update() {

        // Set Time
        this.updated = LocalDateTime.now(ZoneOffset.UTC);

        // Save Document do Mongo Database
        getFinder().save(this);

        new Thread(this::cache).start();
    }

    /**
     * Its not Real Delete from Database, but only set as deleted with one common boolean value!
     * For Real Remove use {@link #deletePermanent() } instead
     */
    @JsonIgnore public void delete() {

        // Set Time
        this.removed = LocalDateTime.now(ZoneOffset.UTC);
        this.deleted = true;

        // Not Remove, but update!
        getFinder().save(this);

        // Evict the object from cache
        new Thread(this::evict).start();
    }

    /**
     * Permanently remove from Mongo DB,
     * its not possible to resque that without backup on database!!!
     * Do it only if you know, what you are doing!
     */
    @JsonIgnore public void deletePermanent() {

        // Remove Permanently
        getFinder().delete(this);
        new Thread(this::evict).start();
    }


    /**
     * Method finds the cache field in the class
     * and if present it puts or replaces the value in the cache.
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void cache() {
        long start = System.currentTimeMillis();
        Class<? extends BaseMongoModel> cls = this.getClass();

        logger.trace("cache - finding cache finder for {}", cls.getSimpleName());

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheMongoFinder.class)) {
                try {

                    logger.debug("cache - found cache finder field");

                    CacheMongoFinder<BaseMongoModel> cacheFinder = (CacheMongoFinder<BaseMongoModel>) field.get(null);
                    cacheFinder.cache(this.id, this);

                } catch (IllegalStateException e) {
                    // Nothing bug on local development
                } catch (Exception e) {
                    logger.error("cache", e);
                }
            }
        }
    }

    /**
     * This method should be called when object is no longer fresh
     * and it should be removed from the cache. It finds the cache field
     * and if present it removes this object from it.
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void evict() {
        long start = System.currentTimeMillis();
        Class<? extends BaseMongoModel> cls = this.getClass();

        logger.trace("evict - finding cache finder for {}", cls.getSimpleName());

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheMongoFinder.class)) {
                try {

                    logger.debug("evict - found cache finder field");

                    CacheMongoFinder<?> cacheFinder = (CacheMongoFinder<?>) field.get(null);
                    cacheFinder.evict(this.id);

                } catch (Exception e) {
                    logger.error("evict", e);
                }
            }
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public abstract CacheMongoFinder<?> getFinder();

}

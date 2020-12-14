package core.mongo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.exceptions.UnauthorizedException;
import core.mongo.BaseMongoModel;
import core.mongo.CacheMongoFinder;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;

public abstract  class _MongoModel_AbstractToken<T extends BaseMongoModel> extends BaseMongoModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(_MongoModel_AbstractToken.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    // UUID Token
    public String token;

    // Owner
    @JsonIgnore
    public ObjectId person_id;

    // Time to expiration
    public LocalDateTime access_age;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void isValid() {
        try {
            if (this.access_age != null) {
                if (this.access_age.isBefore( LocalDateTime.now(ZoneOffset.UTC))) {
                    logger.trace("isValid - token is expired");
                    this.delete();
                } else {
                    this.access_age = LocalDateTime.now(ZoneOffset.UTC).plus(72, HOURS);
                    this.update();
                    return;
                }
            } else {
                logger.trace("isValid - token is probably permanent");
                return;
            }
        } catch (Exception e) {
            logger.error("isValid", e);
        }

        throw new UnauthorizedException();
    }

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void clear_all(ObjectId person_id) {
        getFinder().query().field("person_id").equal(person_id).asList().forEach(BaseMongoModel::deletePermanent);
    }


    public T getByToken(UUID token) {
        T databased_token = getFinder().query().field("token").equal(token.toString()).get();
        if(databased_token == null) throw new UnauthorizedException();
        return databased_token;
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        this.token = UUID.randomUUID().toString();
        super.save();
    }

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public abstract CacheMongoFinder<T> getFinder();

}

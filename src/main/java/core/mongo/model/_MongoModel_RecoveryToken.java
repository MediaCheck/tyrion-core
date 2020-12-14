package core.mongo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.exceptions.NotFoundException;
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

public abstract  class _MongoModel_RecoveryToken<T extends BaseMongoModel> extends BaseMongoModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(_MongoModel_RecoveryToken.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public Enum_RecoveryType type;
    public ObjectId person_id;
    public String email;
    public String new_password;

    public String token;


    /* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void  setPasswordRecoveryToken() {
        while(true) { // I need Unique Value
            this.token = UUID.randomUUID().toString();
            if (getFinder().query().field("password_recovery_token").equal(this.token).count() == 0) break;
        }
    }

    @JsonIgnore
    public _MongoModel_RecoveryToken setValidation(String email) {

        this.email = email;
        this.token = UUID.randomUUID().toString();

        save();
        return this;
    }

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        if(token == null) {
            throw new IllegalArgumentException("_MongoModel_RecoveryToken token is missing");
        }

        super.save();
    }

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public T getByEmail(String email) {
        T t = getFinder().query().field("email").equalIgnoreCase(email).get();
        return t;
    }


    @JsonIgnore
    public T getByToken(String token) {
        T t = getFinder().query().field("token").equal(token).get();
        return t;
    }
    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public abstract CacheMongoFinder<T> getFinder();

}

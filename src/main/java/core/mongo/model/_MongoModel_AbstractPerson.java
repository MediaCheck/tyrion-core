package core.mongo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import core.storage.StoredItem;
import core.util.RegistrationState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import core.exceptions.BadRequestException;
import core.exceptions.NotFoundException;
import io.swagger.annotations.ApiModelProperty;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.mongo.CacheMongoFinder;
import core.mongo.BaseMongoModel;
import xyz.morphia.annotations.Embedded;

public abstract class _MongoModel_AbstractPerson<T extends BaseMongoModel> extends BaseMongoModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(_MongoModel_AbstractPerson.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, value = "Optional value set by Service")
    public String email; // Byzance Hardware ID (not Full ID)

    public String first_name;
    public String last_name;

    @JsonIgnore public String password;

    @JsonIgnore public Boolean validated;       // True - Default
    @JsonIgnore public Boolean frozen;          // False - Default

    public RegistrationState state;

    @JsonIgnore @Embedded
    public StoredItem picture;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public String picture_link() {
        return this.picture != null ? this.picture.getLink() : null;
    }

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @JsonIgnore
    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /**
     * Override if you want change default settings
     */
    @Override
    public void save() {

        System.out.println("_MongoModel_AbstractPerson save");

        try {
            if (this.getByEmail(this.email) != null) {
                throw new BadRequestException("User with this email Already Registered");
            }
        } catch (NotFoundException e) {
            // Its Ok
        }

        if (validated == null) {
            this.validated = true;
        }

        if(frozen == null) {
            this.frozen = false;
        }

        super.save();
    }

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public abstract String get_path();


    @JsonIgnore
    protected String get_personal_picture_path() {
        return "/pictures_persons" + "/" + this.id;
    }

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    /* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public T getByEmail(String email) {
        T t = getFinder().query().field("email").equalIgnoreCase(email).get();
        if(t == null) throw new NotFoundException(this.getClass());
        return t;
    }

    /**
     * If user is not created by self, but invited to project etc.
     * @param email
     * @return
     */
    @JsonIgnore
    public T getInvitedAccount(String email) {
        T t = getFinder().query().field("email").equalIgnoreCase(email).field("state").equal(RegistrationState.INVITED).get();
        return t;
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override
    @JsonIgnore
    public abstract CacheMongoFinder<T> getFinder();

}

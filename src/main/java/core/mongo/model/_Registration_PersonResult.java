package core.mongo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.exceptions.BadRequestException;
import core.exceptions.NotFoundException;
import core.mongo.BaseMongoModel;
import core.mongo.CacheMongoFinder;
import core.storage.StoredItem;
import core.swagger._Swagger_Abstract_Default;
import core.util.RegistrationState;
import io.swagger.annotations.ApiModelProperty;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morphia.annotations.Embedded;

public class _Registration_PersonResult extends _Swagger_Abstract_Default {

    public boolean new_user_required_email_approve = false;
    public boolean registration_on_invitation = false;
    public _MongoModel_AbstractPerson person;
}

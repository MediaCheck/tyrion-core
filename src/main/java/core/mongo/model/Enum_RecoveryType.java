package core.mongo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.exceptions.NotFoundException;
import core.mongo.BaseMongoModel;
import core.mongo.CacheMongoFinder;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public enum Enum_RecoveryType {
    PASSWORD_RESTART,
    CHANGE_PASSWORD,
    ACCOUNT_EMAIL_VALIDATION
}
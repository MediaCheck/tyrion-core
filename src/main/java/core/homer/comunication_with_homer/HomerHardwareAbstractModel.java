package core.homer.comunication_with_homer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.mongo.BaseMongoModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public abstract class HomerHardwareAbstractModel extends BaseMongoModel {

    @JsonIgnore
    public abstract UUID getUUID();

    @JsonIgnore
    public abstract URL getInstanceURL() throws MalformedURLException;
}

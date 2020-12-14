package core.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.common.JsonSerializable;

public abstract class BaseResult implements JsonSerializable {

    @JsonIgnore public String message;
    @JsonIgnore public String state;
    @JsonIgnore public Integer code;

    @JsonProperty
    public abstract String state();

    @JsonProperty
    public abstract int code();

    @JsonProperty
    public abstract String message();

}

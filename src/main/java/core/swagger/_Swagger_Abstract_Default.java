package core.swagger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import core.common.JsonSerializable;

public abstract class _Swagger_Abstract_Default implements JsonSerializable {


    /** Converts this model to JSON
     * @return JSON representation of this model
     */
    @JsonIgnore
    @Override
    public ObjectNode json() {
        return (ObjectNode) Json.toJson(this);
    }


    /**
     * Converts this model to printable string
     * @return formatted string
     */
    @JsonIgnore
    public String prettyPrint() {
        return this.getClass() + ":\n" + Json.prettyPrint(json());
    }

}

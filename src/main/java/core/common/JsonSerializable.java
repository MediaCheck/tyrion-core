package core.common;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public interface JsonSerializable {

    default ObjectNode json() {
        return (ObjectNode) Json.toJson(this);
    }
}

package core.exceptions;

import com.fasterxml.jackson.databind.JsonNode;

public class InvalidBodyException extends BaseException {

    private JsonNode errors;

    public InvalidBodyException(JsonNode errors) {
        super(errors.toString());
        this.errors = errors;
    }

    public JsonNode getErrors() {
        return errors;
    }
}

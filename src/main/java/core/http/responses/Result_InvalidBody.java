package core.http.responses;

import com.fasterxml.jackson.databind.JsonNode;
import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Invalid_Body", description="Provided body is not valid. Something is missing or some field input is not allowed.")
public class Result_InvalidBody extends BaseResult {

    public Result_InvalidBody(JsonNode errors) {
        this.exception = errors;
    }

    @ApiModelProperty(value = "state", allowableValues = "invalid_body", required = true, readOnly = true)
    public String state() {
        return "invalid_body";
    }

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public int code() {
        return 400;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        return "Invalid body. Exception bellow";
    }

    @ApiModelProperty(required = true, readOnly = true, hidden = true)
    public JsonNode exception;
}

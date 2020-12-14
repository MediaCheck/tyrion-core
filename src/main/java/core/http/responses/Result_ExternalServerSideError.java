package core.http.responses;

import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_ExternalServerSideError", description="Unknown Error on external server ")
public class Result_ExternalServerSideError extends BaseResult {

    public Result_ExternalServerSideError() {}

    public Result_ExternalServerSideError(String message) {
        this.message = message;
    }


    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state() {
        return "internal_server_error";
    }

    @ApiModelProperty(value = "code", allowableValues = "XXX", required = true, readOnly = true)
    public int code() {
        return 478;
    }

    @ApiModelProperty(value = "Internal Server Error", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Internal Server Error";
    }

}

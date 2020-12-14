package core.http.responses;


import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Internal_Server_Error", description="When server is in fault state")
public class Result_InternalServerError extends BaseResult {

    public Result_InternalServerError() {}

    public Result_InternalServerError(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "internal_server_error", required = true, readOnly = true)
    public String state() {
        return "internal_server_error";
    }

    @ApiModelProperty(value = "code", allowableValues = "500", required = true, readOnly = true)
    public int code() {
        return 500;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Server encountered fatal error";
    }

}

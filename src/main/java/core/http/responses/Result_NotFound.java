package core.http.responses;

import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Found", description="Requested entity was not found")
public class Result_NotFound extends BaseResult {

    public Result_NotFound(String message) {
        this.message = message;
    }


    @ApiModelProperty(value = "state", allowableValues = "not_found", required = true, readOnly = true)
    public String state() {
        return "not_found";
    }

    @ApiModelProperty(value = "code", allowableValues = "404", required = true, readOnly = true)
    public int code() {
        return 404;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Required object not found. But Logs not specific witch one. Please in case of fire ;) contact technical support";
    }

}

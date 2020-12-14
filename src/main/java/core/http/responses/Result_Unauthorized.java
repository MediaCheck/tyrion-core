package core.http.responses;

import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Unauthorized")
public class Result_Unauthorized extends BaseResult {

    @ApiModelProperty(value = "state", allowableValues = "unauthorized", required = true, readOnly = true)
    public String state() {
        return "unauthorized";
    }

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public int code() {
        return 401;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Unauthorized access - please log in";
    }

}

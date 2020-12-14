package core.http.responses;

import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Custom_Result", description="Customizable response object")
public class Result_Custom extends BaseResult {

    public Result_Custom(){}
    public Result_Custom(String message, Integer code, String state){
        this.message = message;
        this.state = state;
        this.code = code;
    }

    @ApiModelProperty(value = "state", allowableValues = "error", required = true, readOnly = true)
    public String state() {
        return state;
    }

    @ApiModelProperty(value = "code", allowableValues = "XXX", required = true, readOnly = true)
    public int code() {
        return code;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        return message;
    }

}

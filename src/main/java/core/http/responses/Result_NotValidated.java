package core.http.responses;


import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Not_Validated", description="When account is not validated.")
public class Result_NotValidated extends BaseResult {

    public Result_NotValidated() {}

    public Result_NotValidated(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error_person_account_is_not_validated", required = true, readOnly = true)
    public String state() {
        return "error_person_account_is_not_validated";
    }

    @ApiModelProperty(value = "code", allowableValues = "705", required = true, readOnly = true)
    public int code() {
        return 705;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Your account is not validated";
    }

}

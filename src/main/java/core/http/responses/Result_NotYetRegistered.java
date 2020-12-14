package core.http.responses;


import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_NotYetRegistered", description="When account is not properly registered by email owner.")
public class Result_NotYetRegistered extends BaseResult {

    public Result_NotYetRegistered() {}

    public Result_NotYetRegistered(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error_person_account_is_not_validated", required = true, readOnly = true)
    public String state() {
        return "error_person_account_is_not_yet_properly_registered";
    }

    @ApiModelProperty(value = "code", allowableValues = "706", required = true, readOnly = true)
    public int code() {
        return 706;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Your account is not yet properly registered";
    }

}

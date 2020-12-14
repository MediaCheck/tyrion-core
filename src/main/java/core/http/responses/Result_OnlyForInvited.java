package core.http.responses;


import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_OnlyForInvited", description="When account is not properly registered by email owner.")
public class Result_OnlyForInvited extends BaseResult {

    public Result_OnlyForInvited() {}

    public Result_OnlyForInvited(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error_person_account_is_not_invited_to_register", required = true, readOnly = true)
    public String state() {
        return "error_person_account_is_not_invited_to_register";
    }

    @ApiModelProperty(value = "code", allowableValues = "706", required = true, readOnly = true)
    public int code() {
        return 707;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Only invited users can register own account";
    }

}

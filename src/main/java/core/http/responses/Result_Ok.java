package core.http.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Ok")
public class Result_Ok extends BaseResult {

    @JsonIgnore
    private Integer code = 200;

    public Result_Ok() {}

    public Result_Ok(String message) {
        this.message = message;
    }

    public Result_Ok(Integer code) {
        this.code = code;
    }

    @ApiModelProperty(value = "state", allowableValues = "error_person_account_is_not_validated", required = true, readOnly = true)
    public String state() {
        return "ok";
    }

    @ApiModelProperty(value = "code", allowableValues = "200, 718, etc", required = true, readOnly = true)
    public int code() {
        return code;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User. Server fills the message only when it is important", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "";
    }

}

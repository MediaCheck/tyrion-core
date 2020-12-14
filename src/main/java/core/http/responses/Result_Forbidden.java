package core.http.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Forbidden",
          description="Permission is needed for this action.")
public class Result_Forbidden extends BaseResult {

    @JsonIgnore public String message;
    @JsonIgnore public String state;
    @JsonIgnore public Integer code;

    public Result_Forbidden() {}

    public Result_Forbidden(String message) {
        this.message = message;
    }

    @JsonProperty @ApiModelProperty(allowableValues = "forbidden", required = true, readOnly = true)
    public String state() {
        return "forbidden";
    }

    @JsonProperty @ApiModelProperty(allowableValues = "403", required = true, readOnly = true)
    public int code() {

        return 403;
    }

    @JsonProperty @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "For this operation you have to be object owner, or you need special permission for that.";
    }

}

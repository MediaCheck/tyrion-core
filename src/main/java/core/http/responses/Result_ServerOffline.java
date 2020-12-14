package core.http.responses;

import core.http.BaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Server_Offline", description="External servers (compilation, blocko cloud_blocko_server etc.) are offline")
public class Result_ServerOffline extends BaseResult {

    public Result_ServerOffline(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "server_is_offline", required = true, readOnly = true)
    public String state() {
        return "server_is_offline";
    }

    @ApiModelProperty(value = "code", allowableValues = "403", required = true, readOnly = true)
    public int code() {
        return 477;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "Server is offline, operation is not supported now";
    }

}

package core.swagger.homer_auth.out;

import core.swagger._Swagger_Abstract_Default;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(
        value = "Homer_AuthenticationResult_HardwareLogSubscribe",
        description = ""
)
public class Swagger_Homer_AuthenticationResult_HardwareLogSubscribe extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true )
    public String hardware_id;

    @ApiModelProperty(required = true, readOnly = true )
    public boolean result = false;
}

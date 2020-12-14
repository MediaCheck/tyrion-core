package core.swagger.homer_auth.out;

import core.swagger._Swagger_Abstract_Default;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(
        value = "AuthenticationResult_PersonConnection",
        description = ""
)
public class Swagger_Homer_AuthenticationResult_PersonConnection extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true )
    public Boolean result;

}

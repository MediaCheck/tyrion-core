package core.swagger.homer_auth.in;

import core.swagger._Swagger_Abstract_Default;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(
        value = "Homer_AuthenticationRequest_PersonConnection",
        description = ""
)
public class Swagger_Homer_AuthenticationRequest_PersonConnection extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true )
    @Constraints.Required
    public String user_token;
}

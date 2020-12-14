package core.swagger.homer_auth.in;

import core.swagger._Swagger_Abstract_Default;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(
        value = "Homer_AuthenticationRequest_InstanceRemoteViewSubscribe",
        description = ""
)
public class Swagger_Homer_AuthenticationRequest_InstanceRemoteViewSubscribe extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true)
    @Constraints.Required
    public UUID instance_id;

    @ApiModelProperty(required = true, readOnly = true)
    @Constraints.Required
    public UUID user_token;

}

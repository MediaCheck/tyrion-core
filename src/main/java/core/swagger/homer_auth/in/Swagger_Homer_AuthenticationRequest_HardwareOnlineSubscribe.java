package core.swagger.homer_auth.in;

import core.swagger._Swagger_Abstract_Default;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.List;
import java.util.UUID;

@ApiModel(
        value = "Homer_AuthenticationRequest_HardwareOnlineSubscribe",
        description = ""
)
public class Swagger_Homer_AuthenticationRequest_HardwareOnlineSubscribe extends _Swagger_Abstract_Default {

    @ApiModelProperty(required = true, readOnly = true )
    @Constraints.Required
    public String user_token;

    @ApiModelProperty(required = true, readOnly = true )
    @Constraints.Required
    public List<UUID> hardware_id;
}

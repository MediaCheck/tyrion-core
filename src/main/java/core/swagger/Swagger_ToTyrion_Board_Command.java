package core.swagger;


import core.customer_backends_common.help.Enum_BoardCommand;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(description = "Json Model for developers commands to Hardware. For example restart, redirect etc. Please, use that, only if you know, what you are doing.",
          value = "HardwareCommand")
public class Swagger_ToTyrion_Board_Command extends _Swagger_Abstract_Default {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public UUID hardware_id;

    @ApiModelProperty(required = true, value = "Command")
    public Enum_BoardCommand command;

}

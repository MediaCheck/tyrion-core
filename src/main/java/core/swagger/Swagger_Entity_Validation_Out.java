package core.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Validation of *-some Entity",
         value = "Entity_Validation_Out")
public class Swagger_Entity_Validation_Out extends _Swagger_Abstract_Default {

    @ApiModelProperty(value = "Entity (Email, NickName.. etc) is valid if valid = true", required = true, readOnly = true)
    public boolean valid;

    @ApiModelProperty(value = "If valid = false, Json probably contains message for user", required = false, readOnly = true, example = "Email is used")
    public String message;

}

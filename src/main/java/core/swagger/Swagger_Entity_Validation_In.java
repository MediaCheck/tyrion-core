package core.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Validation of *-some Entity",
        value = "Entity_Validation_In")
public class Swagger_Entity_Validation_In {

    @ApiModelProperty(value = "This field is required", required = true, allowableValues = "email, nick_name, vat_number")
    public String key;

    @ApiModelProperty(value = "This field is required", required = true)
    public String value;
}

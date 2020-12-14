package core.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for Create Account",
        value = "Person_Create")
public class Swagger_Person_Create extends _Swagger_Abstract_Default {

    @Constraints.Required
    public String first_name; // Byzance Hardware ID (not Full ID)

    @Constraints.Required
    public String last_name; // Byzance Hardware ID (not Full ID)

    @Constraints.Required
    @Constraints.Email
    public String email; // Byzance Hardware ID (not Full ID)

    @Constraints.Required
    @Constraints.MinLength(6)
    public String password; // Byzance Hardware ID (not Full ID)

    @ApiModelProperty(required = false)
    public String phone_number;

}

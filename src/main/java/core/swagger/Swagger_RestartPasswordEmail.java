package core.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Constraint;

@ApiModel(description = "Json Model with email",
        value = "RestartPasswordEmail")
public class Swagger_RestartPasswordEmail extends _Swagger_Abstract_Default {

    @Constraints.MinLength(8)
    @Constraints.MaxLength(128)
    @Constraints.Required
    public String new_password;

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Required password_recovery_token")
    public String password_recovery_token;
}

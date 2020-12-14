package core.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model with email",
        value = "ChangeOwnPassword")
public class Swagger_ChangeOwnPassword {

    @Constraints.MinLength(8)
    @Constraints.MaxLength(128)
    @Constraints.Required
    public String new_password;
}

package core.swagger;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model with email",
        value = "EmailRequired")
public class Swagger_EmailRequired {

    @Constraints.Email
    @Constraints.Required
    public String email;
}

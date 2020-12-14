package core.swagger;

import core.common.JsonSerializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(description = "Json Model that you will get, if login was successful",
        value = "Login_Token")
public class Swagger_Login_Token implements JsonSerializable {

    @ApiModelProperty(value = "X-AUTH-TOKEN - used this token in HTML head for verifying the identities", readOnly = true, required = true)
    public UUID auth_token;

}

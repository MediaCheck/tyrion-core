package core.homer.scheduler_service;

import core.swagger._Swagger_Abstract_Default;
import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import java.util.UUID;

@ApiModel(
        value = "BLOCKO_Common",
        description = "HOMER Blocko instance object from Hardware"
)
public class _Swagger_BLOCKO_Response_Common extends _Swagger_Abstract_Default {

    @Constraints.Required
    public String message_action_group_type;

    @Constraints.Required
    public UUID ioda_uuid;

    @Constraints.Required
    public UUID message_uuid;

    public String error_message;

    public Integer error_code;
}

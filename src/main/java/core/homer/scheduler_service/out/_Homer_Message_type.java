package core.homer.scheduler_service.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.homer.comunication_with_homer.HomerHardwareAbstractModel;
import core.swagger._Swagger_Abstract_Default;
import play.data.validation.Constraints;

import java.util.UUID;

public abstract class _Homer_Message_type extends _Swagger_Abstract_Default {

    // ----- Public ----------------------------------------------------------------

    @Constraints.Required
    public UUID message_uuid = UUID.randomUUID();

    @Constraints.Required
    public UUID ioda_uuid;

    // ----- Private ---------------------------------------------------------------

    @JsonIgnore
    private String msg_type;

    // --------- Constructor -------------------------------------------------------

    public _Homer_Message_type(HomerHardwareAbstractModel homer_compatible_hardware, String msg_type) {
        this.ioda_uuid = homer_compatible_hardware.getUUID();
        this.msg_type = msg_type;
    }

    @JsonProperty(value = "message_type")
    public String _abstract_message_type() {
        return msg_type;
    }

    @JsonIgnore
    public UUID getMessageUUID() {
        return message_uuid;
    }
}

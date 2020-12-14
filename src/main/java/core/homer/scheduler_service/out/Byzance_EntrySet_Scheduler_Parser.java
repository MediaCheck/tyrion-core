package core.homer.scheduler_service.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.homer.comunication_with_homer.HomerHardwareAbstractModel;
import core.homer.scheduler_service.enums.Enum_FlagScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Byzance_EntrySet_Scheduler_Parser extends _Homer_Message_type {

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    public Byzance_EntrySet_Scheduler_Parser(HomerHardwareAbstractModel homer_compatible_hardware, List<String> entry_commands, Enum_FlagScheduler flag, String message_type) {
        super(homer_compatible_hardware, message_type);
        this.entry_commands = entry_commands;
        this.flag = flag;
    }

    @JsonIgnore
    public Enum_FlagScheduler flag;

    @JsonIgnore
    public List<String> entry_commands = new ArrayList<>();

    @JsonProperty
    public String entry() {

        StringBuilder builder = new StringBuilder();
        builder.append(flag.flag);
        builder.append(";");

        for(int i = 0, j = (entry_commands.size() - 1); i < entry_commands.size(); i++) {
            builder.append(entry_commands.get(i));
            if(i < j)  builder.append(";");
        }

        return builder.toString();
    }
}

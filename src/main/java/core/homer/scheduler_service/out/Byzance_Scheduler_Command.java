package core.homer.scheduler_service.out;

import core.homer.scheduler_service.enums.Enum_FlagScheduler;

import java.util.ArrayList;
import java.util.List;

public class Byzance_Scheduler_Command {
    public Enum_FlagScheduler flag;
    public List<String> entry_commands = new ArrayList<>();
}

package core.homer.scheduler_service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import core.homer.scheduler_service.out.Byzance_Scheduler_Agregation;
import core.http.form.FormFactory;
import core.mongo._BaseMongoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import core.homer.scheduler_service.enums.Enum_FlagScheduler;
import core.homer.scheduler_service.in.BYZANCE_Scheduler_ArrayInterpreter_TimeAndHexCommand;
import core.homer.scheduler_service.out.Byzance_Scheduler_Command;
import core.homer.scheduler_service.in.BYZANCE_Scheduler_TimeAndHexCommand;

import java.lang.reflect.Array;
import java.util.*;

@Singleton
public class Service_SchedulerGenerator extends _BaseMongoController {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(Service_SchedulerGenerator.class);

    /* VALUE  --------------------------------------------------------------------------------------------------------------*/

    private Config config;
    private Integer entry_array_size;
    private static final Integer INDEX_MAX_SIZE = 12;

    /* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    @Inject
    public Service_SchedulerGenerator(WSClient ws, FormFactory formFactory, Config config) {
        super(ws, formFactory);
        this.config = config;

        if(!this.config.hasPath("byzance_scheduler")) {
            logger.error(
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n" +
                    "In application.conf is missing object byzance_scheduler! Please set it! \n");
        } else {
            this.entry_array_size = this.config.getInt("byzance_scheduler.entry_size");
            logger.trace("constructor: Scheduler config entry_size:{}", this.entry_array_size);
        }
    }


    // https://youtrack.byzance.cz/youtrack/issue/HW-1564
    public Byzance_Scheduler_Agregation create (
            List<BYZANCE_Scheduler_ArrayInterpreter_TimeAndHexCommand> command_list) {

        List<Byzance_Scheduler_Command> commands = new ArrayList<>();

        for(BYZANCE_Scheduler_ArrayInterpreter_TimeAndHexCommand command : command_list) {
            commands.addAll(create_command_structure(command));
        }

        Byzance_Scheduler_Agregation agregation = new Byzance_Scheduler_Agregation();
        agregation.commands = commands;
        agregation.size = count_for_check(commands);

        return agregation;
    }

    /* COMMAND  --------------------------------------------------------------------------------------------------------*/

    private  List<Byzance_Scheduler_Command> create_command_structure(BYZANCE_Scheduler_ArrayInterpreter_TimeAndHexCommand command_request) {


        if(command_request.day_list_repeater.isEmpty() || command_request.day_list_repeater.size() > 7) {
            logger.error("createCommand - List with day is empty or is higher then 7");
            throw new IllegalArgumentException("createCommand - List with day is empty or is higher then 7");
        }

        if(duplicates(command_request.day_list_repeater)) {
            logger.error("createCommand - day_list_repeater contains duplications");
            throw new IllegalArgumentException("createCommand - day_list_repeater contains duplications");
        }


        this.sort(command_request);
        this.duplicates_and_non_change_reduction(command_request);

        // Validations
        for (BYZANCE_Scheduler_TimeAndHexCommand hex_command : command_request.commands) {
            if (hex_command.time_in_second < 0 || hex_command.time_in_second > (60 * 60 * 24)) {
                logger.error("time_in_second - is lower then 0 or higher than day in second");
                throw new IllegalArgumentException("time_in_second - is lower then 0 or higher than day in second");
            }

            if (hex_command.hexa_decimal_command.length() != 8) {
                logger.error("hexa_decimal_command - must be  in format  0x00000000  - 8 'Digits' ");
                throw new IllegalArgumentException("hexa_decimal_command - must be  in format  0x00000000  - 8 'Digits'");
            }
        }



        // Create Command
        List<Byzance_Scheduler_Command> commands = new ArrayList<>();

        for(int i = 0; i < command_request.commands.size(); i += this.entry_array_size) {

            Byzance_Scheduler_Command command = new Byzance_Scheduler_Command();

            for(int j = 0; j < command_request.commands.size() && (i + j) < command_request.commands.size() && j < this.entry_array_size; j++ ) {
                String time_record = make_time_entry_record(command_request.commands.get( i + j ).time_in_second);
                command.entry_commands.add( time_record + "-" + command_request.commands.get( i + j ).hexa_decimal_command);
            }

            try {

                command.flag = make_flag(command_request.day_list_repeater);


                /*
                // máme limitu na velikost příkazu!!!§
                if(command.entry_commands.size() > INDEX_MAX_SIZE) {
                    for (int size_index = 0; i < command.entry_commands.size(); i = i + INDEX_MAX_SIZE) {
                        Byzance_Scheduler_Command parse_command = new Byzance_Scheduler_Command();
                        parse_command.flag =  command.flag;
                        parse_command.entry_commands = command.entry_commands.subList(size_index, ( (size_index + (INDEX_MAX_SIZE-1)) < command.entry_commands.size() )? (size_index + (INDEX_MAX_SIZE-1) ) : (command.entry_commands.size() - 1));
                        commands.add(parse_command);
                    }
                } else {
                    commands.add(command);
                }
                */
                commands.add(command);



                // ITs List of independent days "Enum_FlagScheduler"
            } catch (EnumConstantNotPresentException e) {
                for(Integer day : command_request.day_list_repeater) {
                    Byzance_Scheduler_Command copy = new Byzance_Scheduler_Command();
                    copy.entry_commands = command.entry_commands;
                    copy.flag = make_flag(Collections.singletonList(day));
                    commands.add(copy);
                }
            }

        }

        return commands;
    }


    private void sort(BYZANCE_Scheduler_ArrayInterpreter_TimeAndHexCommand command_request) {

        for (int i = 0; i < command_request.commands.size() - 1; i++) {
            int j = i + 1;
            BYZANCE_Scheduler_TimeAndHexCommand tmp = command_request.commands.get(j);
            while (j > 0 && tmp.time_in_second < command_request.commands.get(j-1).time_in_second) {
                command_request.commands.set(j, command_request.commands.get(j-1));
                j--;
            }
            command_request.commands.set(j,tmp);
        }
    }

    private void duplicates_and_non_change_reduction(BYZANCE_Scheduler_ArrayInterpreter_TimeAndHexCommand command_request) {

        List<BYZANCE_Scheduler_TimeAndHexCommand> commands_after_reduction = new ArrayList<>();

        for(int i = 0; i < command_request.commands.size(); i++) {

            if(i == 0) {
                commands_after_reduction.add(command_request.commands.get(i));
            }

            if(commands_after_reduction.size() > 0 && command_request.commands.get(i).hexa_decimal_command
                    .equals(commands_after_reduction.get(commands_after_reduction.size() - 1).hexa_decimal_command)) {
                // Duplicity
                continue;
            }

            commands_after_reduction.add(command_request.commands.get(i));
        }

        command_request.commands = commands_after_reduction;
    }

    private boolean duplicates (final List<Integer> day_list_repeater) {
        Set<Integer> lump = new HashSet<Integer>();
        for (int i : day_list_repeater)
        {
            if (lump.contains(i)) return true;
            lump.add(i);
        }
        return false;
    }

    private Enum_FlagScheduler make_flag(List<Integer> day_list_repeater) {

        // ITs Individual Day
        if(day_list_repeater.size() == 1) {
            switch (day_list_repeater.get(0)) {
                case 1 : {
                    return Enum_FlagScheduler.MONDAY;
                } case 2 : {
                    return Enum_FlagScheduler.TUESDAY;
                }case 3 : {
                    return Enum_FlagScheduler.WEDNESDAY;
                }case 4 : {
                    return Enum_FlagScheduler.THURSDAY;
                }case 5 : {
                    return Enum_FlagScheduler.FRIDAY;
                }case 6 : {
                    return Enum_FlagScheduler.SATURDAY;
                }case 7 : {
                    return Enum_FlagScheduler.SUNDAY;
                } default: {
                    throw new IllegalArgumentException("Day higher value is 7");
                }
            }
        }

        // Check Weekend
        if(day_list_repeater.size() == 2) {
            if(day_list_repeater.contains(6) && day_list_repeater.contains(7)) {
                return Enum_FlagScheduler.WEEKEND;
            }
        }

        if(day_list_repeater.size() == 7) {
            return Enum_FlagScheduler.EVERYDAY;
        }

        if(day_list_repeater.size() == 5) {
            if(day_list_repeater.contains(1) &&
               day_list_repeater.contains(2) &&
               day_list_repeater.contains(3) &&
               day_list_repeater.contains(4) &&
               day_list_repeater.contains(5)
            ) {
                return Enum_FlagScheduler.WORKING_DAY;
            }
        }

        throw new EnumConstantNotPresentException(Enum_FlagScheduler.class, "");
    }

    private String make_time_entry_record( Integer time_in_second) {

        String hour = ((Integer)(time_in_second / 3600)).toString();
        if(hour.length() == 1) hour = "0" + hour;

        String minutes = ((Integer)((time_in_second - ((time_in_second / 3600) * 3600 )) / 60)).toString();
        if(minutes.length() < 2) minutes = "0" + minutes;
        if(minutes.length() < 2) minutes = "0" + minutes;

        return hour + minutes;

    }

    public String binaryToHex(String binary) {
        int digitNumber = 1;
        int sum = 0;
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < binary.length(); i++){
            if (digitNumber == 1)
                sum+= Integer.parseInt(binary.charAt(i) + "") * 8;
            else if (digitNumber == 2)
                sum+= Integer.parseInt(binary.charAt(i) + "") * 4;
            else if (digitNumber == 3)
                sum+= Integer.parseInt(binary.charAt(i) + "") * 2;
            else if (digitNumber == 4 || i < binary.length() + 1) {

                sum+= Integer.parseInt(binary.charAt(i) + ""); // * 1
                digitNumber = 0;
                if(sum < 10)
                    response.append(sum);
                else if(sum == 10)
                    response.append('A');
                else if(sum == 11)
                    response.append('B');
                else if(sum == 12)
                    response.append("C");
                else if(sum == 13)
                    response.append("D");
                else if(sum == 14)
                    response.append("E");
                else if(sum == 15)
                    response.append("F");
                sum=0;
            }
            digitNumber++;
        }

        return response.toString();
    }

    private int count_for_check(List<Byzance_Scheduler_Command> commands) {

        int size = 0;

        for(int i = 0; i < commands.size(); i++) {
            switch (commands.get(i).flag) {

                case EVERYDAY : {
                    size +=  commands.get(i).entry_commands.size() * 7;
                    break;
                }

                case WEEKEND: {
                    size +=  commands.get(i).entry_commands.size() * 2;
                    break;
                }

                case WORKING_DAY: {
                    size +=  commands.get(i).entry_commands.size() * 5;
                    break;
                }

                default: size += commands.get(i).entry_commands.size();
            }
        }

        return size;
    }

}

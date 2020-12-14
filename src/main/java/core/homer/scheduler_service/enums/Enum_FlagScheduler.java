package core.homer.scheduler_service.enums;

public enum Enum_FlagScheduler {

    MONDAY("0"), //        0
    TUESDAY("1"),
    WEDNESDAY("2"),
    THURSDAY("3"),
    FRIDAY("4"),
    SATURDAY("5"),
    SUNDAY("6"),

    EVERYDAY("E"), // MONDAY - SUNDAY
    WEEKEND("W"),  // SATURDAY - SUNDAY
    WORKING_DAY("D"); // MONDAY - FRIDAY


    public final String flag;

    private Enum_FlagScheduler(String flag) {
        this.flag = flag;
    }

}

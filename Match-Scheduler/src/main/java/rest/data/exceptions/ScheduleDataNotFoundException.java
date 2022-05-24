package rest.data.exceptions;

public class ScheduleDataNotFoundException extends Exception {
    public ScheduleDataNotFoundException() {
        super("Schedule Data not found by the given id.");
    }
}

package unimag.proyect.mappers;

import java.time.DayOfWeek;

import unimag.proyect.enums.WeekDay;

public final class WeekDayMapper {
    private WeekDayMapper() {}

    public static WeekDay from(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY    -> WeekDay.MONDAY;
            case TUESDAY   -> WeekDay.TUESDAY;
            case WEDNESDAY -> WeekDay.WEDNESDAY;
            case THURSDAY  -> WeekDay.THURSDAY;
            case FRIDAY    -> WeekDay.FRIDAY;
            case SATURDAY  -> WeekDay.SATURDAY;
            case SUNDAY    -> WeekDay.SUNDAY;
        };
    }
}

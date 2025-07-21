import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class referenceTime {
    final static Pattern tdy = Pattern.compile("(?i)\\btdy\\b");
    final static Pattern yday = Pattern.compile("(?i)\\byday\\b");
    final static Pattern tmrw = Pattern.compile("(?i)\\btmrw\\b");
    final static Pattern _2mrw = Pattern.compile("(?i)\\b2mrw\\b");
    final static Pattern Ago = Pattern.compile("(?i)(\\d+|a) \\b(day|month|year|week|hour|hr)(s)? ago\\b");
    final static Pattern IN = Pattern.compile("(?i)in (\\d+|a) \\b(day|month|year|week|hour|hr)(s)?\\b");
    final static Pattern InRange = Pattern
            .compile(
                    "(?i)\\b(last|next) (\\d+ )?(day|month|year|week|hour|hr|weekend|monday|tuesday|wednesday|thurday|friday|saturday|sunday)(s)?\\b");
    final static Pattern _this = Pattern.compile(
            "(?i)\\bthis (month|year|week|hour|hr|weekend|monday|tuesday|wednesday|thurday|friday|saturday|sunday)\\b");

    // check the format to be shown by these in the output wrt the format supported
    // in the DateTimeQueryPraser
    public static String simplify(String query) {
        LocalDateTime refTime = LocalDateTime.now();
        LocalDate refDate = refTime.toLocalDate();
        search2.what a;
        Matcher match;

        if ((a = search2.DL_light(query.toLowerCase(), "today")).match) {
            query = query.replaceAll(a.replacement, refDate.toString());
        } else if ((match = tdy.matcher(query)).find(0)) {
            query = query.replaceAll(match.group(), refDate.toString());
        }
        if ((a = search2.DL_light(query.toLowerCase(), "yesterday")).match) {
            query = query.replaceAll(a.replacement, refDate.minusDays(1).toString());
        } else if ((match = yday.matcher(query)).find(0)) {
            query = query.replaceAll(match.group(), refDate.minusDays(1).toString());
        }
        if ((a = search2.DL_light(query.toLowerCase(), "tomorrow")).match) {
            query = query.replaceAll(a.replacement, refDate.plusDays(1).toString());
        } else if (((match = tmrw.matcher(query)).find(0)) || ((match = _2mrw.matcher(query)).find(0))) {
            query = query.replaceAll(match.group(), refDate.plusDays(1).toString());
        }
        if ((match = Ago.matcher(query)).find(0)) {
            int num = 0;
            if (match.group(1).equalsIgnoreCase("a")) {
                num = 1;
            } else {
                num = Integer.parseInt(match.group(1));
            }
            switch (match.group(2).toLowerCase()) {
                case "day":
                    query = query.replaceAll(match.group(),
                            refDate.minusDays(num).toString());
                    break;
                case "month":
                    query = query.replaceAll(match.group(),
                            refDate.getMonth().minus(num).toString());
                    break;
                case "year":
                    query = query.replaceAll(match.group(),
                            Integer.toString((refDate.getYear()) - (num)));
                    break;
                case "week":
                    LocalDate exact_week_back = refDate.minusWeeks(num);
                    LocalDate startOfweek = exact_week_back
                            .minusDays(
                                    (exact_week_back.getDayOfWeek().getValue() - (settings.isWeekStartSun() ? 7 : 1)
                                            + 7) % 7);
                    LocalDate endOfweek = startOfweek.plusDays(6);
                    query = query.replaceAll(match.group(),
                            "between " + startOfweek.toString() + " and " + endOfweek.toString());
                    break;
                case "hour", "hr":
                    LocalDateTime exact_hr_back = refTime.minusHours(num);
                    int hr = exact_hr_back.getHour();
                    if (exact_hr_back.getMinute() > 30) {
                        hr += 1;
                    }
                    query = query.replaceAll(match.group(), exact_hr_back.toLocalDate().toString() + " at "
                            + Integer.toString(hr));
                    break;
                default:
                    break;
            }
        }
        if ((match = _this.matcher(query)).find(0)) {
            switch (match.group(1).toLowerCase()) {
                case "day":
                    query = query.replaceAll(match.group(),
                            refDate.toString());
                    break;
                case "month":
                    query = query.replaceAll(match.group(),
                            refDate.getMonth().toString());
                    break;
                case "year":
                    query = query.replaceAll(match.group(),
                            Integer.toString((refDate.getYear())));
                    break;
                case "week":
                    LocalDate startOfweek = refDate
                            .minusDays(
                                    (refDate.getDayOfWeek().getValue() - (settings.isWeekStartSun() ? 7 : 1)
                                            + 7) % 7);
                    LocalDate endOfweek = startOfweek.plusDays(6);
                    query = query.replaceAll(match.group(),
                            "between " + startOfweek.toString() + " and " + endOfweek.toString());
                    break;
                case "hour", "hr":
                    query = query.replaceAll(match.group(), refDate.toString() + " at "
                            + Integer.toString(refTime.getHour()));
                    break;
                case "weekend", "weekends": {
                    LocalDate sat = GetDayInThatWeek(refDate, DayOfWeek.SATURDAY);
                    LocalDate sun = GetDayInThatWeek(refDate, DayOfWeek.SUNDAY);
                    query = query.replaceAll(match.group(), sat + " and " + sun);
                    break;
                }
                case "monday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.MONDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                case "tuesday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.TUESDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                case "wednesday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.SUNDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                case "thursday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.THURSDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                case "friday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.FRIDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                case "saturday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.SATURDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                case "sunday": {

                    LocalDate day = GetDayInThatWeek(refDate, DayOfWeek.SUNDAY);
                    query = query.replaceAll(match.group(), day.toString());
                    break;
                }
                default:
                    break;
            }
        }
        if ((match = InRange.matcher(query)).find(0)) {
            int num;
            if (match.group(2) == null) {
                num = 1;
            } else {
                num = Integer.parseInt(match.group(2).strip());
            }
            switch (match.group(1).toLowerCase()) {
                case "last":
                    switch (match.group(3).toLowerCase()) {
                        case "day":
                            query = query.replaceAll(match.group(), "between " + refDate + " and " +
                                    refDate.minusDays(num).toString());
                            break;
                        case "month":
                            query = query.replaceAll(match.group(),
                                    "between " + refDate.getMonth().toString() + " and " +
                                            refDate.getMonth().minus(num).toString());
                            break;
                        case "year":
                            query = query.replaceAll(match.group(),
                                    "between " + Integer.toString(refDate.getYear()) + " and " +
                                            Integer.toString((refDate.getYear()) - (num)));
                            break;
                        case "week":
                            LocalDate exact_week_back = refDate.minusWeeks(num);
                            LocalDate startOfweek = exact_week_back
                                    .minusDays(
                                            (exact_week_back.getDayOfWeek().getValue()
                                                    - (settings.isWeekStartSun() ? 7 : 1)
                                                    + 7) % 7);
                            query = query.replaceAll(match.group(),
                                    "between " + startOfweek.toString() + " and " + refDate.toString());
                            break;
                        case "hour", "hr":
                            LocalDateTime exact_hr_back = refTime.minusHours(num);
                            int hr = exact_hr_back.getHour();
                            if (exact_hr_back.getMinute() > 30) {
                                hr += 1;
                            }
                            query = query.replaceAll(match.group(),
                                    "between " + exact_hr_back.toLocalDate().toString() + " at "
                                            + Integer.toString(hr) + " and " + refDate.toString() + " at "
                                            + refTime.getHour());
                            break;
                        case "weekend": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate sat = GetDayInThatWeek(date, DayOfWeek.SATURDAY);
                            LocalDate sun = GetDayInThatWeek(date, DayOfWeek.SUNDAY);
                            query = query.replaceAll(match.group(), sat + " and " + sun);
                            break;
                        }
                        case "monday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.MONDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "tuesday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.TUESDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "wednesday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.SUNDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "thursday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.THURSDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "friday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.FRIDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "saturday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.SATURDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "sunday": {
                            LocalDate date = refDate.minusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.SUNDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                case "next":
                    switch (match.group(3).toLowerCase()) {
                        case "day":
                            query = query.replaceAll(match.group(), "between " + refDate + " and " +
                                    refDate.plusDays(num).toString());
                            break;
                        case "month":
                            query = query.replaceAll(match.group(),
                                    "between " + refDate.getMonth().toString() + " and " +
                                            refDate.getMonth().plus(num).toString());
                            break;
                        case "year":
                            query = query.replaceAll(match.group(),
                                    "between " + refDate.getYear() + " and " +
                                            Integer.toString((refDate.getYear()) + (num)));
                            break;
                        case "week":
                            LocalDate exact_week_back = refDate.minusWeeks(num);
                            LocalDate endOfweek = (exact_week_back
                                    .minusDays(
                                            (exact_week_back.getDayOfWeek().getValue()
                                                    - (settings.isWeekStartSun() ? 7 : 1)
                                                    + 7) % 7))
                                    .plusDays(6);
                            query = query.replaceAll(match.group(),
                                    "between " + endOfweek.toString() + " and " + refDate.toString());
                            break;
                        case "hour", "hr":
                            LocalDateTime exact_hr_later = refTime.plusHours(num);
                            int hr = exact_hr_later.getHour();
                            query = query.replaceAll(match.group(),
                                    "between " + exact_hr_later.toLocalDate().toString() + " at "
                                            + Integer.toString(hr) + " and " + refDate.toString() + " at "
                                            + refTime.getHour());
                            break;
                        case "weekend": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate sat = GetDayInThatWeek(date, DayOfWeek.SATURDAY);
                            LocalDate sun = GetDayInThatWeek(date, DayOfWeek.SUNDAY);
                            query = query.replaceAll(match.group(), sat + " and " + sun);
                            break;
                        }
                        case "monday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.MONDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "tuesday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.TUESDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "wednesday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.SUNDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "thursday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.THURSDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "friday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.FRIDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "saturday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.SATURDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        case "sunday": {
                            LocalDate date = refDate.plusWeeks(1);
                            LocalDate day = GetDayInThatWeek(date, DayOfWeek.SUNDAY);
                            query = query.replaceAll(match.group(), day.toString());
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        if ((match = IN.matcher(query)).find(0)) {
            int num = 0;
            if (match.group(1).equalsIgnoreCase("a")) {
                num = 1;
            } else {
                num = Integer.parseInt(match.group(1).strip());
            }
            switch (match.group(2).toLowerCase()) {
                case "day":
                    query = query.replaceAll(match.group(),
                            refDate.plusDays(num).toString());
                    break;
                case "month":
                    query = query.replaceAll(match.group(),
                            refDate.getMonth().plus(num).toString());
                    break;
                case "year":
                    query = query.replaceAll(match.group(),
                            Integer.toString((refDate.getYear()) + (num)));
                    break;
                case "week":
                    LocalDate exact_week_later = refDate.plusWeeks(num);
                    LocalDate startOfweek = exact_week_later
                            .minusDays(
                                    (exact_week_later.getDayOfWeek().getValue() - (settings.isWeekStartSun() ? 7 : 1)
                                            + 7) % 7);
                    LocalDate endOfweek = startOfweek.plusDays(6);
                    query = query.replaceAll(match.group(),
                            "between " + startOfweek.toString() + " and " + endOfweek.toString());
                    break;
                case "hour", "hr":
                    LocalDateTime exact_hr_later = refTime.plusHours(num);
                    int hr = exact_hr_later.getHour();
                    query = query.replaceAll(match.group(), exact_hr_later.toLocalDate().toString() + " at "
                            + Integer.toString(hr));
                    break;
                default:
                    break;
            }
        }
        return query;
    }

    private static LocalDate GetDayInThatWeek(LocalDate refDate, DayOfWeek day) {
        int refDay = refDate.getDayOfWeek().getValue();
        int daynum = day.getValue();
        if (!settings.defaultweekStartSun) {
            if (daynum <= refDay) {
                return refDate.with(TemporalAdjusters.previousOrSame(day));
            } else {
                return refDate.with(TemporalAdjusters.next(day));
            }
        } else {
            if (daynum == 7) {
                return refDate.with(TemporalAdjusters.previousOrSame(day));
            }
            if (refDay == 7) {
                return refDate.with(TemporalAdjusters.nextOrSame(day));
            }
            if (daynum <= refDay) {
                return refDate.with(TemporalAdjusters.previousOrSame(day));
            } else {
                return refDate.with(TemporalAdjusters.next(day));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(simplify("next weekends"));
    }
}

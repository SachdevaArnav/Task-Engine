import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// the range thing is still pending
public class DateTimeQueryPraser {
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
    private static final Pattern SHORT_TEXT_MONTH_YEAR_PATTERN = Pattern
            .compile("(?i)\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{4}");
    private static final Pattern LONG_TEXT_MONTH_YEAR_PATTERN = Pattern.compile(
            "(?i)\\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{4}");
    private static final Pattern SHORT_TEXT_MONTH_YEAR_2DIGIT_PATTERN = Pattern.compile(
            "(?i)\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{2}",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LONG_TEXT_MONTH_YEAR_2DIGIT_PATTERN = Pattern.compile(
            "(?i)\\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2}");
    private static final Pattern SHORT_TEXT_DATE_PATTERN = Pattern
            .compile(
                    "(?i)\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\d{4}");
    private static final Pattern LONG_TEXT_DATE_PATTERN = Pattern.compile(
            "(?i)\\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\b(3[01]|[12][0-9]|0?[1-9])\\b \\d{4}");
    private static final Pattern SHORT_TEXT_DATE_REVERSE_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{4}");
    private static final Pattern LONG_TEXT_DATE_REVERSE_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{4}");
    private static final Pattern SHORT_TEXT_DATE_REVERSE_2DIGIT_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{2}");
    private static final Pattern LONG_TEXT_DATE_REVERSE_2DIGIT_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2}");
    private static final Pattern LONG_TEXT_DATE_at_HOUR_CLOCK_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} at \\b(2[0-3]|1[0-9]|0?[1-9])\\b");
    private static final Pattern LONG_TEXT_DATE_at_12_HOUR_CLOCK_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} at \\b(1[0-2]|0?[1-9])\\b \\b(a\\.?m\\.?|p\\.?m\\.?)\\b");

    private static final Pattern LONG_TEXT_DATE_HOUR_CLOCK_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} \\b(2[0-3]|1[0-9]|0?[1-9])\\b o'clock");

    private static final Pattern LONG_TEXT_DATE_12_HOUR_CLOCK_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} \\b(1[0-2]|0?[1-9])\\b \\b(a\\.?m\\.?|p\\.?m\\.?)\\b o'clock");
    private static final Pattern LONG_TEXT_DATE_TIME_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b (january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} \\b(2[0-3]|1[0-9]|0?[1-9]):([1-5][0-9]|0?[1-9])\\b");
    private static final Pattern LONG_TEXT_DATE_TIME_12_PATTERN = Pattern
            .compile(
                    "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} \\b(1[0-2]|0?[1-9]):([1-5][0-9]|0?[1-9])\\b \\b(a\\.?m\\.?|p\\.?m\\.?)\\b");
    private static final Pattern NUMERIC_DATE_2D = Pattern
            .compile("\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(1[0-2]|0?[1-9])\\b \\d{2}");
    private static final Pattern NUMERIC_DATE = Pattern
            .compile("\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(1[0-2]|0?[1-9])\\b \\d{4}");
    private static final Pattern DAY_MONTH_PATTERN = Pattern.compile(
            "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b");
    private static final Pattern DAY_MONTH_SHORT_PATTERN = Pattern.compile(
            "(?i)\\b(3[01]|[12][0-9]|0?[1-9])\\b \\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b");
    private static final Pattern ONLY_MONTH_PATTERN = Pattern.compile(
            "(?i)\\b(january|february|march|april|may|june|july|august|september|october|november|december)\\b");
    private static final Pattern ONLY_MONTH_SHORT_PATTERN = Pattern.compile(
            "(?i)\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b");

    public static class ParsedDateTime {
        public final LocalDateTime start;
        public final LocalDateTime end;
        public final ChronoUnit grain;

        // This class constructor is to make objects which can have range of
        // datetimes along with their grains(like they r upto hrs,months,days)
        public ParsedDateTime(LocalDateTime start, LocalDateTime end, ChronoUnit grain) {
            this.grain = grain;
            this.end = end;
            this.start = start;
        }
    }

    static String cleaninput = "";

    // all forms of date time where its yy in starting is missing (and its the
    // standard date time of java)
    public static ParsedDateTime parse(String input) {
        ParsedDateTime[] datetimes = new ParsedDateTime[2];
        Matcher longTextDateTimeMatcher = LONG_TEXT_DATE_TIME_PATTERN.matcher(input);
        Matcher longTextDateTime12Matcher = LONG_TEXT_DATE_TIME_12_PATTERN.matcher(input);
        Matcher longTextDateHourClockMatcherat = LONG_TEXT_DATE_at_HOUR_CLOCK_PATTERN.matcher(input);
        Matcher longTextDate12HourClockMatcherat = LONG_TEXT_DATE_at_12_HOUR_CLOCK_PATTERN.matcher(input);
        Matcher longTextDateHourClockMatcher = LONG_TEXT_DATE_HOUR_CLOCK_PATTERN.matcher(input);
        Matcher longTextDate12HourClockMatcher = LONG_TEXT_DATE_12_HOUR_CLOCK_PATTERN.matcher(input);
        Matcher longTextDateReverse2DigitMatcher = LONG_TEXT_DATE_REVERSE_2DIGIT_PATTERN.matcher(input);
        Matcher shortTextDateReverse2DigitMatcher = SHORT_TEXT_DATE_REVERSE_2DIGIT_PATTERN.matcher(input);
        Matcher longTextDateReverseMatcher = LONG_TEXT_DATE_REVERSE_PATTERN.matcher(input);
        Matcher shortTextDateReverseMatcher = SHORT_TEXT_DATE_REVERSE_PATTERN.matcher(input);
        Matcher longTextDateMatcher = LONG_TEXT_DATE_PATTERN.matcher(input);
        Matcher shortTextDateMatcher = SHORT_TEXT_DATE_PATTERN.matcher(input);
        Matcher longTextMonthYear2DigitMatcher = LONG_TEXT_MONTH_YEAR_2DIGIT_PATTERN.matcher(input);
        Matcher shortTextMonthYear2DigitMatcher = SHORT_TEXT_MONTH_YEAR_2DIGIT_PATTERN.matcher(input);
        Matcher longTextMonthYearMatcher = LONG_TEXT_MONTH_YEAR_PATTERN.matcher(input);
        Matcher shortTextMonthYearMatcher = SHORT_TEXT_MONTH_YEAR_PATTERN.matcher(input);
        Matcher numericDateMatcher = NUMERIC_DATE.matcher(input);
        Matcher numericDate2DMatcher = NUMERIC_DATE_2D.matcher(input);
        Matcher dayMonthMatcher = DAY_MONTH_PATTERN.matcher(input);
        Matcher dayMonthShortMatcher = DAY_MONTH_SHORT_PATTERN.matcher(input);
        Matcher onlyMonthMatcher = ONLY_MONTH_PATTERN.matcher(input);
        Matcher onlyShortMonthMatcher = ONLY_MONTH_SHORT_PATTERN.matcher(input);
        Matcher yearMatcher = YEAR_PATTERN.matcher(input);
        int i = 0;
        while (longTextDateTimeMatcher.find(0) ||
                longTextDateTime12Matcher.find(0) ||
                longTextDateHourClockMatcherat.find(0) ||
                longTextDate12HourClockMatcherat.find(0) ||
                longTextDateHourClockMatcher.find(0) ||
                longTextDate12HourClockMatcher.find(0) ||
                longTextDateReverse2DigitMatcher.find(0) ||
                shortTextDateReverse2DigitMatcher.find(0) ||
                longTextDateReverseMatcher.find(0) ||
                shortTextDateReverseMatcher.find(0) ||
                longTextDateMatcher.find(0) ||
                shortTextDateMatcher.find(0) ||
                longTextMonthYear2DigitMatcher.find(0) ||
                shortTextMonthYear2DigitMatcher.find(0) ||
                longTextMonthYearMatcher.find(0) ||
                shortTextMonthYearMatcher.find(0) ||
                numericDateMatcher.find(0) ||
                numericDate2DMatcher.find(0) ||
                dayMonthMatcher.find(0) ||
                yearMatcher.find(0) ||
                onlyMonthMatcher.find(0) ||
                onlyShortMonthMatcher.find(0) ||
                dayMonthShortMatcher.find(0)) {
            String replacement;
            if (i < 2) {
                try {
                    if (longTextDateTimeMatcher.find(0)) {
                        replacement = longTextDateTimeMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy HH:mm")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.HOURS);
                    } else if (longTextDateTime12Matcher.find(0)) {
                        replacement = longTextDateTime12Matcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy h:mm a")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.HOURS);
                    } else if (longTextDateHourClockMatcherat.find(0)) {
                        replacement = longTextDateHourClockMatcherat.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy at HH")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.HOURS);
                    } else if (longTextDate12HourClockMatcherat.find(0)) {
                        replacement = longTextDate12HourClockMatcherat.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy at h a")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.HOURS);
                    } else if (longTextDateHourClockMatcher.find(0)) {
                        replacement = longTextDateHourClockMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy HH 'o''clock'")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.HOURS);
                    } else if (longTextDate12HourClockMatcher.find(0)) {
                        replacement = longTextDate12HourClockMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy h a 'o''clock'")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.HOURS);
                    } else if (longTextDateReverse2DigitMatcher.find(0)) {
                        replacement = longTextDateReverse2DigitMatcher.group();
                        LocalDate date = LocalDate.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (shortTextDateReverse2DigitMatcher.find(0)) {
                        replacement = shortTextDateReverse2DigitMatcher.group();
                        LocalDate date = LocalDate.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMM yy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (longTextDateReverseMatcher.find(0)) {
                        replacement = longTextDateReverseMatcher.group();
                        LocalDate date = LocalDate.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMMM yyyy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (shortTextDateReverseMatcher.find(0)) {
                        replacement = shortTextDateReverseMatcher.group();
                        LocalDate date = LocalDate.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MMM yyyy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (longTextDateMatcher.find(0)) {
                        replacement = longTextDateMatcher.group();
                        LocalDate date = LocalDate.parse(replacement, new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMMM dd yyyy")
                                .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (shortTextDateMatcher.find(0)) {
                        replacement = shortTextDateMatcher.group();
                        LocalDate date = LocalDate.parse(replacement, new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMM dd yyyy")
                                .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (longTextMonthYearMatcher.find(0)) {
                        replacement = longTextMonthYearMatcher.group();
                        YearMonth monthyear = YearMonth.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("MMMM yyyy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
                    } else if (shortTextMonthYearMatcher.find(0)) {
                        replacement = shortTextMonthYearMatcher.group();
                        YearMonth monthyear = YearMonth.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("MMM yyyy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
                    } else if (longTextMonthYear2DigitMatcher.find(0)) {
                        replacement = longTextMonthYear2DigitMatcher.group();
                        YearMonth monthyear = YearMonth.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("MMMM yy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
                    } else if (shortTextMonthYear2DigitMatcher.find(0)) {
                        replacement = shortTextMonthYear2DigitMatcher.group();
                        YearMonth monthyear = YearMonth.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("MMM yy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
                    } else if (numericDateMatcher.find(0)) {
                        replacement = numericDateMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MM yyyy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.DAYS);
                    } else if (numericDate2DMatcher.find(0)) {
                        replacement = numericDate2DMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement,
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .appendPattern("dd MM yy")
                                        .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.DAYS);
                    } else if (dayMonthMatcher.find(0)) {
                        replacement = dayMonthMatcher.group();
                        String[] parts = replacement.split(" ");
                        int day = Integer.parseInt(parts[0]);
                        DateTimeFormatter MONTH_NAME_PARSER = new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMMM")
                                .toFormatter(Locale.ENGLISH);
                        Month month = Month.from(MONTH_NAME_PARSER.parse(parts[1]));
                        LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (dayMonthShortMatcher.find(0)) {
                        replacement = dayMonthShortMatcher.group();

                        String[] parts = replacement.split(" ");
                        int day = Integer.parseInt(parts[0]);
                        DateTimeFormatter MONTH_NAME_PARSER = new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMM")
                                .toFormatter(Locale.ENGLISH);
                        Month month = Month.from(MONTH_NAME_PARSER.parse(parts[1]));
                        LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
                        datetimes[i] = new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
                    } else if (onlyMonthMatcher.find(0)) {
                        replacement = onlyMonthMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement, new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMMM")
                                .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.MONTHS);
                    } else if (onlyShortMonthMatcher.find(0)) {
                        replacement = onlyShortMonthMatcher.group();
                        LocalDateTime date = LocalDateTime.parse(replacement, new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMM")
                                .toFormatter(Locale.ENGLISH));
                        datetimes[i] = new ParsedDateTime(date, null, ChronoUnit.MONTHS);
                    } else if (yearMatcher.find(0)) {
                        replacement = yearMatcher.group();
                        int yr = Integer.parseInt(replacement);
                        datetimes[i] = new ParsedDateTime(LocalDateTime.of(yr, 1, 1, 0, 0), null, ChronoUnit.YEARS);
                    } else {
                        datetimes[i] = null;
                        break;
                    }
                } catch (Exception e) {
                    datetimes[i] = null;
                    break;
                }
                i++;
                input = input.replaceFirst(replacement, "").replaceAll(" +", " ");
                longTextDateTimeMatcher = LONG_TEXT_DATE_TIME_PATTERN.matcher(input);
                longTextDateTime12Matcher = LONG_TEXT_DATE_TIME_12_PATTERN.matcher(input);
                longTextDateHourClockMatcherat = LONG_TEXT_DATE_at_HOUR_CLOCK_PATTERN.matcher(input);
                longTextDate12HourClockMatcherat = LONG_TEXT_DATE_at_12_HOUR_CLOCK_PATTERN.matcher(input);
                longTextDateHourClockMatcher = LONG_TEXT_DATE_HOUR_CLOCK_PATTERN.matcher(input);
                longTextDate12HourClockMatcher = LONG_TEXT_DATE_12_HOUR_CLOCK_PATTERN.matcher(input);
                longTextDateReverse2DigitMatcher = LONG_TEXT_DATE_REVERSE_2DIGIT_PATTERN.matcher(input);
                shortTextDateReverse2DigitMatcher = SHORT_TEXT_DATE_REVERSE_2DIGIT_PATTERN.matcher(input);
                longTextDateReverseMatcher = LONG_TEXT_DATE_REVERSE_PATTERN.matcher(input);
                shortTextDateReverseMatcher = SHORT_TEXT_DATE_REVERSE_PATTERN.matcher(input);
                longTextDateMatcher = LONG_TEXT_DATE_PATTERN.matcher(input);
                shortTextDateMatcher = SHORT_TEXT_DATE_PATTERN.matcher(input);
                longTextMonthYear2DigitMatcher = LONG_TEXT_MONTH_YEAR_2DIGIT_PATTERN.matcher(input);
                shortTextMonthYear2DigitMatcher = SHORT_TEXT_MONTH_YEAR_2DIGIT_PATTERN.matcher(input);
                longTextMonthYearMatcher = LONG_TEXT_MONTH_YEAR_PATTERN.matcher(input);
                shortTextMonthYearMatcher = SHORT_TEXT_MONTH_YEAR_PATTERN.matcher(input);
                numericDateMatcher = NUMERIC_DATE.matcher(input);
                numericDate2DMatcher = NUMERIC_DATE_2D.matcher(input);
                dayMonthMatcher = DAY_MONTH_PATTERN.matcher(input);
                dayMonthShortMatcher = DAY_MONTH_SHORT_PATTERN.matcher(input);
                onlyMonthMatcher = ONLY_MONTH_PATTERN.matcher(input);
                onlyShortMonthMatcher = ONLY_MONTH_SHORT_PATTERN.matcher(input);
                yearMatcher = YEAR_PATTERN.matcher(input);
            } else {
                break;
            }
        }
        cleaninput = input;
        if (datetimes[1] != null) {
            ParsedDateTime start;
            ParsedDateTime end;
            ChronoUnit lowerC = getLowerChronoUnit(datetimes[0].grain, datetimes[1].grain);
            if (search2.truncate(datetimes[0].start, datetimes[0].grain)
                    .isBefore(search2.truncate(datetimes[1].start, datetimes[1].grain))) {
                start = datetimes[0];
                end = datetimes[1];
            } else {
                start = datetimes[1];
                end = datetimes[0];
            }
            if (lowerC.equals(end.grain)) {
                return new ParsedDateTime(start.start, end.start, lowerC);
            } else {
                return new ParsedDateTime(start.start, getAtEndOfChronoUnit(end.start, end.grain, lowerC), lowerC);
            }
        } else {
            return datetimes[0];
        }
    }

    public static String getCleanInput() {
        return cleaninput;
    }

    public static ChronoUnit getLowerChronoUnit(ChronoUnit c1, ChronoUnit c2) {
        byte numC1 = getChronoNum(c1);
        byte numC2 = getChronoNum(c2);
        if (numC1 >= numC2) {
            return c2;
        } else
            return c1;
    }

    public static byte getChronoNum(ChronoUnit c) {
        switch (c) {
            case YEARS:
                return 4;
            case MONTHS:
                return 3;
            case DAYS:
                return 2;
            case HOURS:
                return 1;
            case MINUTES:
                return 0;
            default:
                return 0;
        }
    }

    public static LocalDateTime getAtEndOfChronoUnit(LocalDateTime base, ChronoUnit unit, ChronoUnit precision) {
        switch (unit) {
            case YEARS:
                base = base.withMonth(12).withDayOfMonth(31).withHour(23).withMinute(59).withSecond(59)
                        .withNano(999_999_999);
                break;
            case MONTHS:
                base = base.withDayOfMonth(base.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59)
                        .withSecond(59).withNano(999_999_999);
                break;
            case DAYS:
                base = base.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
                break;
            default:
                break;
        }
        return search2.truncate(base, precision);
    }

}

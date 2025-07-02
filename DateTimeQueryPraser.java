import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateTimeQueryPraser {
    private static final Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern SHORT_TEXT_MONTH_YEAR_PATTERN = Pattern.compile("(?i)^[a-z]{3} \\d{4}$");
    private static final Pattern LONG_TEXT_MONTH_YEAR_PATTERN = Pattern.compile("(?i)^[a-z]+ \\d{4}$");
    private static final Pattern SHORT_TEXT_MONTH_YEAR_2DIGIT_PATTERN = Pattern.compile("(?i)^[a-z]{3} \\d{2}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LONG_TEXT_MONTH_YEAR_2DIGIT_PATTERN = Pattern.compile("(?i)^[a-z]+ \\d{2}$");
    private static final Pattern SHORT_TEXT_DATE_PATTERN = Pattern.compile("(?i)^[a-z]{3} \\d{1,2} \\d{4}$");
    private static final Pattern LONG_TEXT_DATE_PATTERN = Pattern.compile("(?i)^[a-z]+ \\d{1,2} \\d{4}$");
    private static final Pattern SHORT_TEXT_DATE_REVERSE_PATTERN = Pattern.compile("^\\d{1,2} (?i)[a-z]{3} \\d{4}$");
    private static final Pattern LONG_TEXT_DATE_REVERSE_PATTERN = Pattern.compile("^\\d{1,2} (?i)[a-z]+ \\d{4}$");
    private static final Pattern SHORT_TEXT_DATE_REVERSE_2DIGIT_PATTERN = Pattern
            .compile("^\\d{1,2} (?i)[a-z]{3} \\d{2}$");
    private static final Pattern LONG_TEXT_DATE_REVERSE_2DIGIT_PATTERN = Pattern
            .compile("^\\d{1,2} (?i)[a-z]+ \\d{2}$");
    private static final Pattern LONG_TEXT_DATE_HOUR_CLOCK_PATTERN = Pattern
            .compile("^\\d{1,2} (?i)[a-z]+ \\d{2} \\d{1,2} o'clock$");
    private static final Pattern LONG_TEXT_DATE_TIME_PATTERN = Pattern
            .compile("^\\d{1,2} (?i)[a-z]+ \\d{2} \\d{1,2}:\\d{1,2}$");
    private static final Pattern NUMERIC_DATE_2D = Pattern.compile("^\\d{1,2} \\d{1,2} \\d{2}$");
    private static final Pattern NUMERIC_DATE = Pattern.compile("^\\d{1,2} \\d{1,2} \\d{4}$");
    private static final Pattern DAY_MONTH_PATTERN = Pattern.compile("^\\d{1,2} [a-z]+$");

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

        public boolean isRange() {
            return end != null;
        }
    }

    public static ParsedDateTime parse(String input) {
        if (YEAR_PATTERN.matcher(input).matches()) {
            int yr = Integer.parseInt(input);
            return new ParsedDateTime(LocalDateTime.of(yr, 1, 1, 0, 0), null, ChronoUnit.YEARS);
        } else if (SHORT_TEXT_MONTH_YEAR_PATTERN.matcher(input).matches()) {
            YearMonth monthyear = YearMonth.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (LONG_TEXT_MONTH_YEAR_PATTERN.matcher(input).matches()) {
            YearMonth monthyear = YearMonth.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMMM yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (SHORT_TEXT_MONTH_YEAR_2DIGIT_PATTERN.matcher(input).matches()) {
            YearMonth monthyear = YearMonth.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM yy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (LONG_TEXT_MONTH_YEAR_2DIGIT_PATTERN.matcher(input).matches()) {
            YearMonth monthyear = YearMonth.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMMM yy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (SHORT_TEXT_DATE_PATTERN.matcher(input).matches()) {
            LocalDate date = LocalDate.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (LONG_TEXT_DATE_PATTERN.matcher(input).matches()) {
            LocalDate date = LocalDate.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMMM dd yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (SHORT_TEXT_DATE_REVERSE_PATTERN.matcher(input).matches()) {
            LocalDate date = LocalDate.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd MMM yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (LONG_TEXT_DATE_REVERSE_PATTERN.matcher(input).matches()) {
            LocalDate date = LocalDate.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd MMMM yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (SHORT_TEXT_DATE_REVERSE_2DIGIT_PATTERN.matcher(input).matches()) {
            LocalDate date = LocalDate.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd MMM yy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (LONG_TEXT_DATE_REVERSE_2DIGIT_PATTERN.matcher(input).matches()) {
            LocalDate date = LocalDate.parse(input, new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd MMMM yy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (LONG_TEXT_DATE_HOUR_CLOCK_PATTERN.matcher(input).matches()) {
            LocalDateTime date = LocalDateTime.parse(input,
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yy HH o'clock")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.HOURS);
        } else if (LONG_TEXT_DATE_TIME_PATTERN.matcher(input).matches()) {
            LocalDateTime date = LocalDateTime.parse(input,
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yy HH:mm")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.HOURS);
        } else if (NUMERIC_DATE_2D.matcher(input).matches()) {
            LocalDateTime date = LocalDateTime.parse(input,
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MM yy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.DAYS);
        } else if (NUMERIC_DATE.matcher(input).matches()) {
            LocalDateTime date = LocalDateTime.parse(input,
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MM yyyy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.DAYS);
        } else if (DAY_MONTH_PATTERN.matcher(input).matches()) {
            String[] parts = input.split(" ");
            int day = Integer.parseInt(parts[0]);
            DateTimeFormatter MONTH_NAME_PARSER = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM") // good enough for both short and full names
                    .toFormatter(Locale.ENGLISH);
            Month month = Month.from(MONTH_NAME_PARSER.parse(parts[1]));
            LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        }
        throw new IllegalArgumentException("Unsupported or unrecognized datetime format: " + input);
    }

}

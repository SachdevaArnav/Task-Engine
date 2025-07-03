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
            .compile("(?i)\\b^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{4}");
    private static final Pattern LONG_TEXT_MONTH_YEAR_PATTERN = Pattern.compile(
            "(?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{4}");
    private static final Pattern SHORT_TEXT_MONTH_YEAR_2DIGIT_PATTERN = Pattern.compile(
            "(?i)\\b^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{2}",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LONG_TEXT_MONTH_YEAR_2DIGIT_PATTERN = Pattern.compile(
            "(?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2}");
    private static final Pattern SHORT_TEXT_DATE_PATTERN = Pattern
            .compile("(?i)\\b^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{1,2} \\d{4}");
    private static final Pattern LONG_TEXT_DATE_PATTERN = Pattern.compile(
            "(?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{1,2} \\d{4}");
    private static final Pattern SHORT_TEXT_DATE_REVERSE_PATTERN = Pattern
            .compile("\\d{1,2} (?i)\\b^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{4}");
    private static final Pattern LONG_TEXT_DATE_REVERSE_PATTERN = Pattern
            .compile(
                    "\\d{1,2} (?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{4}");
    private static final Pattern SHORT_TEXT_DATE_REVERSE_2DIGIT_PATTERN = Pattern
            .compile("\\d{1,2} (?i)\\b^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b \\d{2}");
    private static final Pattern LONG_TEXT_DATE_REVERSE_2DIGIT_PATTERN = Pattern
            .compile(
                    "\\d{1,2} (?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2}");
    private static final Pattern LONG_TEXT_DATE_at_HOUR_CLOCK_PATTERN = Pattern
            .compile(
                    "\\d{1,2} (?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} at \\d{1,2}");
    private static final Pattern LONG_TEXT_DATE_HOUR_CLOCK_PATTERN = Pattern
            .compile(
                    "\\d{1,2} (?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} \\d{1,2} o'clock");
    private static final Pattern LONG_TEXT_DATE_TIME_PATTERN = Pattern
            .compile(
                    "\\d{1,2} (?i)\\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b \\d{2} \\d{1,2}:\\d{1,2}");
    private static final Pattern NUMERIC_DATE_2D = Pattern.compile("\\d{1,2} \\d{1,2} \\d{2}");
    private static final Pattern NUMERIC_DATE = Pattern.compile("\\d{1,2} \\d{1,2} \\d{4}");
    private static final Pattern DAY_MONTH_PATTERN = Pattern.compile(
            "\\d{1,2} \\b^(january|february|march|april|may|june|july|august|september|october|november|december)\\b");

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
        Matcher longTextDateTimeMatcher = LONG_TEXT_DATE_TIME_PATTERN.matcher(input);
        Matcher longTextDateHourClockMatcherat = LONG_TEXT_DATE_at_HOUR_CLOCK_PATTERN.matcher(input);
        Matcher longTextDateHourClockMatcher = LONG_TEXT_DATE_HOUR_CLOCK_PATTERN.matcher(input);
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
        Matcher yearMatcher = YEAR_PATTERN.matcher(input);

        if (longTextDateTimeMatcher.find()) {
            LocalDateTime date = LocalDateTime.parse(longTextDateTimeMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yy HH:mm")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.HOURS);
        } else if (longTextDateHourClockMatcherat.find()) {
            LocalDateTime date = LocalDateTime.parse(longTextDateHourClockMatcherat.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yy at HH")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.HOURS);
        } else if (longTextDateHourClockMatcher.find()) {
            LocalDateTime date = LocalDateTime.parse(longTextDateHourClockMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yy HH 'o''clock'")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.HOURS);
        } else if (longTextDateReverse2DigitMatcher.find()) {
            LocalDate date = LocalDate.parse(longTextDateReverse2DigitMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (shortTextDateReverse2DigitMatcher.find()) {
            LocalDate date = LocalDate.parse(shortTextDateReverse2DigitMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMM yy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (longTextDateReverseMatcher.find()) {
            LocalDate date = LocalDate.parse(longTextDateReverseMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMMM yyyy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (shortTextDateReverseMatcher.find()) {
            LocalDate date = LocalDate.parse(shortTextDateReverseMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MMM yyyy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (longTextDateMatcher.find()) {
            LocalDate date = LocalDate.parse(longTextDateMatcher.group(), new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMMM dd yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (shortTextDateMatcher.find()) {
            LocalDate date = LocalDate.parse(shortTextDateMatcher.group(), new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM dd yyyy")
                    .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (longTextMonthYearMatcher.find()) {
            YearMonth monthyear = YearMonth.parse(longTextMonthYearMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("MMMM yyyy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (shortTextMonthYearMatcher.find()) {
            YearMonth monthyear = YearMonth.parse(shortTextMonthYearMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("MMM yyyy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (longTextMonthYear2DigitMatcher.find()) {
            YearMonth monthyear = YearMonth.parse(longTextMonthYear2DigitMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("MMMM yy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (shortTextMonthYear2DigitMatcher.find()) {
            YearMonth monthyear = YearMonth.parse(shortTextMonthYear2DigitMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("MMM yy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(monthyear.atDay(1).atStartOfDay(), null, ChronoUnit.MONTHS);
        } else if (numericDateMatcher.find()) {
            LocalDateTime date = LocalDateTime.parse(numericDateMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MM yyyy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.DAYS);
        } else if (numericDate2DMatcher.find()) {
            LocalDateTime date = LocalDateTime.parse(numericDate2DMatcher.group(),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("dd MM yy")
                            .toFormatter(Locale.ENGLISH));
            return new ParsedDateTime(date, null, ChronoUnit.DAYS);
        } else if (dayMonthMatcher.find()) {
            String[] parts = input.split(" ");
            int day = Integer.parseInt(parts[0]);
            DateTimeFormatter MONTH_NAME_PARSER = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMM")
                    .toFormatter(Locale.ENGLISH);
            Month month = Month.from(MONTH_NAME_PARSER.parse(parts[1]));
            LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
            return new ParsedDateTime(date.atStartOfDay(), null, ChronoUnit.DAYS);
        } else if (yearMatcher.find()) {
            int yr = Integer.parseInt(yearMatcher.group());
            return new ParsedDateTime(LocalDateTime.of(yr, 1, 1, 0, 0), null, ChronoUnit.YEARS);
        } else {
            return new ParsedDateTime(null, null, ChronoUnit.YEARS);
        }
    }

}

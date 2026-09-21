package gestclub.util;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
public class DateFR {
    public final static String DATE_PATTERN = "dd/MM/yyyy";
    public final static DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static String format(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }
    public static LocalDate parse(String dateTxt) {
        try {
            return DATE_FORMATTER.parse(dateTxt, LocalDate::from);
        } catch(DateTimeParseException dtpe) {
            return null;
        }
    }
    public static boolean isDate(String dateTxt) {
        return parse(dateTxt) != null;
    }
}

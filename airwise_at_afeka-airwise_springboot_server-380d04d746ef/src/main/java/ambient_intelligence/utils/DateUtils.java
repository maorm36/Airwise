package ambient_intelligence.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class DateUtils {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static String getCurrentFormattedDate() {
        return OffsetDateTime.now().format(FORMATTER);
    }

    public static String createTimestampAndFormat(String timestamp) {
        return OffsetDateTime.parse(timestamp, FORMATTER).format(FORMATTER);
    }

    public static long getMinutesDiffFromNow(String timestamp) {
        OffsetDateTime inputTime = OffsetDateTime.parse(timestamp, FORMATTER);
        OffsetDateTime now = OffsetDateTime.now();
        return Duration.between(inputTime, now).toMinutes();
    }
}

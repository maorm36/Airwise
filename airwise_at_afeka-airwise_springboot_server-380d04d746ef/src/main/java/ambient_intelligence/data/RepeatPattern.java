package ambient_intelligence.data;

public enum RepeatPattern {
    ONCE,
    EVERY_DAY,
    EVERY_WEEKDAY,
    WEEKENDS;

    public static RepeatPattern fromString(String input) {
        return RepeatPattern.valueOf(input.trim().toUpperCase().replace(" ", "_"));
    }
}
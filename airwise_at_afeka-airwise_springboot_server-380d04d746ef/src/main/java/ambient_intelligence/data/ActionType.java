package ambient_intelligence.data;

public enum ActionType {
    TURN_ON,
    TURN_OFF;

    public static ActionType fromString(String input) {
        return ActionType.valueOf(input.trim().toUpperCase().replace(" ", "_"));
    }
}
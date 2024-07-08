package logger;

public enum LogLevel {
    ERROR("\u001B[31m"),
    SUCCESS("\u001B[32m"),
    INFO("\u001B[90m");

    private final String color;

    LogLevel(String color) {
        this.color = color;
    }

    public static final String colorReset = "\u001B[0m";

    public String getColor() {
        return color;
    }
}

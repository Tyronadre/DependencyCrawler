package logger;

import java.util.HashMap;
import java.util.Map;

public abstract class Logger {
    protected static boolean disabled = false;
    protected static boolean verbose = true;

    private static final Map<String, Logger> loggers = new HashMap<>();

    public static Logger of(String name) {
        if (name.isEmpty() || name.length() > 20) {
            throw new IllegalArgumentException("Name must be between 1 and 20 characters, was " + name + ".");
        }
        if (!loggers.containsKey(name)) {
            loggers.put(name, new DefaultLogger(name));
        }
        return loggers.get(name);
    }

    public abstract void appendInfo(String msg);

    public abstract void appendError(String msg);

    public abstract void appendSuccess(String msg);

    public abstract void info(String msg);

    public abstract void error(String msg);

    public abstract void success(String msg);

    public abstract void errorOverwriteLine(String msg, int index);

    public static void setVerbose(boolean verbose) {
        Logger.verbose = verbose;
    }

    public static void setDisabled(boolean disabled) {
        Logger.disabled = disabled;
    }
}

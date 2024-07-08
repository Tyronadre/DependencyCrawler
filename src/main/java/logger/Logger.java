package logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public abstract class Logger implements System.Logger {
    // if true, the logger will print the full stack trace of an exception
    static final boolean devMode = true;

    protected static LogLevel level = LogLevel.INFO;

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

    public static void setLevel(LogLevel level) {
        Logger.level = level;
    }

    public abstract void info(String msg);

    public abstract void error(String msg);

    public void error(String msg, Exception e) {
        if (devMode) {
            error(msg + "\n" + e + "\n" + Arrays.stream(e.getStackTrace()).map(it -> "\t" + it.toString()).collect(Collectors.joining("\n")));
        } else {
            error(msg + " " + e);
        }
    }

    public abstract void success(String msg);

    public void normal(String s) {
        System.out.println(LogLevel.colorReset + s);
    }


    // ------ Classes from LOGGER ------ //

    @Override
    public boolean isLoggable(Level level) {
        return true;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        switch (level) {
            case ALL -> normal(msg + thrown.getMessage());
            case TRACE, DEBUG, INFO -> info(msg + thrown.getMessage());
            case WARNING, ERROR -> error(msg + thrown.getMessage());
            case OFF -> {
            }
        }
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params) {
        switch (level) {
            case ALL -> normal(Arrays.toString(params));
            case TRACE, DEBUG, INFO -> info(Arrays.toString(params));
            case WARNING, ERROR -> error(Arrays.toString(params));
            case OFF -> {
            }
        }
    }
}

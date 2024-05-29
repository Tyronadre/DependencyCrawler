package logger;

import java.util.HashMap;
import java.util.Map;

public interface Logger {

    Map<String, Logger> loggers = new HashMap<>();

    static Logger of(String name) {
        if (name.isEmpty() || name.length() > 20) {
            throw new IllegalArgumentException("Name must be between 1 and 20 characters");
        }
        if (!loggers.containsKey(name)) {
            loggers.put(name, new DefaultLogger(name));
        }
        return loggers.get(name);
    }

    void appendInfo(String msg);

    void appendError(String msg);

    void appendSuccess(String msg);

    void info(String msg);

    void error(String msg);

    void success(String msg);

    void errorOverwriteLine(String msg, int index);

    void setVerbose(boolean verbose);

    void setDisabled(boolean disabled);
}

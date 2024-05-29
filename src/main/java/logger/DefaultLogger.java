package logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DefaultLogger implements Logger {
    private final String name;
    private boolean verbose = true;
    private boolean disabled = false;

    protected DefaultLogger(String name) {
        this.name = name;
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
    private String getPrefix(LogLevel level) {
        return formatter.format(LocalDateTime.now()) + " - " + name + " ".repeat(Math.max(0, 20 - name.length())) + level.getColor() + switch (level) {
            case INFO -> "    ";
            case ERROR -> "   ";
            case SUCCESS -> " ";
        } + level + " --- ";
    }

    private void log(LogLevel level, String msg) {
        if (disabled) {
            return;
        }
        if (!verbose && level == LogLevel.INFO) {
            return;
        }
        System.out.println(getPrefix(level) + msg + LogLevel.colorReset);
    }

    @Override
    public void appendInfo(String msg) {
        log(LogLevel.INFO, msg);
    }

    @Override
    public void appendError(String msg) {
        log(LogLevel.ERROR, msg);
    }

    @Override
    public void appendSuccess(String msg) {
        log(LogLevel.SUCCESS, msg);
    }

    @Override
    public void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    @Override
    public void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    @Override
    public void success(String msg) {
        log(LogLevel.SUCCESS, msg);
    }

    @Override
    public void errorOverwriteLine(String msg, int index) {
        throw new UnsupportedOperationException("Not supported by this logger");
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}

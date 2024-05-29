package logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

public class DefaultLogger implements Logger {
    private final String name;
    private boolean verbose = true;
    private boolean disabled = false;

    private static final OutputStream logFile;

    static {
        try {
            logFile = new FileOutputStream(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm_").format(LocalDateTime.now()) + "_log.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected DefaultLogger(String name) {
        this.name = name;
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

    private String getPrefix(LogLevel level, boolean withColor) {
        return formatter.format(LocalDateTime.now()) + " - " + name + " ".repeat(Math.max(0, 20 - name.length())) + (withColor ? level.getColor() : "") + switch (level) {
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

        System.out.println(getPrefix(level, true) + msg + LogLevel.colorReset);
        synchronized (logFile) {
            try {
                for (byte aByte : (getPrefix(level, false) + msg + "\n").getBytes(StandardCharsets.UTF_8)) {
                    logFile.write(aByte);
                }
                logFile.flush();
            } catch (IOException e) {
                System.err.println("Error writing to log file: " + e.getMessage());
            }
        }
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

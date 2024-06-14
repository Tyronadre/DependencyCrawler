package logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DefaultLogger extends Logger {
    private final String name;

    private static final OutputStream logFile;

    static {
        try {
            new File("logs").mkdirs();
            logFile = new FileOutputStream("logs/" + DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm_").format(LocalDateTime.now()) + "_log.txt");
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
    public void normal(String s) {
        System.out.println(LogLevel.colorReset + s);
    }

    @Override
    public void errorOverwriteLine(String msg, int index) {
        throw new UnsupportedOperationException("Not supported by this logger");
    }

}

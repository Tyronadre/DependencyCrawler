package logger;

import settings.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Logger {
    // if true, the logger will print the full stack trace of an exception
    private static final boolean devMode = true;
    private static final int numberOfLogFiles = 10;
    private static final Map<String, Logger> loggers = new HashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
    private static LogLevel level = LogLevel.INFO;
    private static final OutputStream logFile ;

    static {
        var logDir = new File(Settings.getDataFolder(), "logs");
            //delete the oldest log file if there are more than the max number of log files that should be kept
            if (logDir.exists() && logDir.isDirectory()) {
                var files = logDir.listFiles();
                if (files != null && files.length >= Logger.numberOfLogFiles)
                    Arrays.stream(files).min(Comparator.comparingLong(File::lastModified)).ifPresent(File::delete);
            } else if (logDir.exists() && !logDir.isDirectory()) {
                System.err.println("Could not create logs directory " + logDir.getAbsoluteFile() + ". It is not a directory. Logs will not be written to a file.");
            } else if (!logDir.exists()) {
                if (!new File(Settings.getDataFolder(), "logs").mkdirs()) {
                    System.err.println("Could not create logs directory " + logDir.getAbsoluteFile() + ". Logs will not be written to a file.");
                }
            }

        OutputStream logFile1;
        try {
            logFile1 = new FileOutputStream(new File(logDir, DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm_").format(LocalDateTime.now()) + "_log.txt"));
        } catch (FileNotFoundException e) {
            System.err.println("Could not create log file: " + e.getMessage() + ". Logs will not be written to a file.");
            logFile1 = null;
        }
        logFile = logFile1;
    }

    /**
     * Returns a logger with the given name
     * @param name The name of the logger
     * @return The logger
     */
    public static Logger of(String name) {
        if (name.isEmpty() || name.length() > 20) {
            throw new IllegalArgumentException("Name must be between 1 and 20 characters, was " + name + ".");
        }
        if (!loggers.containsKey(name)) {
            loggers.put(name, new Logger(name));
        }
        return loggers.get(name);
    }

    /**
     * Sets the maximum log level
     * @param level The maximum log level
     */
    public static void setLevel(LogLevel level) {
        Logger.level = level;
    }

    private final String name;
    private Logger(String name) {
        this.name = name;
    }

    /**
     * Logs an info message
     * @param msg The message to log
     */
    public void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    /**
     * Logs an error
     * @param msg The message to log
     */
    public void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    /**
     * Logs an error with an exception
     * @param msg the message to log
     * @param e the exception to log
     */
    public void error(String msg, Exception e) {
        if (devMode) {
            error(msg + "\n" + e + "\n" + Arrays.stream(e.getStackTrace()).map(it -> "\t" + it.toString()).collect(Collectors.joining("\n")));
        } else {
            error(msg + " " + e);
        }
    }

    /**
     * Logs a success message
     * @param msg The message to log
     */
    public void success(String msg) {
        log(LogLevel.SUCCESS, msg);
    }

    /**
     * Logs a message without any color, that will not be affected by the log level or written to a file
     * @param msg The message to log
     */
    public void normal(String msg) {
        System.out.println(LogLevel.colorReset + msg);
    }

    private void log(LogLevel level, String msg) {
        if (!Logger.devMode && Logger.level == null) {
            return;
        }
        if (!Logger.devMode && Logger.level.ordinal() < level.ordinal()) {
            return;
        }

        System.out.println(getPrefix(level, true) + msg + LogLevel.colorReset);

        if (logFile == null) return;

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

    private String getPrefix(LogLevel level, boolean withColor) {
        return formatter.format(LocalDateTime.now()) + " - " + name + " ".repeat(Math.max(0, 20 - name.length())) + (withColor ? level.getColor() : "") + switch (level) {
            case INFO -> "    ";
            case ERROR -> "   ";
            case SUCCESS -> " ";
        } + level + " --- ";
    }
}

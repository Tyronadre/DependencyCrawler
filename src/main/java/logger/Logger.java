package logger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logger {
    Map<Level, String> colorMap = Map.of(
            Level.ERROR, "\u001B[31m",
            Level.INFO, "\u001B[90m",
            Level.SUCCESS, "\u001B[32m"
    );
    String colorReset = "\u001B[0m";

    String name;

    private Logger(String name) {
        this.name = name;
    }

    private static final Map<String, Logger> loggers = new HashMap<>();

    public static Logger of(String name) {
        if (!loggers.containsKey(name)) {
            loggers.put(name, new Logger(name));
        }
        return loggers.get(name);
    }

    LogMessage currentLog = null;

    private void log() {
        System.out.print(colorReset + "\r" + currentLog.getLog());
    }

    private void logLine() {
        if (currentLog != null) {
            System.out.println(colorReset + "\r" + currentLog.getLog() + colorReset);
            currentLog = null;
        } else {
            System.out.println(colorReset);
        }
    }

    private void appendLog(Level level, String msg) {
        if (currentLog == null) {
            currentLog = new LogMessage(level, msg);
        } else {
            currentLog.append(level, msg);
        }
    }

    public void info(String msg) {
        appendLog(Level.INFO, msg);
        log();
    }

    public void error(String msg) {
        appendLog(Level.ERROR, msg);
        log();
    }

    public void success(String msg) {
        appendLog(Level.SUCCESS, msg);
        log();
    }

    public void infoLine(String msg) {
        appendLog(Level.INFO, msg);
        logLine();
    }

    public void errorLine(String msg) {
        appendLog(Level.ERROR, msg);
        logLine();
    }

    public void successLine(String msg) {
        appendLog(Level.SUCCESS, msg);
        logLine();
    }

    public void errorOverwriteLine(String msg, int index) {
        currentLog.overwrite(Level.ERROR, msg, index);
        logLine();
    }

    public enum Level {
        INFO, ERROR, SUCCESS
    }

    private class LogMessage {
        LocalDateTime startTime;
        LocalDateTime time;
        List<String> messages = new ArrayList<>();

        public LogMessage(Level level, String message) {
            this.startTime = this.time = LocalDateTime.now();
            this.addFirstMessage(level, message);
        }

        private void addFirstMessage(Level level, String message) {
            this.messages.add(0, colorMap.get(level) + switch (level) {
                case INFO -> "    ";
                case ERROR -> "   ";
                case SUCCESS -> " ";
            } + level + " --- " + message);
        }

        public void append(Level level, String message) {
            this.time = LocalDateTime.now();
            this.messages.add(colorMap.get(level) + message);
        }

        public String getLog() {
            var log = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh:mm:ss:SSS").format(time) + "\t" + String.join(" ", messages);
            if (startTime != time) {
                log += " (" + (time.toInstant(ZoneOffset.UTC).toEpochMilli() - startTime.toInstant(ZoneOffset.UTC).toEpochMilli()) + "ms)";
            }
            return log;
        }

        public void overwrite(Level level, String msg, int index) {
            messages.subList(index, messages.size()).clear();
            if (index == 0) {
                this.addFirstMessage(level, msg);
            } else {
                this.append(level, msg);
            }
        }
    }

}

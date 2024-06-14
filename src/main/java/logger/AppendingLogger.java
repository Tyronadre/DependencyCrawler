package logger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppendingLogger extends Logger{
    Map<LogLevel, String> colorMap = Map.of(
            LogLevel.ERROR, "\u001B[31m",
            LogLevel.INFO, "\u001B[90m",
            LogLevel.SUCCESS, "\u001B[32m"
    );
    String colorReset = "\u001B[0m";

    String name;

    protected AppendingLogger(String name) {
        this.name = name;
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

    private void appendLog(LogLevel LogLevel, String msg) {
        if (currentLog == null) {
            currentLog = new LogMessage(LogLevel, msg);
        } else {
            currentLog.append(LogLevel, msg);
        }
    }

    public void appendInfo(String msg) {
        appendLog(LogLevel.INFO, msg);
        log();
    }

    public void appendError(String msg) {
        appendLog(LogLevel.ERROR, msg);
        log();
    }

    public void appendSuccess(String msg) {
        appendLog(LogLevel.SUCCESS, msg);
        log();
    }

    public void info(String msg) {
        appendLog(LogLevel.INFO, msg);
        logLine();
    }

    public void error(String msg) {
        appendLog(LogLevel.ERROR, msg);
        logLine();
    }

    public void success(String msg) {
        appendLog(LogLevel.SUCCESS, msg);
        logLine();
    }

    @Override
    public void normal(String s) {
        
    }

    public void errorOverwriteLine(String msg, int index) {
        currentLog.overwrite(LogLevel.ERROR, msg, index);
        logLine();
    }

    private class LogMessage {
        LocalDateTime startTime;
        LocalDateTime time;
        List<String> messages = new ArrayList<>();

        public LogMessage(LogLevel LogLevel, String message) {
            this.startTime = this.time = LocalDateTime.now();
            this.addFirstMessage(LogLevel, message);
        }

        private void addFirstMessage(LogLevel LogLevel, String message) {
            this.messages.add(0, colorMap.get(LogLevel) + switch (LogLevel) {
                case INFO -> "    ";
                case ERROR -> "   ";
                case SUCCESS -> " ";
                case NORMAL -> "";
            } + LogLevel + " --- " + message);
        }

        public void append(LogLevel LogLevel, String message) {
            this.time = LocalDateTime.now();
            this.messages.add(colorMap.get(LogLevel) + message);
        }

        public String getLog() {
            var log = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh:mm:ss:SSS").format(time) + "\t" + String.join(" ", messages);
            if (startTime != time) {
                log += " (" + (time.toInstant(ZoneOffset.UTC).toEpochMilli() - startTime.toInstant(ZoneOffset.UTC).toEpochMilli()) + "ms)";
            }
            return log;
        }

        public void overwrite(LogLevel LogLevel, String msg, int index) {
            messages.subList(index, messages.size()).clear();
            if (index == 0) {
                this.addFirstMessage(LogLevel, msg);
            } else {
                this.append(LogLevel, msg);
            }
        }
    }
}

package logger;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class ExecutionServiceLogger {
    private final int THREADS;
    private final String[] lines;

    public ExecutionServiceLogger(int threads) {
        this.THREADS = threads;
        this.lines = new String[threads];
        AnsiConsole.systemInstall();

        for (int i = 0; i < THREADS; i++) {
            lines[i] = Ansi.ansi().fg(Ansi.Color.BLUE).a(" THREAD " + i + ": " + buildProgressBar(0) + String.format(" %.2f%%", 0.0)).toString();
        }

        for (String line : lines) {
            System.out.println(line);
        }
    }

    public synchronized void displayProgress(long threadId, String threadStatus, double progress, String name, String data) {
        String progressBar = buildProgressBar(progress);
        Ansi.Color color = progress < 100 ? Ansi.Color.YELLOW : Ansi.Color.GREEN;

        lines[(int) (threadId % THREADS)] = Ansi.ansi().fg(color).a(" THREAD " + (threadId % THREADS + 1) + "\t" + ": " + progressBar + threadStatus + String.format(" %.2f%%", progress) + " " + name + " " + data).reset().toString();
        printProgress();
    }

    private void printProgress() {
        System.out.print(Ansi.ansi().cursorUp(THREADS));

        for (String line : lines) {
            System.out.println(line);
        }
        System.out.flush();
    }

    private String buildProgressBar(double progress) {
        StringBuilder progressBar = new StringBuilder("[");
        int progressChars = (int) (progress / 100 * 20);
        progressBar.append("=".repeat(Math.max(0, progressChars)));
        progressBar.append(" ".repeat(Math.max(0, 20 - progressChars)));
        progressBar.append("]");
        return progressBar.toString();
    }

    public void end() {
        AnsiConsole.systemUninstall();
    }

    class LineData {
        double progress;

    }
}

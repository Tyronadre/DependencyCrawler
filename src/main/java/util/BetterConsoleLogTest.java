package util;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BetterConsoleLogTest {
    private static final int TOTAL_TASKS = 100;
    private static final int THREADS = 4;
    private static final String[] progressLines = new String[THREADS];

    public static void main(String[] args) throws InterruptedException {
        AnsiConsole.systemInstall();

        // Print initial content in the terminal
        System.out.println("Initial terminal content...");
        System.out.println("Starting tasks...");

        // Initialize progress lines
        for (int i = 0; i < THREADS; i++) {
            progressLines[i] = Ansi.ansi().fg(Ansi.Color.YELLOW).a("L Task " + i + ": " + buildProgressBar(0) + String.format(" %.2f%%", 0.0)).toString();
            System.out.println(progressLines[i]);
        }
        System.out.flush();


        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < TOTAL_TASKS; i++) {
            int finalI = i;
            executor.submit(() -> {

                try {
                    Thread.sleep(50);
                    for (int j = 1; j <= 37; j++) {
                        Thread.sleep(130);
                        displayProgress((int) (Thread.currentThread().getId() % THREADS + 1), finalI, j / 37.0 * 100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.awaitTermination(1, TimeUnit.HOURS);

        AnsiConsole.systemUninstall();
    }

    public static synchronized void displayProgress(int threadId, int taskId, double progress) throws InterruptedException {
        String progressBar = buildProgressBar(progress);
        Ansi.Color color = progress < 100 ? Ansi.Color.YELLOW : Ansi.Color.GREEN;

        progressLines[threadId - 1] = Ansi.ansi().fg(color).a(" THREAD " + threadId + ": " + progressBar + String.format(" %.2f%%", progress) + " TASK " + taskId).reset().toString();
        printProgress();

    }

    public static void printProgress() {
//        // Move the cursor to the starting point for the progress display
//        System.out.print(Ansi.ansi().cursorDown(2).eraseLine());
        System.out.print(Ansi.ansi().cursorUp(THREADS));

        // Print all progress lines
        for (String line : progressLines) {
            if (line != null) {
                System.out.print(Ansi.ansi().eraseLine().a(line).newline());
            }
        }
        System.out.flush();
    }

    public static String buildProgressBar(double progress) {
        int barLength = 30;
        int completed = (int) (progress / 100 * barLength);
        int remaining = barLength - completed;
        return "[" + "#".repeat(completed) + " ".repeat(remaining) + "]";
    }
}

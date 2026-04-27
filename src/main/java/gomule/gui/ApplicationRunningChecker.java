package gomule.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ApplicationRunningChecker implements Runnable {

    private final Runtime runtime;
    private final String applicationName;
    private final Runnable action;

    public ApplicationRunningChecker(Runtime runtime, String applicationName, Runnable action) {
        this.runtime = runtime;
        this.applicationName = applicationName;
        this.action = action;
    }

    public void start() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build()).scheduleWithFixedDelay(this, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        try {
            Process proc = runtime.exec(new String[]{"cmd", "/c", "tasklist"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                boolean isApplicationRunning = reader.lines()
                        .anyMatch(s -> s.startsWith(applicationName));
                if (isApplicationRunning) action.run();
            }
        } catch (IOException e) {
            System.err.println("ApplicationRunningChecker failed to run");
        }
    }
}
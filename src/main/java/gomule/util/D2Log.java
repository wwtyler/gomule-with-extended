package gomule.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Tiny dependency-free logger for GoMule.
 *
 * <p>Goals:
 * <ul>
 *   <li>Single static facade — no Logger wiring needed.</li>
 *   <li>Levels DEBUG / INFO / WARN / ERROR. Threshold via system property
 *       {@code -Dgomule.log.level=DEBUG} (default INFO).</li>
 *   <li>Simultaneously writes to stderr (legacy behaviour) <em>and</em>
 *       {@code logs/gomule.log} next to the running JAR — so GUI users that
 *       launch via double-click can still find a log.</li>
 *   <li>Each line is stamped with timestamp, level, area, and the current
 *       {@link D2LogContext} chain.</li>
 *   <li>{@link #error(String, Throwable, String, Object...)} writes the full
 *       stack trace to the file but only the first line to stderr.</li>
 * </ul>
 *
 * <p>NOT goals: async, JMX, structured JSON, runtime reconfig. If GoMule ever
 * needs that, we promote to SLF4J — until then this is the right size.
 */
public final class D2Log {

    public enum Level {
        DEBUG, INFO, WARN, ERROR;
        boolean enabledAt(Level threshold) { return this.ordinal() >= threshold.ordinal(); }
    }

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    /** Properties loaded once from {@code projects/app.properties} (best-effort). */
    private static final Properties FILE_PROPS = loadFileProps();
    private static final Level THRESHOLD;
    private static final PrintWriter FILE;

    static {
        Level lvl = Level.INFO;
        try {
            String p = resolve("gomule.log.level", "log.level");
            if (p != null) lvl = Level.valueOf(p.trim().toUpperCase());
        } catch (Exception ignore) { /* keep INFO */ }
        THRESHOLD = lvl;
        FILE = openFile();
    }

    /**
     * Look up a config value, preferring {@code -D} system properties over
     * {@code projects/app.properties}. Returns {@code null} if unset.
     */
    private static String resolve(String sysKey, String propKey) {
        String v = System.getProperty(sysKey);
        if (v != null && !v.isEmpty()) return v;
        if (FILE_PROPS != null) {
            v = FILE_PROPS.getProperty(propKey);
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    private static Properties loadFileProps() {
        // Anchored to D2Project.PROJECTS_DIR ("projects"); resolved relative to CWD.
        Path p = Paths.get("projects", "app.properties");
        if (!Files.isRegularFile(p)) return null;
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(p.toFile())) {
            props.load(in);
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    private D2Log() {}

    public static void debug(String area, String fmt, Object... args) { log(Level.DEBUG, area, null, fmt, args); }
    public static void info (String area, String fmt, Object... args) { log(Level.INFO , area, null, fmt, args); }
    public static void warn (String area, String fmt, Object... args) { log(Level.WARN , area, null, fmt, args); }
    public static void error(String area, String fmt, Object... args) { log(Level.ERROR, area, null, fmt, args); }
    public static void error(String area, Throwable t, String fmt, Object... args) { log(Level.ERROR, area, t, fmt, args); }

    private static void log(Level lvl, String area, Throwable t, String fmt, Object... args) {
        if (!lvl.enabledAt(THRESHOLD)) return;
        String msg;
        try { msg = (args == null || args.length == 0) ? fmt : String.format(fmt, args); }
        catch (Exception e) { msg = fmt + " {format-error: " + e.getMessage() + "}"; }

        String line = String.format("%s %-5s %-12s%s  %s",
                LocalDateTime.now().format(TS), lvl, area,
                D2LogContext.render(), msg);

        // stderr: first line only (no full trace) to avoid scrollback flood
        System.err.println(line);
        if (t != null && (lvl == Level.ERROR || THRESHOLD == Level.DEBUG)) {
            System.err.println("    cause: " + t.getClass().getSimpleName()
                    + ": " + t.getMessage());
        }

        // file: full detail
        if (FILE != null) {
            synchronized (FILE) {
                FILE.println(line);
                if (t != null) t.printStackTrace(FILE);
                FILE.flush();
            }
        }
    }

    /** Reset the log file (delete + reopen). Useful for tests / debug sessions. */
    public static synchronized void rotate() {
        Path log = logFilePath();
        if (log == null) return;
        try {
            Path bak = log.resolveSibling("gomule.log.1");
            if (Files.exists(log)) Files.move(log, bak, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignore) { /* best effort */ }
    }

    // ---------------------------------------------------------------- private

    private static PrintWriter openFile() {
        Path log = logFilePath();
        if (log == null) return null;
        try {
            Files.createDirectories(log.getParent());
            // Roll on every JVM start: keep current as .1, truncate fresh.
            if (Files.exists(log) && Files.size(log) > 0) {
                Path bak = log.resolveSibling("gomule.log.1");
                Files.move(log, bak, StandardCopyOption.REPLACE_EXISTING);
            }
            Writer w = Files.newBufferedWriter(log, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            PrintWriter pw = new PrintWriter(w, false);
            pw.println("# GoMule log opened " + LocalDateTime.now()
                    + "  threshold=" + THRESHOLD + "  file=" + log.toAbsolutePath()
                    + "  config=" + (FILE_PROPS != null ? "projects/app.properties" : "defaults"));
            pw.flush();
            return pw;
        } catch (Exception e) {
            System.err.println("[D2Log] Failed to open log file: " + e);
            return null;
        }
    }

    private static Path logFilePath() {
        try {
            // 1. Full path override wins (sys prop > properties).
            String fileOverride = resolve("gomule.log.file", "log.file");
            if (fileOverride != null) return Paths.get(fileOverride);

            // 2. Directory override (sys prop > properties); filename stays gomule.log.
            String dirOverride = resolve("gomule.log.dir", "log.dir");
            if (dirOverride != null) return Paths.get(dirOverride, "gomule.log");

            // 3. Default: anchor next to the running JAR (works for both fat-jar and IDE runs).
            Path here = Paths.get(D2Log.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path dir = Files.isDirectory(here) ? here : here.getParent();
            if (dir == null) dir = Paths.get(".");
            return dir.resolve("logs").resolve("gomule.log");
        } catch (Exception e) {
            return Paths.get("logs", "gomule.log");
        }
    }
}

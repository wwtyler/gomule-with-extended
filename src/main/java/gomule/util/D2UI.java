package gomule.util;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Lightweight UI configuration helper.
 *
 * <p>Reads optional UI tuning knobs from {@code projects/app.properties}
 * (same file used by {@link D2Log}). All values are also overridable via
 * {@code -D} system properties for power users.
 *
 * <p>Supported keys:
 * <ul>
 *   <li>{@code ui.scale} (sys: {@code gomule.ui.scale}) — display scale factor
 *       for inventory/stash panels (default {@code 1.5}, clamped 0.5..3.0).
 *       Both background images and item icons render larger; mouse coordinates
 *       are translated back to native space so click-handling logic is
 *       unchanged.</li>
 *   <li>{@code ui.tooltip.font.size} (sys: {@code gomule.ui.tooltip.font.size}) —
 *       Swing tooltip font size in points (default {@code 16}, clamped
 *       8..48).</li>
 *   <li>{@code ui.menu.font.size} (sys: {@code gomule.ui.menu.font.size}) —
 *       Default font size in points for all Swing UI components (menus,
 *       buttons, labels, tables, etc.). Default {@code 0} = keep L&F
 *       default; any positive value overrides. Clamped 8..48.</li>
 * </ul>
 */
public final class D2UI {

    private static final Properties FILE_PROPS = loadFileProps();
    private static double           uiScale     = computeUiScale();
    private static int              tooltipFont  = computeTooltipFontSize();
    private static int              menuFont     = computeMenuFontSize();

    private D2UI() {}

    /** UI scale factor for inventory/stash painter panels. */
    public static double getUiScale() { return uiScale; }

    /**
     * Update the UI scale factor at runtime (clamped to 0.5–3.0).
     * Callers must trigger a rebuild/repack of all open inventory panels
     * after calling this method.
     */
    public static void setUiScale(double scale) {
        uiScale = Math.max(0.5, Math.min(3.0, scale));
    }

    /** Tooltip font size in points. */
    public static int getTooltipFontSize() { return tooltipFont; }

    /** Update tooltip font size at runtime (clamped 8–48). */
    public static void setTooltipFontSize(int size) {
        tooltipFont = Math.max(8, Math.min(48, size));
    }

    /**
     * Default font size for all Swing UI components (menus, buttons, labels…).
     * Returns 0 when the user has not set the property (keep L&F default).
     */
    public static int getMenuFontSize() { return menuFont; }

    /** Update menu/component font size at runtime (clamped 8–48; 0 = L&F default). */
    public static void setMenuFontSize(int size) {
        menuFont = (size <= 0) ? 0 : Math.max(8, Math.min(48, size));
    }

    /** Scale an int dimension by {@link #getUiScale()} (HALF_UP rounding). */
    public static int sx(int n) { return (int) Math.round(n * uiScale); }

    /** Reverse-scale a screen-space int back to native coordinates. */
    public static int unsx(int n) { return (int) Math.round(n / uiScale); }

    private static Properties loadFileProps() {
        // 1. Load global app.properties (base layer)
        Path globalPath = Paths.get("projects", "app.properties");
        Properties props = new Properties();
        if (Files.isRegularFile(globalPath)) {
            try (FileInputStream in = new FileInputStream(globalPath.toFile())) {
                props.load(in);
            } catch (Exception e) {
                // ignore, use defaults
            }
        }
        // 2. Load current project's project.properties (overrides global ui settings)
        String currentProject = props.getProperty("current-project");
        if (currentProject != null && !currentProject.isEmpty()) {
            Path projectPath = Paths.get("projects", currentProject, "project.properties");
            if (Files.isRegularFile(projectPath)) {
                try (FileInputStream in = new FileInputStream(projectPath.toFile())) {
                    Properties projectProps = new Properties();
                    projectProps.load(in);
                    // Project-level ui settings override global ones
                    for (String key : new String[]{"ui.scale", "ui.tooltip.font.size", "ui.menu.font.size"}) {
                        String val = projectProps.getProperty(key);
                        if (val != null && !val.isEmpty()) {
                            props.setProperty(key, val);
                        }
                    }
                } catch (Exception e) {
                    // ignore, keep global values
                }
            }
        }
        return props.isEmpty() ? null : props;
    }

    private static String resolve(String sysKey, String propKey) {
        String v = System.getProperty(sysKey);
        if (v != null && !v.isEmpty()) return v;
        if (FILE_PROPS != null) {
            v = FILE_PROPS.getProperty(propKey);
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    private static double computeUiScale() {
        String s = resolve("gomule.ui.scale", "ui.scale");
        double def = 1.5;
        if (s == null) return def;
        try {
            double v = Double.parseDouble(s.trim());
            return Math.max(0.5, Math.min(3.0, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int computeTooltipFontSize() {
        String s = resolve("gomule.ui.tooltip.font.size", "ui.tooltip.font.size");
        int def = 16;
        if (s == null) return def;
        try {
            int v = Integer.parseInt(s.trim());
            return Math.max(8, Math.min(48, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int computeMenuFontSize() {
        String s = resolve("gomule.ui.menu.font.size", "ui.menu.font.size");
        if (s == null) return 0;  // 0 = keep L&F default
        try {
            int v = Integer.parseInt(s.trim());
            return (v <= 0) ? 0 : Math.max(8, Math.min(48, v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

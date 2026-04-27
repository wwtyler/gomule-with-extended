package gomule.translations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static gomule.files.FileReaderUtils.getResource;
import gomule.gui.FileManagerProperties;

public class TranslationsLoader {

    /**
     * Property key in app.properties that controls the display locale.
     * Defaults to {@code zhCN}.
     */
    public static final String PROP_LOCALE = "translations.locale";
    public static final String DEFAULT_LOCALE = "zhCN";

    /** Property key for lng search directories (semicolon-separated). */
    public static final String PROP_LNG_DIRS = "lng.data.dirs";

    // Default lng directories — separate from sprite.data.dirs
    private static final String DEFAULT_LNG_DIRS =
            "D:\\BlizGames\\Diablo II Resurrected\\mods\\D2RMMMDKV3\\D2RMMMDKV3.mpq\\data" + ";" +
            "D:\\260305\\D2RM_Ladiks Casc Viewer\\Work\\data\\data";

    /** File names to scan in {@code local/lng/strings/} within each data dir. */
    private static final String[] LNG_FILES = {
            "item-names.json",
            "item-modifiers.json",
            "item-nameaffixes.json",
            "item-runes.json",
            "mercenaries.json",
            "monsters.json",
            "npcs.json",
            "skills.json",
            "ui-controller.json",
    };

    public static String loadLocale() {
        try {
            Properties props = FileManagerProperties.loadFileManagerProperties();
            String locale = props.getProperty(PROP_LOCALE);
            if (locale != null && !locale.isBlank()) {
                return locale.trim();
            }
        } catch (IOException e) {
            // fall through to default
        }
        return DEFAULT_LOCALE;
    }

    public static void saveLocale(String locale) {
        try {
            Properties props = FileManagerProperties.loadFileManagerProperties();
            props.setProperty(PROP_LOCALE, locale);
            FileManagerProperties.saveFileManagerProperties(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Translations loadTranslations() {
        String locale = loadLocale();
        List<Translations> list = new ArrayList<>();

        // ── 1. Mod overrides from external lng directories (higher priority) ──
        List<File> lngDirs = loadLngDirs();
        for (File dataDir : lngDirs) {
            for (String fileName : LNG_FILES) {
                File lngFile = new File(dataDir,
                        "local" + File.separator + "lng" + File.separator + "strings" + File.separator + fileName);
                if (lngFile.isFile()) {
                    try (InputStream is = new FileInputStream(lngFile)) {
                        list.add(MapBasedTranslations.loadTranslations(is, locale));
                    } catch (IOException e) {
                        // skip unreadable files silently
                    }
                }
            }
        }

        // ── 2. Bundled JAR resources (fallback, always enUS) ──
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/item-names.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/item-modifiers.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/item-nameaffixes.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/item-runes.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/mercenaries.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/monsters.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/npcs.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/skills.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/ui-controller.json")));
        list.add(MapBasedTranslations.loadTranslations(getResource("d2Files/D2R_1.0/translations/custom-gomule.json")));

        return new CompositeTranslations(list.toArray(new Translations[0]));
    }

    /** Returns the configured lng data directories (in order). */
    public static List<File> loadLngDirs() {
        String raw = DEFAULT_LNG_DIRS;
        try {
            Properties props = FileManagerProperties.loadFileManagerProperties();
            String stored = props.getProperty(PROP_LNG_DIRS);
            if (stored != null && !stored.isBlank()) {
                raw = stored;
            }
        } catch (IOException e) {
            // use defaults
        }
        List<File> dirs = new ArrayList<>();
        for (String part : raw.split(";")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                dirs.add(new File(trimmed));
            }
        }
        return dirs;
    }

    /** Persists the lng data directories to filemanager.properties. */
    public static void saveLngDirs(List<String> paths) {
        try {
            Properties props = FileManagerProperties.loadFileManagerProperties();
            props.setProperty(PROP_LNG_DIRS, String.join(";", paths));
            FileManagerProperties.saveFileManagerProperties(props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

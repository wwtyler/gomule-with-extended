package gomule;

import gomule.translations.Translations;
import gomule.translations.TranslationsLoader;

public class D2Files {

    private static D2Files INSTANCE;
    private final Translations translations;

    private D2Files(Translations translations) {
        this.translations = translations;
    }

    public static D2Files getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new D2Files(TranslationsLoader.loadTranslations());
        }
        return INSTANCE;
    }

    /**
     * Forces the singleton to reload translations on the next {@link #getInstance()} call.
     * Call this after the data-directory or locale settings change.
     */
    public static synchronized void reset() {
        INSTANCE = null;
    }

    public Translations getTranslations() {
        return translations;
    }
}

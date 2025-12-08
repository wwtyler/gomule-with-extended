package gomule.translations;

import javax.annotation.Nullable;

public interface Translations {
    @Nullable
    String getTranslationOrNull(String key);

    default String getTranslation(String key) {
        String translationOrNull = getTranslationOrNull(key);
//        if (translationOrNull == null) throw new IllegalArgumentException("No translation for " + key);
        if (translationOrNull == null) {return  key;}
        else return translationOrNull;
    }
}

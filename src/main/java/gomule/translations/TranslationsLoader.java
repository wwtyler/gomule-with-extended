package gomule.translations;

import static gomule.files.FileReaderUtils.getResource;

public class TranslationsLoader {

    public static Translations loadTranslations() {
        String toPath =  "d2Files/D2R_1.0/translations/";
        // String toPath =  "translations/";
        return new CompositeTranslations(
                MapBasedTranslations.loadTranslations(getResource(toPath + "item-names.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "item-modifiers.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "item-nameaffixes.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "item-runes.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "mercenaries.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "monsters.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "npcs.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "skills.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "ui-controller.json")),
                MapBasedTranslations.loadTranslations(getResource(toPath + "custom-gomule.json")));
    }
}

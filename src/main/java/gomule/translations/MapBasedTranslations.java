package gomule.translations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.ImmutableMap;

public class MapBasedTranslations implements Translations {
    private final Map<String, String> translationData;
    private static final JsonMapper MAPPER = new JsonMapper();

    public MapBasedTranslations(Map<String, String> translationData) {
        this.translationData = translationData;
    }

    @SuppressWarnings("null")
    public static Translations loadTranslations(InputStream inputStream) {
        try {
            // Use a mutable HashMap to tolerate duplicate keys in mod translation files
            // (e.g. MDK V3 item-modifiers.json contains "Chaotic" twice).
            // Last entry wins, matching D2R's MPQ-load behavior.
            java.util.HashMap<String, String> map = new java.util.HashMap<>();
            MAPPER.readTree(inputStream).forEach(node -> {
                String key = node.get("Key").textValue();
                String val = node.get("enUS").textValue();
                if (key != null && val != null) {
                    map.put(key, val);
                }
            });
            return new MapBasedTranslations(ImmutableMap.copyOf(map));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTranslationOrNull(String key) {
        return translationData.get(key);
    }

    @Override
    public String toString() {
        return "MapBasedTranslations{" + "translationData=" + translationData + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapBasedTranslations that = (MapBasedTranslations) o;
        return Objects.equals(translationData, that.translationData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(translationData);
    }
}

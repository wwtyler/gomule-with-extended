package gomule.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import randall.d2files.D2TxtFile;
import randall.d2files.D2TxtFileItemProperties;

/**
 * Cache for D2R item sprite images.
 *
 * Resolution order for sprite files:
 *   1. Each configured data directory (sprite.data.dirs in app.properties), in order
 *   2. First matching .lowend.sprite file wins
 *
 * Items.json lookup:  hd/items/items.json  → itemCode → asset path
 * Sprite path:        hd/global/ui/items/{category}/{asset}.lowend.sprite
 * Category derived from which TSV file contains the item code (misc/armor/weapon).
 */
public class D2SpriteCache {

    private static final String SENTINEL_NOT_FOUND = "__NOT_FOUND__";
    private static final String PROP_SPRITE_DIRS = "sprite.data.dirs";

    // Default search directories (semicolon-separated, tried in order)
    private static final String DEFAULT_SPRITE_DIRS =
            "D:\\BlizGames\\Diablo II Resurrected\\mods\\TylerPack\\TylerPack.mpq\\data" + ";" +
            "D:\\260305\\D2RM_Ladiks Casc Viewer\\Work\\data\\data";

    private static D2SpriteCache INSTANCE;

    /** itemCode → asset string (e.g. "gem/chipped_diamond") */
    private final Map<String, String> assetMap = new HashMap<>();
    /** itemCode → cached BufferedImage (null values stored as SENTINEL) */
    private final Map<String, Object> imageCache = new ConcurrentHashMap<>();

    private List<File> dataDirs;
    private boolean itemsJsonLoaded = false;

    private D2SpriteCache() {
        loadDataDirs();
    }

    public static synchronized D2SpriteCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new D2SpriteCache();
        }
        return INSTANCE;
    }

    /** Call this to force re-read of config (e.g. after settings change). */
    public static synchronized void reset() {
        INSTANCE = null;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the scaled/raw sprite image for the given item code, or null. */
    public BufferedImage getImage(String itemCode) {
        return getImage(itemCode, -1);
    }

    /**
     * Returns the sprite image for the given item code and variable-graphic variant index.
     * When gfxNum >= 0, tries the variant file first (e.g. "ring/ring1" for gfxNum=0),
     * then falls back to the base asset ("ring/ring") if the variant file is not found.
     * When gfxNum < 0, returns the base asset image directly.
     */
    public BufferedImage getImage(String itemCode, int gfxNum) {
        if (itemCode == null || itemCode.isBlank()) return null;
        String key = gfxNum >= 0 ? (itemCode.trim() + ":" + gfxNum) : itemCode.trim();

        Object cached = imageCache.get(key);
        if (cached == SENTINEL_NOT_FOUND) return null;
        if (cached instanceof BufferedImage image) return image;

        ensureItemsJsonLoaded();

        String asset = assetMap.get(itemCode.trim());
        if (asset == null) {
            imageCache.put(key, SENTINEL_NOT_FOUND);
            return null;
        }

        String category = resolveCategory(itemCode.trim());
        if (category == null) {
            imageCache.put(key, SENTINEL_NOT_FOUND);
            return null;
        }

        // When gfxNum >= 0, build variant asset path by appending (gfxNum+1) to the filename part.
        // E.g. "ring/ring" + "1" → "ring/ring1" for gfxNum=0
        String resolvedAsset = asset;
        if (gfxNum >= 0) {
            resolvedAsset = asset + (gfxNum + 1);
        }

        // Path: hd/global/ui/items/{category}/{asset}.sprite (HD) or .lowend.sprite (fallback)
        String assetPath = "hd" + File.separator + "global" + File.separator + "ui" + File.separator
                + "items" + File.separator + category + File.separator
                + resolvedAsset.replace("/", File.separator);

        File spriteFile = resolveFile(assetPath + ".sprite");
        if (spriteFile == null) {
            spriteFile = resolveFile(assetPath + ".lowend.sprite");
        }

        // If variant file not found, fall back to base asset
        if (spriteFile == null && gfxNum >= 0) {
            String basePath = "hd" + File.separator + "global" + File.separator + "ui" + File.separator
                    + "items" + File.separator + category + File.separator
                    + asset.replace("/", File.separator);
            spriteFile = resolveFile(basePath + ".sprite");
            if (spriteFile == null) {
                spriteFile = resolveFile(basePath + ".lowend.sprite");
            }
        }

        if (spriteFile == null) {
            imageCache.put(key, SENTINEL_NOT_FOUND);
            return null;
        }

        BufferedImage img = SpriteParser.parse(spriteFile);
        imageCache.put(key, img != null ? img : SENTINEL_NOT_FOUND);
        return img;
    }

    /** Returns the configured data directories (for display in settings). */
    public List<File> getDataDirs() {
        return Collections.unmodifiableList(dataDirs);
    }

    /** Update and persist the data directories. Resets cached images. */
    public void setDataDirs(List<String> paths) {
        try {
            Properties props = FileManagerProperties.loadFileManagerProperties();
            props.setProperty(PROP_SPRITE_DIRS, String.join(";", paths));
            FileManagerProperties.saveFileManagerProperties(props);
        } catch (IOException e) {
            System.err.println("Failed to save sprite directories: " + e.getMessage());
        }
        loadDataDirs();
        imageCache.clear();
        itemsJsonLoaded = false;
        assetMap.clear();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void loadDataDirs() {
        String raw = DEFAULT_SPRITE_DIRS;
        try {
            Properties props = FileManagerProperties.loadFileManagerProperties();
            String stored = props.getProperty(PROP_SPRITE_DIRS);
            if (stored != null && !stored.isBlank()) {
                raw = stored;
            }
        } catch (IOException e) {
            // use defaults
        }

        dataDirs = new ArrayList<>();
        for (String part : raw.split(";")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                dataDirs.add(new File(trimmed));
            }
        }
    }

    private void ensureItemsJsonLoaded() {
        if (itemsJsonLoaded) return;
        itemsJsonLoaded = true;

        // Try each data dir for hd/items/items.json
        String relJson = "hd" + File.separator + "items" + File.separator + "items.json";
        File jsonFile = resolveFile(relJson);
        if (jsonFile == null) return;

        try {
            String json = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
            parseItemsJson(json);
        } catch (IOException e) {
            // leave assetMap empty
        }
    }

    /**
     * Minimal JSON parser for the items.json array format:
     * [{"hax":{"asset":"axe/hand_axe"}}, {"gcw":{"asset":"gem/chipped_diamond"}}, ...]
     */
    private void parseItemsJson(String json) {
        // Simple regex-free approach: scan for "code":{"asset":"value"} patterns
        int pos = 0;
        while (pos < json.length()) {
            // Find next "
            int q1 = json.indexOf('"', pos);
            if (q1 < 0) break;
            int q2 = json.indexOf('"', q1 + 1);
            if (q2 < 0) break;
            String code = json.substring(q1 + 1, q2);

            // After code, expect :{"asset":"<value>"}
            int colon = json.indexOf(':', q2);
            if (colon < 0) break;
            int assetIdx = json.indexOf("\"asset\"", colon);
            if (assetIdx < 0) break;
            int assetVal1 = json.indexOf('"', assetIdx + 7); // opening quote of value (past "asset":)
            if (assetVal1 < 0) break;
            int assetVal2 = json.indexOf('"', assetVal1 + 1);
            if (assetVal2 < 0) break;
            String asset = json.substring(assetVal1 + 1, assetVal2);

            if (!code.isEmpty() && !asset.isEmpty()) {
                assetMap.put(code, asset);
            }
            pos = assetVal2 + 1;
        }
    }

    /**
     * Determine the sprite category directory for a given item code.
     * Matches D2RMM's logic: misc.txt → "misc", armor.txt → "armor", weapons.txt → "weapon"
     */
    private String resolveCategory(String itemCode) {
        try {
            D2TxtFileItemProperties row = D2TxtFile.MISC.searchColumns("code", itemCode);
            if (row != null) return "misc";
        } catch (Exception ignored) {}
        try {
            D2TxtFileItemProperties row = D2TxtFile.ARMOR.searchColumns("code", itemCode);
            if (row != null) return "armor";
        } catch (Exception ignored) {}
        try {
            D2TxtFileItemProperties row = D2TxtFile.WEAPONS.searchColumns("code", itemCode);
            if (row != null) return "weapon";
        } catch (Exception ignored) {}
        return null;
    }

    /** Returns the first existing File for relPath among dataDirs, or null. */
    private File resolveFile(String relPath) {
        for (File dir : dataDirs) {
            File candidate = new File(dir, relPath);
            if (candidate.isFile()) return candidate;
        }
        return null;
    }
}

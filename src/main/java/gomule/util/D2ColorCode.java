package gomule.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class that converts D2R in-game color codes (ÿcX) to HTML {@code <font color="…">} tags
 * for display in Swing HTML tooltips.
 *
 * <p>Color codes are encoded as the two-byte sequence {@code \u00FFc} followed by a code character.
 * {@code ÿc0} resets the color back to the enclosing default.</p>
 *
 * <p>Standard and MDK-extended color codes are supported:</p>
 * <pre>
 *   ÿc0  white / reset      ÿc1  red (fire)       ÿc2  green (poison)
 *   ÿc3  blue (magic)       ÿc4  gold (unique)     ÿc5  gray (disabled)
 *   ÿc6  black              ÿc7  tan gold           ÿc8  orange
 *   ÿc9  yellow (lightning) ÿca  dark green
 *   MDK ColorMod extended codes:
 *   ÿcC  亮绿 limeGreen      ÿcM  紫罗兰 violet      ÿcO  天蓝 skyBlue
 *   ÿcS  琥珀 amber          ÿcT  暗红 darkRed        ÿcU  青 cyan
 *   ÿcV  亮红 brightRed      ÿc;  紫 purple
 * </pre>
 */
public final class D2ColorCode {

    /** The D2R color prefix character (U+00FF, Latin small letter y with diaeresis). */
    public static final char PREFIX = '\u00FF';

    /** Maps code character → HTML hex colour string (without '#'). */
    private static final Map<Character, String> COLOR_MAP = new LinkedHashMap<>();

    static {
        // Core palette (matches vanilla D2R palette)
        COLOR_MAP.put('0', "#FFFFFF"); // white / reset (shown as reset, not literal white)
        COLOR_MAP.put('1', "#FF4444"); // red   – fire
        COLOR_MAP.put('2', "#4DFF4D"); // green – poison
        COLOR_MAP.put('3', "#8787FF"); // blue  – magic label
        COLOR_MAP.put('4', "#A59141"); // tan/gold – unique
        COLOR_MAP.put('5', "#808080"); // gray  – disabled / ethereal description
        COLOR_MAP.put('6', "#000000"); // black
        COLOR_MAP.put('7', "#D2A85A"); // light tan-gold
        COLOR_MAP.put('8', "#FF8040"); // orange
        COLOR_MAP.put('9', "#FFFF00"); // yellow – lightning
        COLOR_MAP.put('a', "#208020"); // dark green
        COLOR_MAP.put('A', "#208020"); // dark green (uppercase alias)
        COLOR_MAP.put('b', "#AAAAAA"); // light gray
        COLOR_MAP.put('B', "#AAAAAA");
        COLOR_MAP.put('c', "#6060C0"); // medium blue
        // 'C' = 亮绿 limeGreen (MDK ColorMod)
        COLOR_MAP.put('C', "#00E040");
        COLOR_MAP.put('D', "#C0C0C0"); // silver
        COLOR_MAP.put('E', "#FF80FF"); // pink
        COLOR_MAP.put('F', "#FF80FF");
        COLOR_MAP.put('G', "#C8A800"); // rich gold
        // 'M' = 紫罗兰 violet (MDK ColorMod)
        COLOR_MAP.put('M', "#9900CC");
        COLOR_MAP.put('N', "#FF8000"); // orange (alias)
        // 'O' = 天蓝 skyBlue (MDK ColorMod)
        COLOR_MAP.put('O', "#00BFFF");
        COLOR_MAP.put('Q', "#CC99FF"); // lavender (MDK ColorMod, ÿcQ=薰衣草)
        // 'S' = 琥珀 amber (MDK ColorMod)
        COLOR_MAP.put('S', "#FFC000");
        // 'T' = 暗红 darkRed (MDK ColorMod)
        COLOR_MAP.put('T', "#8B0000");
        // 'U' = 青 cyan (MDK ColorMod)
        COLOR_MAP.put('U', "#00CCCC");
        // 'V' = 亮红 brightRed (MDK ColorMod)
        COLOR_MAP.put('V', "#FF3030");
        // ';' = 紫 purple (MDK ColorMod)
        COLOR_MAP.put(';', "#9040C0");
    }

    private D2ColorCode() {}

    /**
     * Converts D2R color codes in {@code raw} to HTML {@code <font color="…">} tags.
     *
     * <ul>
     *   <li>Each {@code ÿcX} (where X is not {@code '0'}) opens a {@code <font color="…">} span.</li>
     *   <li>{@code ÿc0} closes all currently open font spans (returning to the surrounding color).</li>
     *   <li>Unknown code characters are skipped (the three-byte sequence is consumed silently).</li>
     * </ul>
     *
     * @param raw the raw translation string, may be {@code null}
     * @return the HTML-ready string, or the original value if no color codes are present
     */
    public static String toHtml(String raw) {
        if (raw == null) return null;

        int idx = raw.indexOf(PREFIX);
        if (idx < 0) return raw; // fast path: no color codes

        StringBuilder sb = new StringBuilder(raw.length() + 64);
        int i = 0;
        int len = raw.length();
        int openCount = 0;

        while (i < len) {
            char ch = raw.charAt(i);
            if (ch == PREFIX && i + 2 < len && raw.charAt(i + 1) == 'c') {
                char code = raw.charAt(i + 2);
                i += 3;

                if (code == '0') {
                    // Reset: close all open colour spans
                    while (openCount > 0) {
                        sb.append("</font>");
                        openCount--;
                    }
                } else {
                    String hex = COLOR_MAP.get(code);
                    if (hex != null) {
                        sb.append("<font color=\"").append(hex).append("\">");
                        openCount++;
                    }
                    // unknown code: skip silently
                }
            } else {
                sb.append(ch);
                i++;
            }
        }

        // Close any spans left open at end of string
        while (openCount > 0) {
            sb.append("</font>");
            openCount--;
        }

        return sb.toString();
    }

    /**
     * Strips all D2R color codes from {@code raw}, returning plain text.
     * Useful when rendering in non-HTML contexts.
     *
     * @param raw the raw translation string, may be {@code null}
     * @return the plain-text string
     */
    public static String strip(String raw) {
        if (raw == null) return null;
        if (raw.indexOf(PREFIX) < 0) return raw;

        StringBuilder sb = new StringBuilder(raw.length());
        int i = 0;
        int len = raw.length();
        while (i < len) {
            char ch = raw.charAt(i);
            if (ch == PREFIX && i + 2 < len && raw.charAt(i + 1) == 'c') {
                i += 3; // skip ÿcX
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }
}

package gomule.d2s;

/**
 * Byte offset constants for the .d2s (Diablo II character save) file format.
 *
 * <p>D2R patch 1.5 introduced file version 105, whose header is <b>16 bytes shorter</b>
 * than the LoD / early-D2R (version 99) layout.  Every structural offset that falls
 * beyond the shortened region must therefore be selected at runtime via the helper
 * methods at the bottom of this class.
 *
 * <p>File layout overview:
 * <pre>
 *  Offset  Size  Field            Notes
 *  ──────  ────  ───────────────  ────────────────────────────────────────────
 *  0       4     magic            0xAA55AA55 (little-endian)
 *  4       4     version          99 = LoD/early-D2R, 105 = D2R 1.5+
 *  8       4     file_size        total byte length of the file
 *  12      4     checksum         CRC32 (field zeroed during calculation)
 *  16      4     weapon_set       active weapon set (0 or 1)
 *
 *  -- version-specific block (LoD offsets shown; v105 = LoD − 16) --
 *  varies  ...   status / class / level / name / merc / Woo! / w4 / gf
 * </pre>
 */
public final class D2SOffsets {

    private D2SOffsets() {}

    // ── Universal header (identical in all versions) ──────────────────────────

    /** 4-byte file magic at the very start of every .d2s file: {@code 0xAA55AA55}. */
    public static final int MAGIC       = 0;

    /**
     * 4-byte file format version.
     * <ul>
     *   <li>99  – LoD / D2R pre-1.5 (item tail-bit format marker {@code 0x69} is set by GoMule)</li>
     *   <li>105 – D2R 1.5+ (header is 16 bytes shorter than v99)</li>
     * </ul>
     */
    public static final int VERSION     = 4;

    /** 4-byte total file length in bytes. Written on save, verified on load. */
    public static final int FILE_SIZE   = 8;

    /** 4-byte CRC32 checksum.  Zeroed out when the checksum is being calculated. */
    public static final int CHECKSUM    = 12;

    /** 4-byte active weapon set field (value 0 or 1). */
    public static final int WEAPON_SET  = 16;

    // ── Version thresholds ────────────────────────────────────────────────────

    /** D2R 1.5+ character file version.  Header is 16 bytes shorter than {@link #VERSION_LOD}. */
    public static final int VERSION_D2R_105 = 105;

    /** LoD / early D2R character file version. */
    public static final int VERSION_LOD     = 99;

    // ── Version-specific offsets: D2R v105 (file version >= 105) ─────────────

    /** Character status flags (ladder, hardcore, expansion …) — D2R v105. */
    public static final int V105_STATUS = 0x14; // 20

    /** Character class byte — D2R v105. */
    public static final int V105_CLASS  = 0x18; // 24

    /** Character level byte — D2R v105. */
    public static final int V105_LEVEL  = 0x1B; // 27

    /** Character name: 16-byte null-padded ASCII string — D2R v105. */
    public static final int V105_NAME   = 0x12B; // 299

    /**
     * Start of the mercenary info block — D2R v105.
     * Layout (relative to this offset):
     * <pre>
     *  +0  1 byte  merc_dead flag
     *  +1  1 byte  (padding / control)
     *  +2  4 bytes merc_id  (non-zero → merc is hired)
     *  +6  2 bytes merc_name_id
     *  +8  2 bytes merc_type (hire table row)
     *  +10 4 bytes merc_experience
     * </pre>
     */
    public static final int V105_MERC   = 161;

    /** Expected absolute byte position of the {@code "Woo!"} act-quests block — D2R v105. */
    public static final int V105_WOO    = 319;

    /** Expected absolute byte position of the {@code "w4"} NPC-state block — D2R v105. */
    public static final int V105_W4     = 698;

    /** Expected absolute byte position of the {@code "gf"} character-stats block — D2R v105. */
    public static final int V105_GF     = 749;

    // ── Version-specific offsets: LoD / D2R < v105 ───────────────────────────

    /** Character status flags — LoD. */
    public static final int LOD_STATUS  = 0x24; // 36

    /** Character class byte — LoD. */
    public static final int LOD_CLASS   = 0x28; // 40

    /** Character level byte — LoD. */
    public static final int LOD_LEVEL   = 0x2B; // 43

    /** Character name: 16-byte null-padded ASCII string — LoD. */
    public static final int LOD_NAME    = 0x10B; // 267

    /** Start of the mercenary info block — LoD. */
    public static final int LOD_MERC    = 177;

    /** Expected absolute byte position of the {@code "Woo!"} act-quests block — LoD. */
    public static final int LOD_WOO     = 335;

    /** Expected absolute byte position of the {@code "w4"} NPC-state block — LoD. */
    public static final int LOD_W4      = 714;

    /** Expected absolute byte position of the {@code "gf"} character-stats block — LoD. */
    public static final int LOD_GF      = 765;

    // ── Post-items section markers (version-independent byte sequences) ───────

    /**
     * Corpse item-list header: {@code JM} (2 bytes) + item count (2 bytes).
     * Always present after character items; count is 0 for a living character.
     */
    public static final byte[] MARKER_CORPSE_JM = {'J', 'M'};

    /** Mercenary section header marker (2 bytes). */
    public static final byte[] MARKER_MERC_JF   = {'j', 'f'};

    /** Mercenary item-list header: {@code JM} (2 bytes) + item count (2 bytes). Present only when merc_id != 0. */
    public static final byte[] MARKER_MERC_JM   = {'J', 'M'};

    /** Golem (iron golem) section marker (2 bytes). */
    public static final byte[] MARKER_GOLEM_KF  = {'k', 'f'};

    // ── Helper: version-conditional offset selection ──────────────────────────

    /** Returns {@code true} if {@code version} corresponds to D2R 1.5+ (v105 header layout). */
    public static boolean isD2Rv105(long version) {
        return version >= VERSION_D2R_105;
    }

    /** Returns the byte offset of the status field for the given file version. */
    public static int statusOffset(long version) {
        return isD2Rv105(version) ? V105_STATUS : LOD_STATUS;
    }

    /** Returns the byte offset of the class field for the given file version. */
    public static int classOffset(long version) {
        return isD2Rv105(version) ? V105_CLASS : LOD_CLASS;
    }

    /** Returns the byte offset of the level field for the given file version. */
    public static int levelOffset(long version) {
        return isD2Rv105(version) ? V105_LEVEL : LOD_LEVEL;
    }

    /** Returns the byte offset of the character name field for the given file version. */
    public static int nameOffset(long version) {
        return isD2Rv105(version) ? V105_NAME : LOD_NAME;
    }

    /** Returns the byte offset of the mercenary info block for the given file version. */
    public static int mercOffset(long version) {
        return isD2Rv105(version) ? V105_MERC : LOD_MERC;
    }

    /** Returns the expected byte position of the {@code "Woo!"} block for the given file version. */
    public static int wooOffset(long version) {
        return isD2Rv105(version) ? V105_WOO : LOD_WOO;
    }

    /** Returns the expected byte position of the {@code "w4"} block for the given file version. */
    public static int w4Offset(long version) {
        return isD2Rv105(version) ? V105_W4 : LOD_W4;
    }

    /** Returns the expected byte position of the {@code "gf"} block for the given file version. */
    public static int gfOffset(long version) {
        return isD2Rv105(version) ? V105_GF : LOD_GF;
    }
}

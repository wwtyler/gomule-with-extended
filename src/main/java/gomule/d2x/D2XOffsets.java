package gomule.d2x;

/**
 * Byte offset constants for the .d2x (ATMA personal stash) file format.
 *
 * <p>The .d2x format is GoMule/ATMA's proprietary single-file stash (not a Blizzard format).
 * The file has a fixed 11-byte header followed immediately by serialised item bytes.
 *
 * <p>File layout:
 * <pre>
 *  Offset  Size  Field       Notes
 *  ──────  ────  ──────────  ──────────────────────────────────────────────────
 *  0       3     magic       ASCII "D2X"
 *  3       2     num_items   16-bit little-endian item count
 *  5       2     version     16-bit little-endian format version (99 = D2R)
 *  7       4     checksum    32-bit rolling checksum (bytes 7-10 zeroed during calc)
 *  11      …     items       contiguous serialised D2Item bytes
 * </pre>
 *
 * <p>Checksum algorithm: for each byte {@code b[i]} in the file, zero bytes 7–10,
 * then accumulate via {@code sum = (sum << 1) | (sum >>> 31)} style rotation plus carry.
 *
 * <p>Version notes:
 * <ul>
 *   <li>Version 99 is the only accepted value; it enables D2R tail-bit item format
 *       ({@code D2Item.sSaveVersion = 0x69}).</li>
 * </ul>
 */
public final class D2XOffsets {

    private D2XOffsets() {}

    // ── File magic ────────────────────────────────────────────────────────────

    /** 3-byte ASCII magic at the start of every .d2x file. */
    public static final String MAGIC_STRING = "D2X";

    /** Byte length of the magic string. */
    public static final int MAGIC_LENGTH    = 3;

    // ── Header field offsets (absolute, from start of file) ──────────────────

    /** Magic "D2X": 3 bytes starting at offset 0. */
    public static final int MAGIC        = 0;

    /** Item count: 2 bytes (16-bit little-endian). */
    public static final int NUM_ITEMS    = 3;

    /**
     * Format version: 2 bytes (16-bit little-endian).
     * Only version {@link #VERSION_D2R} (99) is accepted.
     */
    public static final int VERSION      = 5;

    /**
     * Rolling checksum: 4 bytes.
     * Bytes {@link #CHECKSUM}–{@link #CHECKSUM_END} are treated as 0 during calculation.
     */
    public static final int CHECKSUM     = 7;

    /** Last byte (inclusive) of the checksum field. */
    public static final int CHECKSUM_END = 10;

    /** Byte offset at which item data begins. */
    public static final int ITEMS_START  = 11;

    /** Total header size in bytes. */
    public static final int HEADER_SIZE  = ITEMS_START; // 11

    // ── Field bit widths ──────────────────────────────────────────────────────

    /** Bit width of the {@link #NUM_ITEMS} field. */
    public static final int NUM_ITEMS_BITS  = 16;

    /** Bit width of the {@link #VERSION} field. */
    public static final int VERSION_BITS    = 16;

    /** Bit width of the {@link #CHECKSUM} field. */
    public static final int CHECKSUM_BITS   = 32;

    // ── Version constants ─────────────────────────────────────────────────────

    /**
     * Accepted (and only written) version value.
     * Version 99 implies D2R tail-bit item format ({@code D2Item.sSaveVersion = 0x69}).
     */
    public static final int VERSION_D2R = 99;

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns {@code true} if {@code version} is a supported .d2x version. */
    public static boolean isValidVersion(long version) {
        return version == VERSION_D2R;
    }

    /**
     * Returns {@code true} if items in this stash file use D2R tail-bit format,
     * i.e. {@code D2Item.sSaveVersion} must be set to {@code 0x69} before parsing items.
     */
    public static boolean isD2RItemFormat(long version) {
        return version >= VERSION_D2R;
    }
}

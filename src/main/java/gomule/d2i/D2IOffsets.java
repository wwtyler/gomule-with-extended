package gomule.d2i;

/**
 * Byte offset constants for the .d2i (D2R shared stash) file format.
 *
 * <p>A .d2i file consists of one or more contiguous <em>sections</em>, each beginning
 * with the 4-byte magic {@code 0x55AA55AA}.  All offsets below are <b>relative to the
 * start of the owning section</b>, not to the start of the file.
 *
 * <p>Section header layout (24 bytes minimum):
 * <pre>
 *  Rel.offset  Size  Field          Notes
 *  ──────────  ────  ─────────────  ───────────────────────────────────────────
 *  0           4     magic          0x55AA55AA
 *  4           4     reserved       unknown / padding
 *  8           1     version        96-99 = LoD/early-D2R, 105 = D2R 1.5+
 *  9           3     padding
 *  12          3     gold           gold stored in this pane (24-bit little-endian)
 *  15          1     padding
 *  16          4     length         total section byte length (incl. header)
 *  20          4     section_type   0=stash pane, 1=materials, 2=chronicle/RotW
 *  24         40     (D2R padding)  additional padding present in D2R sections
 *  64          …     JM + items     item list starts here in D2R, at 24 in LoD
 * </pre>
 *
 * <p>Section types:
 * <ul>
 *   <li>0 – Normal stash page: standard {@code JM} item list with grid coordinates.</li>
 *   <li>1 – Materials pane: {@code JM} item list without grid coordinates (D2R 1.5+ only).</li>
 *   <li>2 – Chronicle / RotW metadata: raw bytes, {@code C0EDEA C0} sub-marker, no JM.</li>
 * </ul>
 */
public final class D2IOffsets {

    private D2IOffsets() {}

    // ── Section magic ─────────────────────────────────────────────────────────

    /** 4-byte magic bytes marking the start of each stash section: {@code 0x55AA55AA}. */
    public static final byte[] SECTION_MAGIC = {(byte) 0x55, (byte) 0xAA, (byte) 0x55, (byte) 0xAA};

    // ── Section header field offsets (relative to section start) ─────────────

    /** Magic bytes: 4 bytes. */
    public static final int HDR_MAGIC        = 0;

    /** Reserved / unknown: 4 bytes (skipped on read). */
    public static final int HDR_RESERVED     = 4;

    /**
     * Section format version: 1 byte.
     * <ul>
     *   <li>96–99 – LoD or early D2R; item tail-bit format enabled for version >= {@link #VERSION_ITEM_FORMAT_THRESHOLD}.</li>
     *   <li>105   – D2R 1.5+; section header is 64 bytes (extra 40-byte padding after byte 23).</li>
     * </ul>
     */
    public static final int HDR_VERSION      = 8;

    /** 3 padding bytes following the version byte. */
    public static final int HDR_PAD_AFTER_VER = 9;

    /** Gold stored in this pane: 24 bits (3 bytes, little-endian). */
    public static final int HDR_GOLD         = 12;

    /** 1 padding byte following the gold field. */
    public static final int HDR_PAD_AFTER_GOLD = 15;

    /**
     * Total byte length of this section (including header): 4 bytes.
     * In the D2SharedStashWriter the length is patched as a 24-bit (3-byte) value
     * at this same offset — {@link #HDR_LENGTH_BITS} documents that detail.
     */
    public static final int HDR_LENGTH       = 16;

    /**
     * Bit width used by D2SharedStashWriter when patching the length field.
     * The writer calls {@code patcher.write(newTotalSize, 24)} at byte 16.
     */
    public static final int HDR_LENGTH_BITS  = 24;

    /** Section type identifier: 4 bytes (see class-level javadoc for values). */
    public static final int HDR_SECTION_TYPE = 20;

    // ── Header sizes ──────────────────────────────────────────────────────────

    /**
     * Parsed header size as consumed by {@code D2SharedStash.Header.fromBytes()}.
     * Covers magic through section_type (24 bytes total).
     */
    public static final int HEADER_PARSED_SIZE = 24;

    /**
     * Full on-disk section header size for D2R files (version >= 105).
     * The item list ({@code JM} marker) always begins at this offset within a D2R section.
     */
    public static final int HEADER_D2R_SIZE    = 64;

    /**
     * Byte offset, relative to section start, at which the {@code JM} item-list marker
     * appears in <b>LoD / early-D2R</b> (version &lt; 105) sections.
     */
    public static final int JM_OFFSET_LOD      = HEADER_PARSED_SIZE; // 24

    /**
     * Byte offset, relative to section start, at which the {@code JM} item-list marker
     * appears in <b>D2R 1.5+</b> (version 105) sections.
     */
    public static final int JM_OFFSET_D2R      = HEADER_D2R_SIZE;    // 64

    // ── Section type constants ─────────────────────────────────────────────────

    /** Stash section type: normal inventory page with grid coordinates. */
    public static final int SECTION_TYPE_NORMAL    = 0;

    /** Stash section type: materials / essence page (D2R 1.5+ only). */
    public static final int SECTION_TYPE_MATERIALS = 1;

    /** Stash section type: chronicle / Rite of Walking metadata (raw bytes). */
    public static final int SECTION_TYPE_CHRONICLE = 2;

    // ── Version thresholds ────────────────────────────────────────────────────

    /**
     * Minimum version at which D2R tail-bit item format is used ({@code sSaveVersion = 0x69}).
     * Applies to versions 99 and above.
     */
    public static final int VERSION_ITEM_FORMAT_THRESHOLD = 99;

    /** D2R 1.5+ stash version; implies {@link #HEADER_D2R_SIZE} section headers. */
    public static final int VERSION_D2R_105 = 105;

    /** Valid version values accepted by the reader. */
    public static final int[] VALID_VERSIONS = {96, 97, 98, 99, 105};

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns {@code true} if {@code version} is among the accepted stash section versions. */
    public static boolean isValidVersion(long version) {
        for (int v : VALID_VERSIONS) {
            if (v == version) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if items in this section use D2R tail-bit format
     * (i.e. {@code D2Item.sSaveVersion} must be set to {@code 0x69} before parsing).
     */
    public static boolean isD2RItemFormat(long version) {
        return version >= VERSION_ITEM_FORMAT_THRESHOLD;
    }

    /**
     * Returns the byte offset (relative to section start) where the {@code JM} item-list
     * marker is expected, based on the section version.
     */
    public static int jmOffset(long version) {
        return (version >= VERSION_D2R_105) ? JM_OFFSET_D2R : JM_OFFSET_LOD;
    }
}

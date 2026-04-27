package gomule.d2i;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import gomule.gui.D2ItemListAdapter;
import gomule.item.D2Item;
import gomule.util.D2Backup;
import gomule.util.D2BitReader;
import gomule.util.D2Project;

public class D2SharedStash extends D2ItemListAdapter {
    private final List<D2SharedStashPane> panes;
    private final byte[] originalContent;
    private final D2SharedStashWriter sharedStashWriter;
    private final D2MaterialsPane materialsPane;   // null if no sectionType=1 block
    private final byte[] chronicleRawBytes;        // null if no sectionType=2 block

    /** Backward-compatible 3-arg constructor used by tests (no materials/chronicle). */
    public D2SharedStash(String pFileName, List<D2SharedStashPane> panes, byte[] originalContent) {
        this(pFileName, panes, originalContent, null, null);
    }

    public D2SharedStash(String pFileName, List<D2SharedStashPane> panes, byte[] originalContent,
                         D2MaterialsPane materialsPane, byte[] chronicleRawBytes) {
        super(pFileName);
        this.panes = panes;
        this.originalContent = originalContent;
        this.materialsPane = materialsPane;
        this.chronicleRawBytes = chronicleRawBytes;
        this.sharedStashWriter = new D2SharedStashWriter(pFileName, originalContent);
    }

    public D2SharedStashPane getPane(int index) {
        return panes.get(index);
    }

    public List<D2SharedStashPane> getPanes() {
        return panes;
    }

    @Override
    public boolean containsItem(D2Item pItem) {
        return panes.stream().anyMatch(it -> it.items.contains(pItem));
    }

    @Override
    public void removeItem(D2Item pItem) {
        //Handled by panes
    }

    @Override
    public void addItem(D2Item pItem) {
        //Handled by panes
    }

    @Override
    public List<D2Item> getItemList() {
        return panes.stream().flatMap(it -> it.getItems().stream()).collect(Collectors.toList());
    }

    @Override
    public int getNrItems() {
        return panes.stream().mapToInt(it -> it.items.size()).sum();
    }

    @Override
    public String getFilename() {
        return iFileName;
    }

    @Override
    public boolean isSC() {
        return iFileName.toLowerCase(Locale.forLanguageTag("UTF-8")).contains("softcore");
    }

    @Override
    public boolean isHC() {
        return !isSC();
    }

    @Override
    public void fullDump(PrintWriter pWriter) {
        pWriter.println(iFileName);
        pWriter.println();
        List<D2Item> items = getItemList();
        for (D2Item item : items) {
            item.toWriter(pWriter);
        }
        pWriter.println("Finished: " + iFileName);
        pWriter.println();
    }

    @Override
    protected void saveInternal(D2Project d2Project) {
        if (d2Project != null) D2Backup.backup(d2Project, iFileName, new D2BitReader(originalContent.clone()));
        sharedStashWriter.write(this);
        setModified(false);
    }

    public void replacePane(int paneIndex, D2SharedStashPane newPane) {
        panes.set(paneIndex, newPane);
    }

    public D2MaterialsPane getMaterialsPane() {
        return materialsPane;
    }

    public byte[] getChronicleRawBytes() {
        return chronicleRawBytes;
    }

    public static class D2MaterialsPane {
        private final List<D2Item> items;

        D2MaterialsPane(List<D2Item> items) {
            this.items = new ArrayList<>(items);
        }

        public List<D2Item> getItems() {
            return items;
        }

        /** Removes {@code item} from this materials pane (if present). */
        public void removeItem(D2Item item) {
            items.remove(item);
        }

        /** Adds {@code item} to this materials pane (e.g. when returning a picked-up material). */
        public void addItem(D2Item item) {
            items.add(item);
        }
    }

    public static class D2SharedStashPane {
        private final List<D2Item> items;
        private final D2Item[][] paneGrid;
        private final int gold;

        D2SharedStashPane(List<D2Item> items, D2Item[][] paneGrid, int gold) {
            this.items = items;
            this.paneGrid = paneGrid;
            this.gold = gold;
        }

        public static D2SharedStashPane fromItems(List<D2Item> items, int gold) {
            return new D2SharedStashPane(items, constructPaneGrid(items), gold);
        }

        private static D2Item[][] constructPaneGrid(List<D2Item> items) {
            D2Item[][] grid = new D2Item[16][13];
            for (D2Item item : items) {
                for (int i = item.get_col(); i < (int) item.get_col() + (int) item.get_width(); i++) {
                    for (int j = item.get_row(); j < (int) item.get_row() + (int) item.get_height(); j++) {
                        if (grid[i][j] != null) throw new RuntimeException("Failed to create shared stash pane");
                        grid[i][j] = item;
                    }
                }
            }
            return grid;
        }

        public List<D2Item> getItems() {
            return items;
        }

        public int getGold() {
            return gold;
        }

        public D2Item getItemCovering(int col, int row) {
            return paneGrid[col][row];
        }

        public boolean canDropItem(int col, int row, D2Item item) {
            if (item.isQuestItem()) return false;
            if (col > paneGrid.length - 1 || col < 0 || row > paneGrid[0].length - 1 || row < 0) return false;
            for (int i = col; i < col + item.get_width(); i++) {
                for (int j = row; j < row + item.get_height(); j++) {
                    if (i > paneGrid.length - 1 || j > paneGrid[0].length - 1) return false;
                    if (paneGrid[i][j] != null) return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "D2SharedStashPane{" +
                    "items=" + items +
                    ", paneGrid=" + Arrays.toString(paneGrid) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            D2SharedStashPane that = (D2SharedStashPane) o;
            return Objects.equals(items, that.items) && Arrays.deepEquals(paneGrid, that.paneGrid);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(items);
            result = 31 * result + Arrays.deepHashCode(paneGrid);
            return result;
        }

        public D2SharedStashPane addItem(int col, int row, D2Item item) {
            item.set_col((short) col);
            item.set_row((short) row);
            item.set_location((short) 0);
            item.set_body_position((short) 0);
            item.set_panel((short) 5);
            item.setCharLvl(75);
            List<D2Item> newItems = new ArrayList<>(this.items);
            newItems.add(item);
            return D2SharedStashPane.fromItems(newItems, gold);
        }

        public D2SharedStashPane removeItem(D2Item item) {
            List<D2Item> newItems = new ArrayList<>(this.items);
            newItems.remove(item);
            return D2SharedStashPane.fromItems(newItems, gold);
        }
    }

    static class Header {
        private final long version;
        private final int gold;
        private final long length;
        private final long sectionType;

        public Header(long version, int gold, long length, long sectionType) {
            this.version = version;
            this.gold = gold;
            this.length = length;
            this.sectionType = sectionType;
        }

        public static Header fromBytes(D2BitReader bitReader) {
            // Header layout (relative offsets from section start — see D2IOffsets):
            //   0-7:  magic(4) + reserved(4)  → skipBytes(8)
            //   8:    version (1 byte)
            //   9-11: padding (3 bytes)        → skipBytes(3)
            //   12-14: gold (24 bits)
            //   15:   padding (1 byte)         → skipBytes(1)
            //   16-19: length (32 bits)
            //   20-23: sectionType (32 bits)
            bitReader.skipBytes(D2IOffsets.HDR_VERSION);   // skip to version (offset 8)
            long version = bitReader.read(8);
            bitReader.skipBytes(3);                        // padding after version
            int gold = (int) bitReader.read(24);           // offset 12: gold (3 bytes)
            bitReader.skipBytes(1);                        // padding after gold
            long length = bitReader.read(32);              // offset 16: section length
            long sectionType = bitReader.read(32);         // offset 20: section type (see D2IOffsets.SECTION_TYPE_*)
            return new D2SharedStash.Header(version, gold, length, sectionType);
        }

        public long getVersion() {
            return version;
        }

        public int getGold() {
            return gold;
        }

        public long getLength() {
            return length;
        }

        public long getSectionType() {
            return sectionType;
        }

        @Override
        public String toString() {
            return "Header{" +
                    "version=" + version +
                    ", gold=" + gold +
                    ", length=" + length +
                    ", sectionType=" + sectionType +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;
            return version == header.version && gold == header.gold && length == header.length && sectionType == header.sectionType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, gold, length, sectionType);
        }
    }
}

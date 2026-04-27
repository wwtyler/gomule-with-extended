package gomule.d2i;

import java.util.ArrayList;
import java.util.List;

import gomule.d2i.D2SharedStash.D2SharedStashPane;
import gomule.item.D2Item;
import gomule.util.D2BitReader;

public class D2SharedStashReader {

    static final byte[] STASH_HEADER_START = D2IOffsets.SECTION_MAGIC;

    public D2SharedStash readStash(String filename) throws Exception {
        return readStash(filename, new D2BitReader(filename));
    }

    public D2SharedStash readStash(String filename, D2BitReader bitReader) throws Exception {
        int[] stashHeaderOffsets = bitReader.findBytes(STASH_HEADER_START);
        List<D2SharedStashPane> result = new ArrayList<>();
        D2SharedStash.D2MaterialsPane materialsPane = null;
        byte[] chronicleRawBytes = null;
        for (int stashHeaderOffset : stashHeaderOffsets) {
            // Peek at sectionType (bytes 20-23 of header).
            // sectionType=0: normal stash page (JM + items with x/y grid coords)
            // sectionType=1: materials page (JM + items with advanced_stash_quantity, no grid)
            // sectionType=2: chronicle/RotW metadata (raw bytes, C0 ED EA C0 marker, not JM)
            bitReader.set_byte_pos(stashHeaderOffset);
            D2SharedStash.Header peekHeader = D2SharedStash.Header.fromBytes(bitReader);
            if (peekHeader.getSectionType() == D2IOffsets.SECTION_TYPE_NORMAL) {
                bitReader.set_byte_pos(stashHeaderOffset);
                result.add(readSharedStashPane(bitReader, filename));
            } else if (peekHeader.getSectionType() == D2IOffsets.SECTION_TYPE_MATERIALS) {
                bitReader.set_byte_pos(stashHeaderOffset);
                materialsPane = readMaterialsPane(bitReader, filename);
            } else if (peekHeader.getSectionType() == D2IOffsets.SECTION_TYPE_CHRONICLE) {
                // Chronicle/RotW metadata: preserve as raw bytes for write-back
                int sectionSize = (int) peekHeader.getLength();
                bitReader.set_byte_pos(stashHeaderOffset);
                chronicleRawBytes = bitReader.get_bytes(sectionSize);
            }
        }
        return new D2SharedStash(filename, result, bitReader.getFileContent(), materialsPane, chronicleRawBytes);
    }

    private D2SharedStash.D2MaterialsPane readMaterialsPane(D2BitReader bitReader, String filename) throws Exception {
        D2SharedStash.Header header = D2SharedStash.Header.fromBytes(bitReader);
        long ver = header.getVersion();
        if (!D2IOffsets.isValidVersion(ver))
            throw new RuntimeException("Incorrect materials pane version: " + ver);
        D2Item.sSaveVersion = D2IOffsets.isD2RItemFormat(ver) ? 0x69 : 0;
        bitReader.set_byte_pos(bitReader.findNextFlag("JM", bitReader.get_byte_pos()));
        bitReader.skipBytes(2); // skip "JM" marker
        int numItems = (int) bitReader.read(16);
        List<D2Item> items = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            items.add(new D2Item(filename, bitReader, 75));
        }
        return new D2SharedStash.D2MaterialsPane(items);
    }

    private D2SharedStashPane readSharedStashPane(D2BitReader bitReader, String filename) throws Exception {
        int stashPaneStart = bitReader.get_byte_pos();
        D2SharedStash.Header header = D2SharedStash.Header.fromBytes(bitReader);
        long ver = header.getVersion();
        if (!D2IOffsets.isValidVersion(ver))
            throw new RuntimeException("Incorrect shared stash version: " + ver);
        // D2R 1.5+ tail-bit format for items (must be set before constructing D2Item)
        D2Item.sSaveVersion = D2IOffsets.isD2RItemFormat(ver) ? 0x69 : 0;
        bitReader.set_byte_pos(bitReader.findNextFlag("JM", bitReader.get_byte_pos()));
        bitReader.skipBytes(2);
        int numItems = (int) bitReader.read(16);
        List<D2Item> result = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            result.add(new D2Item(filename, bitReader, 75));
        }
        int calculatedLength = bitReader.get_byte_pos() - stashPaneStart;
        if (calculatedLength != header.getLength())
            throw new RuntimeException("Incorrect shared stash length: " + calculatedLength + " expected: " + header.getLength());
        return D2SharedStashPane.fromItems(result, header.getGold());
    }
}

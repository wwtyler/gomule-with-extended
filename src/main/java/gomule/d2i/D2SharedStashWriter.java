package gomule.d2i;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gomule.d2i.D2IOffsets.HEADER_D2R_SIZE;
import static gomule.d2i.D2IOffsets.SECTION_TYPE_MATERIALS;
import static gomule.d2i.D2IOffsets.SECTION_TYPE_NORMAL;
import static gomule.d2i.D2SharedStashReader.STASH_HEADER_START;
import gomule.item.D2Item;
import gomule.util.D2BitReader;

public class D2SharedStashWriter {
    private final File file;
    private final byte[] originalContent;

    public D2SharedStashWriter(File file, byte[] originalContent) {
        this.file = file;
        this.originalContent = originalContent;
    }

    public D2SharedStashWriter(String filename, byte[] originalContent) {
        this(new File(filename), originalContent);
    }


    public void write(D2SharedStash stash) {
        D2BitReader bitReader = new D2BitReader(originalContent.clone());
        int[] stashHeaderOffsets = bitReader.findBytes(STASH_HEADER_START);
        List<byte[]> stashPanes = new ArrayList<>();
        int normalPaneIndex = 0;
        for (int stashHeaderOffset : stashHeaderOffsets) {
            // Peek at sectionType to decide how to write this block.
            bitReader.set_byte_pos(stashHeaderOffset);
            D2SharedStash.Header peekHeader = D2SharedStash.Header.fromBytes(bitReader);
            if (peekHeader.getSectionType() == SECTION_TYPE_NORMAL) {
                // Normal stash page: re-serialize with updated items
                // NOTE: search AFTER the D2R section header (HEADER_D2R_SIZE bytes) to avoid
                // false-positive JM matches inside the header (e.g. gold or sectionSize bytes).
                stashPanes.add(writeStashPane(stash.getPane(normalPaneIndex++), bitReader, stashHeaderOffset,
                        bitReader.findNextFlag("JM", stashHeaderOffset + HEADER_D2R_SIZE)));
            } else if (peekHeader.getSectionType() == SECTION_TYPE_MATERIALS && stash.getMaterialsPane() != null) {
                // Materials pane: re-serialize with current item bytes so quantity changes are persisted.
                // D2R section headers are always HEADER_D2R_SIZE (64) bytes; JM must start at that offset.
                int sectionStart = stashHeaderOffset;
                bitReader.set_byte_pos(sectionStart);
                byte[] headerBytes = bitReader.get_bytes(HEADER_D2R_SIZE);

                List<D2Item> matItems = stash.getMaterialsPane().getItems();
                int itemPayloadSize = 4; // JM (2) + item count (2)
                for (D2Item item : matItems) itemPayloadSize += item.get_bytes().length;

                int newTotalSize = HEADER_D2R_SIZE + itemPayloadSize;
                byte[] result = new byte[newTotalSize];
                System.arraycopy(headerBytes, 0, result, 0, HEADER_D2R_SIZE);

                // Patch the 24-bit length field at HDR_LENGTH offset.
                D2BitReader patcher = new D2BitReader(result);
                patcher.set_pos(D2IOffsets.HDR_LENGTH * 8);
                patcher.write(newTotalSize, D2IOffsets.HDR_LENGTH_BITS);

                // Write JM + count + item bytes starting at HEADER_D2R_SIZE.
                patcher.set_byte_pos(HEADER_D2R_SIZE);
                patcher.write(19786, 16); // "JM" in D2 bit order
                patcher.write(matItems.size(), 16);
                for (D2Item item : matItems) {
                    byte[] bytes = item.get_bytes();
                    patcher.setBytes(patcher.get_byte_pos(), bytes);
                    patcher.set_byte_pos(patcher.get_byte_pos() + bytes.length);
                }
                stashPanes.add(result);
            } else {
                // SECTION_TYPE_CHRONICLE or materials with no materialsPane: preserve original bytes unchanged
                int sectionSize = (int) peekHeader.getLength();
                bitReader.set_byte_pos(stashHeaderOffset);
                stashPanes.add(bitReader.get_bytes(sectionSize));
            }
        }
        writeToFile(stashPanes);
    }

    private void writeToFile(List<byte[]> stashPanes) {
        D2BitReader bitWriter = new D2BitReader(new byte[0]);
        bitWriter.set_byte_pos(0);
        bitWriter.setBytes(concatenate(stashPanes));
        bitWriter.save(file.getAbsolutePath());
    }

    private byte[] writeStashPane(D2SharedStash.D2SharedStashPane pane, D2BitReader bitReader, int stashHeaderOffset, int itemListStartOffset) {
        bitReader.set_byte_pos(stashHeaderOffset);
        int itemByteLength = pane.getItems().stream().map(it -> it.get_bytes().length).reduce(4, (a, b) -> a + b);
        byte[] oldHeaderBytes = bitReader.get_bytes(itemListStartOffset - stashHeaderOffset);
        D2BitReader writer = new D2BitReader(new byte[oldHeaderBytes.length + itemByteLength]);
        writer.setBytes(0, oldHeaderBytes);
        writeHeader(pane, writer, writer.get_length());
        writer.set_byte_pos(itemListStartOffset - stashHeaderOffset);
        writeItemBytes(pane, writer);
        return writer.getFileContent();
    }

    public void writeHeader(D2SharedStash.D2SharedStashPane pane, D2BitReader bitWriter, long length) {
        bitWriter.skipBytes(D2IOffsets.HDR_VERSION);  // skip to version field (offset 8)
        long version = bitWriter.read(8);
        if (!D2IOffsets.isValidVersion(version))
            throw new RuntimeException("Overwriting wrong version stash: " + version);
        bitWriter.skipBytes(3);
        bitWriter.write(pane.getGold(), 24);
        bitWriter.skipBytes(1);
        bitWriter.write(length, D2IOffsets.HDR_LENGTH_BITS);
    }

    private void writeItemBytes(D2SharedStash.D2SharedStashPane pane, D2BitReader writer) {
        writer.write(19786, 16);
        List<D2Item> items = pane.getItems();
        writer.write(items.size(), 16);
        for (D2Item item : items) {
            byte[] bytesToWrite = item.get_bytes();
            writer.setBytes(writer.get_byte_pos(), bytesToWrite);
            writer.set_byte_pos(writer.get_byte_pos() + bytesToWrite.length);
        }
    }

    private byte[] concatenate(List<byte[]> panes) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (byte[] pane : panes) {
                outputStream.write(pane);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

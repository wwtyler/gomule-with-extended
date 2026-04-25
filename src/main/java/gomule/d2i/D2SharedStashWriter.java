package gomule.d2i;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        for (int i = 0; i < stashHeaderOffsets.length; i++) {
            stashPanes.add(writeStashPane(stash.getPane(i), bitReader, stashHeaderOffsets[i], bitReader.findNextFlag("JM", stashHeaderOffsets[i])));
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
        bitWriter.skipBytes(8);
        long version = bitWriter.read(8);
        // D2 LoD = 99, D2R 使用 99/105 等
        if (version != 99 && version != 105 && version != 96 && version != 97 && version != 98)
            throw new RuntimeException("Overwriting wrong version stash: " + version);
        bitWriter.skipBytes(3);
        bitWriter.write(pane.getGold(), 24);
        bitWriter.skipBytes(1);
        bitWriter.write(length, 24);
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

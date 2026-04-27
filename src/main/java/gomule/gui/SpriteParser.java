package gomule.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

/**
 * Parses D2R .sprite files into BufferedImages.
 *
 * Format (version 31 = raw RGBA, no compression):
 *   offset 0x04: version (uint16 LE) — must be 31
 *   offset 0x08: width  (int32 LE)
 *   offset 0x0C: height (int32 LE)
 *   offset 0x28: raw RGBA pixel data (width × height × 4 bytes)
 */
public class SpriteParser {

    private static final int VERSION_RGBA = 31;
    private static final int RGBA_DATA_OFFSET = 0x28;

    /** Parse a .sprite file. Returns null if unsupported format or invalid data. */
    public static BufferedImage parse(File file) {
        if (file == null || !file.isFile()) return null;
        try {
            return parse(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    /** Parse raw .sprite bytes. Returns null if unsupported format or invalid data. */
    public static BufferedImage parse(byte[] data) {
        if (data == null || data.length < RGBA_DATA_OFFSET + 4) return null;

        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int version = buf.getShort(0x04) & 0xFFFF;
        if (version != VERSION_RGBA) {
            // DXT5 (version=61) and others not yet supported
            return null;
        }

        int width  = buf.getInt(0x08);
        int height = buf.getInt(0x0C);
        if (width <= 0 || height <= 0) return null;

        int expectedDataLen = RGBA_DATA_OFFSET + width * height * 4;
        if (data.length < expectedDataLen) return null;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int src = RGBA_DATA_OFFSET;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = data[src++] & 0xFF;
                int g = data[src++] & 0xFF;
                int b = data[src++] & 0xFF;
                int a = data[src++] & 0xFF;
                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }
}

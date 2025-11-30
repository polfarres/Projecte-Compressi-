package io;

import image.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OutputImageWriter {

    public static void writeRaw(Image image) throws IOException {

        String path = new File(image.imagePath, image.name).getPath();

        path = path.startsWith("/") ? path.substring(1) : path;
        File file = new File(path).getAbsoluteFile();

        // 1) Create parent directory if it doesn't exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create directory: " + parent.getAbsolutePath());
            }
        }

        // 2) Create file if it does not exist
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Cannot create file: " + file.getAbsolutePath());
            }
        }

        // 3) Now safely write to it
        try (FileOutputStream fos = new FileOutputStream(file)) {

            int bytesPerSample = (image.bitsPerSample == 8) ? 1 : 2;
            int totalSize = image.width * image.height * image.bands * bytesPerSample;

            ByteBuffer bufferOut = ByteBuffer.allocate(totalSize);
            bufferOut.order(image.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

            for (int b = 0; b < image.bands; b++) {
                for (int y = 0; y < image.height; y++) {
                    for (int x = 0; x < image.width; x++) {

                        if (image.bitsPerSample == 8) {
                            int value = image.img[b][y][x];
                            bufferOut.put((byte) (value & 0xFF));

                        } else if (image.bitsPerSample == 16) {
                            bufferOut.putShort((short) image.img[b][y][x]);

                        } else {
                            throw new IllegalArgumentException("bitsPerSample no soportado: " + image.bitsPerSample);
                        }
                    }
                }
            }

            fos.write(bufferOut.array());
        }
    } //✅

    public static void writeCompressedImage(Image image) throws IOException {

    }
}

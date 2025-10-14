package io;

import config.RawImageConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RawImageWriter {
    public static void write(BufferedImage image, String format, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        ImageIO.write(image, format, outputFile);
    }

    public static void writeRaw(String path, short[][][] matrix, RawImageConfig config) throws IOException {


        try (FileOutputStream fos = new FileOutputStream(path)) {
            // Si los datos son de 8 bits, cada muestra ocupa 1 byte
            int bytesPerSample = (config.bitsPerSample == 8) ? 1 : 2; // por si luego usas 32 bits

            // Tamaño total del buffer
            int totalSize = config.width * config.height * config.bands * bytesPerSample;
            ByteBuffer bufferOut = ByteBuffer.allocate(totalSize);
            bufferOut.order(config.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

            // Escribimos los datos según la profundidad de bits
            for (int b = 0; b < config.bands; b++) {
                for (int y = 0; y < config.height; y++) {
                    for (int x = 0; x < config.width; x++) {

                        if (config.bitsPerSample == 8) {
                            // Si los valores están en 0–255
                            int value = matrix[b][y][x];
                            bufferOut.put((byte) (value & 0xFF));
                        } else if (config.bitsPerSample == 16) {
                            // Si son flotantes o ints de 32 bits
                            bufferOut.putShort(matrix[b][y][x]);
                        } else {
                            throw new IllegalArgumentException("bitsPerSample no soportado: " + config.bitsPerSample);
                        }
                    }
                }
            }

            // Escribimos al archivo
            fos.write(bufferOut.array());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

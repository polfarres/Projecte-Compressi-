package io;

import image.Image;
import stages.ArithmeticCoder;
import utils.BitReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class InputImageReader {

    public static short[][][] readRaw(String path, int width, int height, int bands, int bitsPerSample,
                                      boolean signed, boolean bigEndian) throws IOException {
        int numPixels = width * height * bands;
        int bytesPerSample = bitsPerSample / 8;
        int totalBytes = numPixels * bytesPerSample;
        short[][][] matrix = new short[bands][height][width];
        byte[] buffer = new byte[totalBytes];

        //El nombree path no puede empezar con /, en caso de que lo tenga se le elimina
        path = path.startsWith("/") ? path.substring(1) : path;
        File f = new File(path);

        try (FileInputStream fis = new FileInputStream(f.getAbsolutePath())) {
            int bytesRead = fis.read(buffer);
            if (bytesRead != totalBytes) {
                throw new IOException("La mida del fitxer no coincideix amb la configuració.");
            }
        }


        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        if (bitsPerSample == 8) {

            for (int b = 0; b < bands; b++)
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++) {
                        matrix[b][y][x] = buffer[b * (width * height) + y * width + x];
                    }

        } else {
            for (int b = 0; b < bands; b++)
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++) {
                        matrix[b][y][x] = bb.getShort(); // o getInt(), getShort(), según el tipo
                    }

        }


        return matrix;
    } //✅

    public static byte[] readAC(Image image) {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(image.imagePath)))) {

            // 1. LEER HEADER
            // Recuperamos dimensiones, Q y el histograma original
            image.readHeader(dis);

            // 2. RECONSTRUIR FRECUENCIAS ACUMULADAS
            // Convertimos el histograma int[] a la lista acumulada que necesita el ArithmeticCoder
            List<Integer> cumFreq = new ArrayList<>();
            int currentSum = 0;
            cumFreq.add(0); // El inicio siempre es 0

            for (int freq : image.frequencies) {
                currentSum += freq;
                cumFreq.add(currentSum);
            }

            // 3. LEER BITSTREAM (El resto del archivo)
            return dis.readAllBytes();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

        if (bitsPerSample == 8){

            for (int b = 0; b < bands; b++)
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++) {
                        matrix[b][y][x] = buffer[b * (width * height) + y * width + x];
                    }

        }else{
            for (int b = 0; b < bands; b++)
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++) {
                        matrix[b][y][x] = bb.getShort(); // o getInt(), getShort(), según el tipo
                    }

        }



        return matrix;
    } //✅


}

package io;

import config.RawImageConfig;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RawImageReader {

    public static short[][][] readRaw(String path, RawImageConfig config) throws IOException {
        int numPixels = config.width * config.height * config.bands;
        int bytesPerSample = config.bitsPerSample / 8;
        int totalBytes = numPixels * bytesPerSample;
        short[][][] matrix = new short[config.bands][config.height][config.width];
        byte[] buffer = new byte[totalBytes];

        try (FileInputStream fis = new FileInputStream(path)) {
            int bytesRead = fis.read(buffer);
            if (bytesRead != totalBytes) {
                throw new IOException("La mida del fitxer no coincideix amb la configuració.");
            }
        }


        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(config.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        if (config.bitsPerSample == 8){

            for (int b = 0; b < config.bands; b++)
                for (int y = 0; y < config.height; y++)
                    for (int x = 0; x < config.width; x++) {
                        matrix[b][y][x] = buffer[b * (config.width * config.height) + y * config.width + x];
                    }

        }else{
            for (int b = 0; b < config.bands; b++)
                for (int y = 0; y < config.height; y++)
                    for (int x = 0; x < config.width; x++) {
                        matrix[b][y][x] = bb.getShort(); // o getInt(), getShort(), según el tipo
                    }

        }



        return matrix;
    }
}

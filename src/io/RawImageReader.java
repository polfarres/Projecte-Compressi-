package io;

import config.RawImageConfig;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import config.RawImageConfig;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

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

    public static int[][][] readRawInt(String filePath, RawImageConfig config) throws IOException {

        int numBandes = config.bands;
        int alçada = config.height;
        int amplada = config.width;

        // Inicialitzem la matriu de residus (que són int)
        int[][][] data = new int[numBandes][alçada][amplada];

        // Utilitzem DataInputStream per llegir enters (4 bytes)
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(filePath)))) {

            // Recorrem la matriu per carregar les dades en el mateix ordre que es van escriure
            for (int b = 0; b < numBandes; b++) {
                for (int y = 0; y < alçada; y++) {
                    for (int x = 0; x < amplada; x++) {

                        // Llegeix un enter (4 bytes) del flux i l'assigna.
                        // El mètode readInt() ja gestiona l'ordre de bytes.
                        data[b][y][x] = dis.readInt();
                    }
                }
            }

        } // El 'try-with-resources' tanca automàticament el DataInputStream

        // Opcional: Comprovació de fi de fitxer
        // Si el fitxer encara conté dades aquí, indicaria un error de format.
        // Amb readInt(), si no hi ha prou bytes, es llança una EOFException.

        return data;
    }
}

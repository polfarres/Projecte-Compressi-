package config;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RawImageConfig {
    public int width;
    public int height;
    public int bands;
    public int bitsPerSample;
    public boolean signed;
    public boolean bigEndian;

    // --- Camps per a la compressió ---
    public int qStep;            // El pas de quantització (Q)
    public short[] frequencies;  // Taula de freqüències (Histograma)

    public RawImageConfig(int width, int height, int bands, int bitsPerSample,
                          boolean signed, boolean bigEndian) {
        this.width = width;
        this.height = height;
        this.bands = bands;
        this.bitsPerSample = bitsPerSample;
        this.signed = signed;
        this.bigEndian = bigEndian;
    }

    /**
     * Configura les dades necessàries per escriure la capçalera de compressió.
     */
    public void setCompressionHeaderData(int qStep, short[] frequencies) {
        this.qStep = qStep;
        this.frequencies = frequencies;
    }

    /**
     * Escriu el HEADER binari al flux de sortida (Encoder).
     * @param dos El flux de dades de sortida.
     */
    public void writeHeader(DataOutputStream dos) throws IOException {
        // 1. Metadades Imatge
        dos.writeInt(width);
        dos.writeInt(height);
        dos.writeInt(bands);
        dos.writeInt(bitsPerSample);
        dos.writeBoolean(signed);
        dos.writeBoolean(bigEndian);

        // 2. Paràmetres de compressió
        dos.writeInt(qStep);

        // 3. Taula de freqüències (Histograma)
        if (frequencies != null) {
            dos.writeInt(frequencies.length); // Escrivim la mida de l'array
            for (short freq : frequencies) {
                dos.writeShort(freq);         // Escrivim cada freqüència
            }
        } else {
            dos.writeInt(0);
        }
    }

    /**
     * Llegeix el HEADER binari d'un flux d'entrada i crea l'objecte de configuració (Decoder).
     * @param dis El flux de dades d'entrada.
     * @return L'objecte RawImageConfig carregat.
     */
    public static RawImageConfig readHeader(DataInputStream dis) throws IOException {
        // 1. Llegir metadades bàsiques
        int width = dis.readInt();
        int height = dis.readInt();
        int bands = dis.readInt();
        int bitsPerSample = dis.readInt();
        boolean signed = dis.readBoolean();
        boolean bigEndian = dis.readBoolean();

        RawImageConfig config = new RawImageConfig(width, height, bands, bitsPerSample, signed, bigEndian);

        // 2. Llegir paràmetres de compressió
        config.qStep = dis.readInt();

        // 3. Llegir taula de freqüències
        int freqLength = dis.readInt();
        if (freqLength > 0) {
            config.frequencies = new short[freqLength];
            for (int i = 0; i < freqLength; i++) {
                config.frequencies[i] = dis.readShort();
            }
        }

        return config;
    }
}
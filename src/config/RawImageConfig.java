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
    public int qStep;
    public int[] frequencies;  // ARRAY D'ENTERS

    public RawImageConfig(int width, int height, int bands, int bitsPerSample,
                          boolean signed, boolean bigEndian) {
        this.width = width;
        this.height = height;
        this.bands = bands;
        this.bitsPerSample = bitsPerSample;
        this.signed = signed;
        this.bigEndian = bigEndian;
    }

    public void setCompressionHeaderData(int qStep, int[] frequencies) {
        this.qStep = qStep;
        this.frequencies = frequencies;
    }

    public void writeHeader(DataOutputStream dos) throws IOException {
        dos.writeInt(width);
        dos.writeInt(height);
        dos.writeInt(bands);
        dos.writeInt(bitsPerSample);
        dos.writeBoolean(signed);
        dos.writeBoolean(bigEndian);

        dos.writeInt(qStep);

        // --- CORRECCIÓ CRÍTICA ---
        // Fem servir writeInt (4 bytes) perquè les freqüències poden ser grans (fins a 2M)
        if (frequencies != null) {
            dos.writeInt(frequencies.length);
            for (int freq : frequencies) {
                dos.writeInt(freq); // writeInt, NO writeShort
            }
        } else {
            dos.writeInt(0);
        }
    }

    public static RawImageConfig readHeader(DataInputStream dis) throws IOException {
        int width = dis.readInt();
        int height = dis.readInt();
        int bands = dis.readInt();
        int bitsPerSample = dis.readInt();
        boolean signed = dis.readBoolean();
        boolean bigEndian = dis.readBoolean();

        RawImageConfig config = new RawImageConfig(width, height, bands, bitsPerSample, signed, bigEndian);

        config.qStep = dis.readInt();

        // --- CORRECCIÓ CRÍTICA ---
        // Fem servir readInt (4 bytes)
        int freqLength = dis.readInt();
        if (freqLength > 0) {
            config.frequencies = new int[freqLength];
            for (int i = 0; i < freqLength; i++) {
                config.frequencies[i] = dis.readInt(); // readInt, NO readShort
            }
        }

        return config;
    }
}
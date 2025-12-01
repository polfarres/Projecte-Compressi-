package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Traducció de la classe BitWriter de C++ a Java.
 * Permet escriure dades bit a bit i emmagatzemar-les en una llista de bytes (buffer).
 */
public class BitWriter {
    private final List<Byte> buffer;
    private int curByte = 0; // Utilitzem int per l'acumulació, fins que es guarda com a byte
    private int bitPos = 0;

    public BitWriter() {
        this.buffer = new ArrayList<>();
    }

    public void writeBit(int bit) {
        // En C++: cur_byte = (cur_byte << 1) | (bit & 1);
        curByte = (curByte << 1) | (bit & 1);
        bitPos++;
        if (bitPos == 8) {
            buffer.add((byte) curByte);
            curByte = 0;
            bitPos = 0;
        }
    }

    public void flush() {
        if (bitPos > 0) {
            // Desplaçar a l'esquerra per omplir amb zeros els bits restants
            curByte <<= (8 - bitPos);
            buffer.add((byte) curByte);
            curByte = 0;
            bitPos = 0;
        }
    }

    public byte[] getBuffer() {
        // Converteix la llista de Bytes a un array de bytes
        byte[] result = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); i++) {
            result[i] = buffer.get(i);
        }
        return result;
    }
    public int getSize() {
        return buffer.size();
    }

    public static double calculatePSNR(double mse, int bitsPerSample) {
        if (mse == 0) return Double.POSITIVE_INFINITY;

        // El valor màxim possible (255 per a 8 bits, 65535 per a 16 bits)
        double maxPixelValue = Math.pow(2, bitsPerSample) - 1;

        return 10 * Math.log10((maxPixelValue * maxPixelValue) / mse);
    }
}
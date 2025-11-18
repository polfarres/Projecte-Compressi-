package io;

/**
 * Traducció de la classe BitReader de C++ a Java.
 * Permet llegir dades bit a bit d'un buffer de bytes.
 */
public class BitReader {
    private final byte[] buffer;
    private int bytePos = 0;
    private int bitPos = 0;

    public BitReader(byte[] buf) {
        this.buffer = buf;
    }

    /**
     * Comprova si hem arribat al final del buffer.
     */
    public boolean hasMore() {
        return bytePos < buffer.length;
    }

    /**
     * Llegeix el següent bit.
     * @return 0 o 1. Retorna 0 si s'arriba al final del buffer.
     */
    public int readBit() {
        if (bytePos >= buffer.length) return 0;

        // C++: (buffer[byte_pos] >> (7 - bit_pos)) & 1;
        // Utilitzem & 0xFF per assegurar que llegim el byte sense signe (com a int)
        int currentByte = buffer[bytePos] & 0xFF;

        int bit = (currentByte >> (7 - bitPos)) & 1;

        bitPos++;
        if (bitPos == 8) {
            bitPos = 0;
            bytePos++;
        }
        return bit;
    }
}
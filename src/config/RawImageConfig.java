package config;

public class RawImageConfig {
    public int width;
    public int height;
    public int bands;          // 1 = gris, 3 = RGB, altres = dades especials
    public int bitsPerSample;  // 8 o 16 (altres complicat en Java)bue
    public boolean signed;     // true = signed, false = unsigned
    public boolean bigEndian;  // true = big endian, false = little endian

    public RawImageConfig(int width, int height, int bands, int bitsPerSample,
                          boolean signed, boolean bigEndian) {
        this.width = width;
        this.height = height;
        this.bands = bands;
        this.bitsPerSample = bitsPerSample;
        this.signed = signed;
        this.bigEndian = bigEndian;
    }
}

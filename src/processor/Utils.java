package processor;

import config.RawImageConfig;

public class Utils {
    public static RawImageConfig parseConfigFromFilename(String filename) {
        // Exemple simple: n1_GRAY.ube8_1_2560_2048.raw
        String name = filename.split("\\.")[1]; // "ube8_1_2560_2048"
        String[] parts = name.split("_");

        int bits = Integer.parseInt(parts[0].replaceAll("\\D", ""));  // "ube8" -> 8
        int bands = Integer.parseInt(parts[1]);
        int height = Integer.parseInt(parts[2]);
        int width = Integer.parseInt(parts[3]);

        boolean signed = parts[0].contains("s");   // conté "s" -> signed
        boolean bigEndian = parts[0].contains("be"); // conté "be" -> big endian

        return new RawImageConfig(width, height, bands, bits, signed, bigEndian);
    }
}

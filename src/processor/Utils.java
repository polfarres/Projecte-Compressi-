package processor;


import config.RawImageConfig;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

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

    public static void printMatrixToFile(short[][][] data, String outputPath, String description) {
        if (data == null || data.length == 0) {
            System.out.println("⚠️ Matriu buida o nul·la: " + description + ". No es crearà el fitxer.");
            return;
        }

        int numBandes = data.length;

        // Utilitzem 'try-with-resources' per assegurar-nos que els recursos es tanquin automàticament
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {

            writer.println("--- " + description + " (" + numBandes + " bandes) ---");

            // Iterem sobre totes les bandes
            for (int b = 0; b < numBandes; b++) {
                int alçada = data[b].length;
                if (alçada == 0) continue;

                int amplada = data[b][0].length;

                writer.println("\n[Banda " + (b + 1) + " / " + numBandes + " | Mida: " + alçada + "x" + amplada + "]");

                // Iterem sobre totes les files
                for (int y = 0; y < alçada; y++) {
                    StringBuilder row = new StringBuilder();

                    // Iterem sobre totes les columnes
                    for (int x = 0; x < amplada; x++) {
                        // Utilitzem format fixe (%6d) i afegim un espai
                        row.append(String.format("%6d", data[b][y][x]));
                    }
                    writer.println(row.toString());
                }
            }

            System.out.println("✅ Les dades s'han guardat amb èxit a: " + outputPath);

        } catch (IOException e) {
            System.err.println("❌ ERROR d'Escriptura: No s'ha pogut guardar la matriu a " + outputPath);
            e.printStackTrace();
        }
    }

    public static String stripPrefixes(String fileName) {
        // Definir els prefixos que el teu projecte afegeix
        String[] prefixes = {"r_", "p_", "Q_3_", "sortida"};

        for (String prefix : prefixes) {
            if (fileName.startsWith(prefix)) {
                // Eliminar el prefix i qualsevol guió o subratllat immediat
                fileName = fileName.substring(prefix.length());
                // Si el nom era R_Q_IMG.raw, ara pot començar per Q_IMG.raw, ho tornem a provar al bucle.
            }
        }
        return fileName;
    }
}

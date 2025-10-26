package processor;

import config.RawImageConfig;
import io.RawImageReader;
import io.RawImageWriter;

import java.io.File;
import java.util.Map;

import static processor.Utils.parseConfigFromFilename;

public class QuantitzationProcess {
    public static void quanticiseRoundingAll(Map<String, short[][][]> Images, int q, String outputPath) {

        // Crear carpeta de salida si no existe
        File folder = new File(outputPath);
        if (!folder.exists()) folder.mkdirs();

        String outputFolder = outputPath;
        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String imageName = entry.getKey();
            short[][][] img = entry.getValue();
            RawImageConfig config = parseConfigFromFilename(imageName);


            try {
                String outputNameRAw = "Q_" + q + "_" + imageName;

                // ----- Quantizice function -----
                for (int b = 0; b < img.length; b++) { // bands
                    for (int x = 0; x < img[b].length; x++) { // width
                        for (int y = 0; y < img[b][x].length; y++) { // height
                            short val = img[b][x][y];
                            // Aplicar cuantización por redondeo
                            int quantized = (int) Math.round((double) val / q) * q;

                            img[b][x][y] = (short) quantized;
                        }
                    }
                }
                // -----

                System.out.println("Image " + imageName + " reduced.");
                RawImageWriter.writeRaw(new File(outputFolder, outputNameRAw).getAbsolutePath(), img, config);


            } catch (Exception e) {
                System.err.println("Error processant: " + imageName);
                e.printStackTrace();
            }
        }
    }

    public static void deQuanticiseRoundingAll(String inputPath, String outputPath) {
        String outputFolder = outputPath + "Q";
        File inputFolder = new File(inputPath);

        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));

        if (files == null) {
            System.out.println("No s'han trobat fitxers RAW a " + inputFolder.getAbsolutePath());
            return;
        }

        // Crear carpeta de salida si no existe
        File folder = new File(outputFolder);
        if (!folder.exists()) folder.mkdirs();

        for (File file : files) {
            try {
                // === Leer Q desde el nombre del archivo ===
                // Ejemplo: Q_100_filename.raw  → Q = 100
                String[] parts = file.getName().split("_");
                int q = 1; // valor por defecto si no se puede leer
                if (parts.length > 1 && parts[0].equalsIgnoreCase("Q")) {
                    try {
                        q = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                        System.err.println("No s'ha pogut llegir Q del fitxer: " + file.getName());
                    }
                }
                // ==========================================

                RawImageConfig config = parseConfigFromFilename(file.getName());
                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);

                // ----- DeQuanticise -----
                for (int b = 0; b < img.length; b++) { // bands
                    for (int x = 0; x < img[b].length; x++) { // width
                        for (int y = 0; y < img[b][x].length; y++) { // height
                            short val = img[b][x][y];

                            // Si los valores eran índices, multiplicar por q
                            int dequantized = val * q;

                            img[b][x][y] = (short) dequantized;
                        }
                    }
                }
                // -----

                String outputNameRAw = "de" + file.getName();
                System.out.println("Image " + file.getName() + " processed. (Q=" + q + ")");
                RawImageWriter.writeRaw(new File(outputFolder, outputNameRAw).getAbsolutePath(), img, config);

            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }
    }



    public static void quanticiseDeadZoneAll(Map<String, short[][][]> Images, int q) {
        //Work in progress
    }
}

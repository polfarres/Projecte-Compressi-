package processor;

import config.RawImageConfig;
import io.RawImageReader;
import io.RawImageWriter;

import java.io.File;
import java.util.Map;

import static processor.Utils.parseConfigFromFilename;

public class QuantitzationProcess {
    public static void quanticiseRoundingAll(Map<String, short[][][]> Images, int q, String outputPath) {


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

                            // Limitar el valor al rango del tipo short (-32768 a 32767)
                            if (quantized > Short.MAX_VALUE) quantized = Short.MAX_VALUE;
                            if (quantized < Short.MIN_VALUE) quantized = Short.MIN_VALUE;

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

    public static void deQuanticiseRoundingAll(int q, String inputPath, String outputPath) {
        String outputFolder = outputPath + "/Round_deQ_" + q + "_";
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
                RawImageConfig config = parseConfigFromFilename(file.getName());
                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);

                // ----- DeQuanticise -----
                for (int b = 0; b < img.length; b++) { // bands
                    for (int x = 0; x < img[b].length; x++) { // width
                        for (int y = 0; y < img[b][x].length; y++) { // height
                            short val = img[b][x][y];
                            // En caso de usar índices, multiplicar por q
                            // En este caso los valores ya están cuantizados, así que:
                            int dequantized = val;

                            // Si hubieras almacenado índices: dequantized = val * q;

                            // Asegurarse del rango short
                            if (dequantized > Short.MAX_VALUE) dequantized = Short.MAX_VALUE;
                            if (dequantized < Short.MIN_VALUE) dequantized = Short.MIN_VALUE;

                            img[b][x][y] = (short) dequantized;
                        }
                    }
                }
                // -----

                String outputNameRAw = "deQ_" + q + "_" + file.getName();
                System.out.println("Image " + file.getName() + " processed.");
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

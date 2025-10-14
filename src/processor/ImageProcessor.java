package processor;

import config.RawImageConfig;
import io.RawImageReader;
import io.RawImageWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageProcessor {

    private final File inputFolder;
    private final File outputFolder;
    private double totalEntropy;

    public ImageProcessor(String inputPath, String outputPath) {
        this.inputFolder = new File(inputPath);
        this.outputFolder = new File(outputPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        this.totalEntropy = 0;

    }

    public void processReadAndWrite() {

        // .listFiles --> funció anònima que accepta tots els fitxers acabats en .raw (primer els passa a minus)
        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));



        if (files == null) {
            System.out.println("No s'han trobat fitxers RAW a " + inputFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                RawImageConfig config = parseConfigFromFilename(file.getName());

                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);

                String outputNameRAw = "sortida" + file.getName();
                RawImageWriter.writeRaw(new File(outputFolder, outputNameRAw).getAbsolutePath(), img, config);


            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public void processEntropy() {
        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));

        for (File file : files) {
            try {
                RawImageConfig config = parseConfigFromFilename(file.getName());

                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);

                Map<Short, Double> probabilities = calculateProbability(img);

                // TODO: FUNCIÓ QUE CALCULI L'ENTROPIA TOTAL
                double imageEntropy = calculateImageEntropy(probabilities);
                this.totalEntropy += imageEntropy;

                System.out.println("Entropia total de " + file.getName() + ": " + imageEntropy + " bits");

            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }
        System.out.println("Entropia total de totes les imatges són: " + this.totalEntropy + " bits");
    }

    private RawImageConfig parseConfigFromFilename(String filename) {
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

    private Map<Short, Double> calculateProbability(short[][][] img) {
        Map<Short, Integer> countMap = new HashMap<>();
        int total = 0;

        // Comptar aparicions
        for (int b = 0; b < img.length; b++) {
            for (int y = 0; y < img[b].length; y++) {
                for (int x = 0; x < img[b][y].length; x++) {
                    short val = img[b][y][x];
                    countMap.put(val, countMap.getOrDefault(val, 0) + 1);
                    total++;
                }
            }
        }

        // Calcular probabilitats
        Map<Short, Double> probMap = new HashMap<>();
        for (Map.Entry<Short, Integer> entry : countMap.entrySet()) {
            probMap.put(entry.getKey(), entry.getValue() / (double) total);
        }

        return probMap;
    }

    private double calculateImageEntropy(Map<Short,Double> probabilities) {
        double entropy = 0;
        for (double p : probabilities.values()) {
            if (p > 0) {
                entropy += -p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }

}

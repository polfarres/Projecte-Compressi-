package processor;

import config.RawImageConfig;
import io.RawImageReader;
import io.RawImageWriter;
import processor.entropy.EntropyCalculator;
import processor.entropy.EntropyOrder0;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {

    private final File inputFolder;
    private final File outputFolder;
    private final List<Double> totalEntropy;

    public ImageProcessor(String inputPath, String outputPath) {
        this.inputFolder = new File(inputPath);
        this.outputFolder = new File(outputPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        this.totalEntropy = new ArrayList<>();
    }

    public void processReadAndWrite() {
        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));

        if (files == null) {
            System.out.println("No s'han trobat fitxers RAW a " + inputFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                RawImageConfig config = parseConfigFromFilename(file.getName());
                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);

                String outputNameRaw = "sortida_" + file.getName();
                RawImageWriter.writeRaw(new File(outputFolder, outputNameRaw).getAbsolutePath(), img, config);

            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public void processEntropy() {
        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));

        if (files == null) {
            System.out.println("No s'han trobat fitxers RAW a " + inputFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                RawImageConfig config = parseConfigFromFilename(file.getName());
                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);

                EntropyCalculator entropyCalc = new EntropyOrder0(); //EntropyOrder1()
                double imageEntropy = entropyCalc.calculate(img);

                System.out.println("Entropia total de " + file.getName() + ": " + imageEntropy + " bits");
                this.totalEntropy.add(imageEntropy);

            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }

    }

    private RawImageConfig parseConfigFromFilename(String filename) {
        // Exemple simple: n1_GRAY.ube8_1_2560_2048.raw
        String name = filename.split("\\.")[1]; // "ube8_1_2560_2048"
        String[] parts = name.split("_");

        int bits = Integer.parseInt(parts[0].replaceAll("\\D", ""));  // "ube8" -> 8
        int bands = Integer.parseInt(parts[1]);
        int height = Integer.parseInt(parts[2]);
        int width = Integer.parseInt(parts[3]);

        boolean signed = parts[0].contains("s");
        boolean bigEndian = parts[0].contains("be");

        return new RawImageConfig(width, height, bands, bits, signed, bigEndian);
    }
}

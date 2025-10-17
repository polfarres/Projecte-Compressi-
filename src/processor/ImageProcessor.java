package processor;

import config.RawImageConfig;
import io.RawImageReader;
import io.RawImageWriter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static processor.Utils.parseConfigFromFilename;

public class ImageProcessor {

    private final File inputFolder;
    private final File outputFolder;
    Map<String, short[][][]> Images;

    public ImageProcessor(String inputPath, String outputPath) {
        this.inputFolder = new File(inputPath);
        this.outputFolder = new File(outputPath);
        this.Images = new HashMap<>();
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
    }



    public void processAll() {

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
                System.out.println("Image " + file.getName() + " processed.");
                RawImageWriter.writeRaw(new File(outputFolder, outputNameRAw).getAbsolutePath(), img, config);


            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public void uploadImages(){
        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));



        if (files == null) {
            System.out.println("No s'han trobat fitxers RAW a " + inputFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                RawImageConfig config = parseConfigFromFilename(file.getName());
                short[][][] img = RawImageReader.readRaw(file.getAbsolutePath(), config);
                Images.put(file.getName(), img);
            } catch (Exception e) {
                System.err.println("Error processant: " + file.getName());
                e.printStackTrace();
            }
        }

        System.out.println("Images Uploaded.");
    }

    public void calculateImageEntropy() {
        uploadImages();

        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String imageName = entry.getKey();
            short[][][] img = entry.getValue();

            Map<Short, Double> p = CalculateProbability.pixelProbability(img);
            double H = CalculateEntropy.entropy(p);
            System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", imageName, H);

        }
    }

    public void calculateConditionalEntropy() {
        // Asegúrate de que 'Images' esté cargado (por ejemplo, con uploadImages())
        uploadImages();


        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String imageName = entry.getKey();
            short[][][] img = entry.getValue();


            // 1. Calcular distribución conjunta P(l, r)
            Map<String, Double> pJoint = CalculateProbability.jointProbability(img);

            // 2. Calcular marginal P(l)
            Map<Short, Double> pLeft = CalculateProbability.marginalLeft(pJoint);

            // 3. Calcular entropía condicional H(R|L)
            double Hcond = CalculateEntropy.conditionalEntropy(pJoint, pLeft);

            // 4. Mostrar resultados
            System.out.printf("Imagen: %s -> Entropía condicional H(R|L): %.4f bits%n", imageName, Hcond);
        }
    }

    public void imageQuantitzation(int q, String outputPath) {
        uploadImages();

        QuantitzationProcess.quanticiseRoundingAll(this.Images,q, outputPath);

    }

    public void deQuantitzation(int q, String inputPath, String outputPath) {

        QuantitzationProcess.deQuanticiseRoundingAll(q, inputPath, outputPath);

    }

}

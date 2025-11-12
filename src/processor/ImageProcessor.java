package processor;

import config.RawImageConfig;
import io.RawImageReader;
import io.RawImageWriter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static processor.DistorsionMetrics.calculatePeakAbsoluteError;
import static processor.Utils.*;

public class ImageProcessor {

    private  File inputFolder;
    private  File outputFolder;
    private Map<String, short[][][]> Images;
    private boolean isImagesUploaded;

    public ImageProcessor(String inputPath, String outputPath) {
        this.inputFolder = new File(inputPath);
        this.outputFolder = new File(outputPath);
        this.isImagesUploaded = false;
        this.Images = new HashMap<>();
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
    }

    public void setInputFolder(String inputPath) {
        inputFolder = new File(inputPath);
    }

    public void setOutputFolder(String outputPath) {
        outputFolder = new File(outputPath);
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
        isImagesUploaded = true;
    }



    public void processAll() {

        if (!isImagesUploaded) {
            System.out.println("No hi ha imatges carregades a la memòria. No es pot processar.");
            return;
        }

        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String fileName = entry.getKey();
            short[][][] img = entry.getValue();

            try {
                // Utilitzem el nom de fitxer original per obtenir la configuració
                RawImageConfig config = parseConfigFromFilename(fileName);

                String outputNameRAw = "sortida" + fileName;

                // Escrivim la imatge carregada a la carpeta de sortida
                RawImageWriter.writeRaw(new File(outputFolder, outputNameRAw).getAbsolutePath(), img, config);

                System.out.println("Image " + fileName + " processed and written to output.");

            } catch (Exception e) {
                System.err.println("Error processant: " + fileName);
                e.printStackTrace();
            }
        }
    }



    public void calculateImageEntropy() {
        if (!isImagesUploaded) {
            System.out.println("No hi ha imatges carregades a la memòria. No es pot processar.");
            return;
        }

        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String imageName = entry.getKey();
            short[][][] img = entry.getValue();

            Map<Short, Double> p = CalculateProbability.pixelProbability(img);
            double H = CalculateEntropy.entropy(p);
            System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", imageName, H);

        }
    }

    public void calculateConditionalEntropy() {

        if (!isImagesUploaded) {
            System.out.println("No hi ha imatges carregades a la memòria. No es pot processar.");
            return;
        }

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

    public void imageQuantitzation(int q) {



        if (!outputFolder.exists()) {
            // Utilitzem mkdirs() per crear la carpeta i tots els seus pares si cal.
            boolean created = outputFolder.mkdirs();

            if (created) {
                System.out.println("Directori "+ outputFolder + " creat amb èxit.");
            }
        }

        if (!isImagesUploaded) {
            System.out.println("No hi ha imatges carregades a la memòria. No es pot processar.");
            return;
        }

        QuantitzationProcess.quanticiseRoundingAll(this.Images,q, outputFolder);

    }

    public void deQuantitzation(int q) {

        File[] files = inputFolder.listFiles((dir, name) ->
                name.toLowerCase().startsWith("q") && name.toLowerCase().endsWith(".raw")
        );


        if (files == null) {
            System.out.println("No s'han trobat fitxers de Qauntització RAW a " + inputFolder.getAbsolutePath());
            return;
        }



        // 2. Comprovar si el directori existeix. Si no existeix, intentar crear-lo.
        if (!outputFolder.exists()) {
            // Utilitzem mkdirs() per crear la carpeta i tots els seus pares si cal.
            boolean created = outputFolder.mkdirs();

            if (created) {
                System.out.println("Directori creat amb èxit.");
            }
        }

        QuantitzationProcess.deQuanticiseRoundingAll(q, inputFolder, outputFolder);

    }
    public void prediction() {

;

        // Assegurar la creació de la carpeta de sortida (obligatori ara que escrivim)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        File[] files = inputFolder.listFiles((dir, name) ->
                name.toLowerCase().startsWith("q") && name.toLowerCase().endsWith(".raw")
        );

        if (files == null || files.length == 0) {
            System.out.println("⚠️ ATENCIÓ: No s'han trobat fitxers quantitzats (Q*.raw) a la carpeta: " + inputFolder.getName());
            return;
        }

        PredictorDPCM predictor = new PredictorDPCM();

        for (File file : files) {
            String fileName = file.getName();

            try {
                RawImageConfig config = parseConfigFromFilename(fileName);
                short[][][] imgDades = RawImageReader.readRaw(file.getAbsolutePath(), config);

                // 1. Aplicar la predicció: el resultat és la matriu de RESIDUS (int[][][])
                int[][][] residuDades = predictor.aplicarPrediccio(imgDades);

                // 2. Generar el nom del fitxer de sortida (.txt)
                String baseName = fileName.replace(".raw", "").replace(".RAW", "");
                String txtOutputName = "PREDICCIÓ_" + baseName + ".txt";

                // 3. Obtenir la ruta completa del fitxer de sortida
                String fullOutputPath = new File(inputFolder, txtOutputName).getAbsolutePath();

                // 4. GUARDAR la matriu de residus al fitxer de text
                printMatrixToFile(residuDades, fullOutputPath, "Residus DPCM per a la imatge: " + fileName);

            } catch (Exception e) {
                System.err.println("Error processant predicció per a: " + fileName);
                e.printStackTrace();
            }
        }
        System.out.println("\n✅ Procés de Predicció DPCM finalitzat.");
    }

    public void deprediction() {


        // Assegurar la creació de la carpeta de sortida (obligatori ara que escrivim)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        File[] files = inputFolder.listFiles((dir, name) ->
                name.toLowerCase().startsWith("q") && name.toLowerCase().endsWith(".raw")
        );

        if (files == null || files.length == 0) {
            System.out.println("⚠️ ATENCIÓ: No s'han trobat fitxers quantitzats (Q*.raw) a la carpeta: " + inputFolder.getName());
            return;
        }

        PredictorDPCM predictor = new PredictorDPCM();

        for (File file : files) {
            String fileName = file.getName();

            try {
                RawImageConfig config = parseConfigFromFilename(fileName);
                short[][][] imgDades = RawImageReader.readRaw(file.getAbsolutePath(), config);

                // 1. Aplicar la predicció: el resultat és la matriu de RESIDUS (int[][][])
                int[][][] residuDades = predictor.aplicarPrediccio(imgDades);

                // 2. Generar el nom del fitxer de sortida (.txt)
                String baseName = fileName.replace(".raw", "").replace(".RAW", "");
                String txtOutputName = "PREDICCIÓ_" + baseName + ".txt";

                // 3. Obtenir la ruta completa del fitxer de sortida
                String fullOutputPath = new File(outputFolder, txtOutputName).getAbsolutePath();


                short[][][] desprediccio = predictor.reconstruirDades(residuDades);

                double mse= calculatePeakAbsoluteError(imgDades,desprediccio);
                System.out.print("mea de la imatge "  +fileName+": " +mse +"\n");

            } catch (Exception e) {
                System.err.println("Error processant predicció per a: " + fileName);
                e.printStackTrace();
            }
        }
        System.out.println("\n✅ Procés de Predicció DPCM finalitzat.");
    }


    public void calculateDistortionMetrics(String originalPath, String reconstructedPath) {


    }



}

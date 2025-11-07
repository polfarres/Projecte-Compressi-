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

        File outputDir = new File(outputPath);

        // 2. Comprovar si el directori existeix. Si no existeix, intentar crear-lo.
        if (!outputDir.exists()) {
            // Utilitzem mkdirs() per crear la carpeta i tots els seus pares si cal.
            boolean created = outputDir.mkdirs();

            if (created) {
                System.out.println("Directori creat amb èxit.");
            }
        }


        uploadImages();

        QuantitzationProcess.quanticiseRoundingAll(this.Images,q, outputPath);

    }

    public void deQuantitzation(int q, String inputPath, String outputPath) {

        File[] files = inputFolder.listFiles((dir, name) ->
                name.toLowerCase().startsWith("q") && name.toLowerCase().endsWith(".raw")
        );


        if (files == null) {
            System.out.println("No s'han trobat fitxers de Qauntització RAW a " + inputFolder.getAbsolutePath());
            return;
        }

        File outputDir = new File(outputPath);

        // 2. Comprovar si el directori existeix. Si no existeix, intentar crear-lo.
        if (!outputDir.exists()) {
            // Utilitzem mkdirs() per crear la carpeta i tots els seus pares si cal.
            boolean created = outputDir.mkdirs();

            if (created) {
                System.out.println("Directori creat amb èxit.");
            }
        }

        QuantitzationProcess.deQuanticiseRoundingAll(q, inputPath, outputPath);

    }
    public void prediction(String inputPath, String outputPath) {

        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);

        // Assegurar la creació de la carpeta de sortida (obligatori ara que escrivim)
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles((dir, name) ->
                name.toLowerCase().startsWith("q") && name.toLowerCase().endsWith(".raw")
        );

        if (files == null || files.length == 0) {
            System.out.println("⚠️ ATENCIÓ: No s'han trobat fitxers quantitzats (Q*.raw) a la carpeta: " + inputPath);
            return;
        }

        PredictorDPCM predictor = new PredictorDPCM();

        for (File file : files) {
            String fileName = file.getName();

            try {
                RawImageConfig config = parseConfigFromFilename(fileName);
                short[][][] imgDades = RawImageReader.readRaw(file.getAbsolutePath(), config);

                // 1. Aplicar la predicció: el resultat és la matriu de RESIDUS (int[][][])
                short[][][] residuDades = predictor.aplicarPrediccio(imgDades);

                // 2. Generar el nom del fitxer de sortida (.txt)
                String baseName = fileName.replace(".raw", "").replace(".RAW", "");
                String txtOutputName = "PREDICCIÓ_" + baseName + ".txt";

                // 3. Obtenir la ruta completa del fitxer de sortida
                String fullOutputPath = new File(outputDir, txtOutputName).getAbsolutePath();

                // 4. GUARDAR la matriu de residus al fitxer de text
                printMatrixToFile(residuDades, fullOutputPath, "Residus DPCM per a la imatge: " + fileName);

            } catch (Exception e) {
                System.err.println("Error processant predicció per a: " + fileName);
                e.printStackTrace();
            }
        }
        System.out.println("\n✅ Procés de Predicció DPCM finalitzat.");
    }

    public void deprediction(String inputPath, String outputPath) {

        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);

        // Assegurar la creació de la carpeta de sortida (obligatori ara que escrivim)
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles((dir, name) ->
                name.toLowerCase().startsWith("q") && name.toLowerCase().endsWith(".raw")
        );

        if (files == null || files.length == 0) {
            System.out.println("⚠️ ATENCIÓ: No s'han trobat fitxers quantitzats (Q*.raw) a la carpeta: " + inputPath);
            return;
        }

        PredictorDPCM predictor = new PredictorDPCM();

        for (File file : files) {
            String fileName = file.getName();

            try {
                RawImageConfig config = parseConfigFromFilename(fileName);
                short[][][] imgDades = RawImageReader.readRaw(file.getAbsolutePath(), config);

                // 1. Aplicar la predicció: el resultat és la matriu de RESIDUS (int[][][])
                short[][][] residuDades = predictor.aplicarPrediccio(imgDades);

                // 2. Generar el nom del fitxer de sortida (.txt)
                String baseName = fileName.replace(".raw", "").replace(".RAW", "");
                String txtOutputName = "PREDICCIÓ_" + baseName + ".txt";

                // 3. Obtenir la ruta completa del fitxer de sortida
                String fullOutputPath = new File(outputDir, txtOutputName).getAbsolutePath();


                short[][][] desprediccio = predictor.reconstruirDades(residuDades);

                double mse= calculatePeakAbsoluteError(imgDades,desprediccio);
                System.out.print(mse);

            } catch (Exception e) {
                System.err.println("Error processant predicció per a: " + fileName);
                e.printStackTrace();
            }
        }
        System.out.println("\n✅ Procés de Predicció DPCM finalitzat.");
    }


    public void calculateDistortionMetrics(String originalPath, String reconstructedPath) {

        File originalDir = new File(originalPath);
        File reconstructedDir = new File(reconstructedPath);

        if (!reconstructedDir.exists() || !reconstructedDir.isDirectory()) {
            System.err.println("❌ ERROR: La carpeta d'imatges quantitzades no existeix: " + reconstructedPath);
            System.out.println("Primer, executa l'opció 5 (Quantització d'imatges).");
            return;
        }

        // 1. Carregar imatges originals (utilitzem el mètode propi per carregar-les a this.Images)
        // La crida a uploadImages() utilitza this.inputFolder (que s'ha inicialitzat amb l'originalPath)
        uploadImages();

        if (this.Images.isEmpty()) {
            System.err.println("❌ ERROR: No s'han pogut carregar les imatges originals de " + originalPath);
            return;
        }

        // 2. Llistar els fitxers quantitzats (.raw)
        File[] reconstructedFiles = reconstructedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));

        if (reconstructedFiles == null || reconstructedFiles.length == 0) {
            System.err.println("❌ ERROR: No s'han trobat fitxers RAW a la carpeta de quantitzades: " + reconstructedPath);
            return;
        }

        System.out.println("Comparant " + this.Images.size() + " imatges originals amb les QUANTITZADES:");

        for (File reconstructedFile : reconstructedFiles) {
            String compressedFileName = reconstructedFile.getName();

            try {
                // Generar el nom original per fer el 'match'.
                String originalFileName = stripPrefixes(compressedFileName);

                // Càrrega de la imatge quantitzada (short[][][])
                RawImageConfig config = parseConfigFromFilename(compressedFileName);
                short[][][] reconstructedImg = RawImageReader.readRaw(reconstructedFile.getAbsolutePath(), config);

                // Obtenció de la imatge original carregada prèviament
                short[][][] originalImg = this.Images.get(originalFileName);

                if (originalImg != null) {
                    // Càlcul de mètriques
                    double mse = DistorsionMetrics.calculateMSE(originalImg, reconstructedImg);
                    int pae = calculatePeakAbsoluteError(originalImg, reconstructedImg);

                    System.out.printf("  [Fitxer Quantitzat: %s] -> MSE: %.4f | PAE: %d%n", compressedFileName, mse, pae);
                } else {
                    System.err.println("  ⚠️ No s'ha trobat la imatge original corresponent per a: " + originalFileName);
                }

            } catch (Exception e) {
                System.err.println("Error processant mètriques per a: " + compressedFileName);
                e.printStackTrace();
            }
        }
    }



}

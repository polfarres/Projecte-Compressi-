package processor;

import config.RawImageConfig;
import io.BitReader;
import io.RawImageReader;
import io.RawImageWriter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Realiza el proceso completo de codificación:
     * Cuantización -> Predicción -> Codificación Aritmética -> Guardado en disco.
     * También realiza una verificación de calidad (PAE/MSE).
     *
     * @param q Factor de cuantización.
     */
    public void coder() {

        // Asegurar que existe la carpeta de salida para comprimidos
        File compressedDir = new File(outputFolder, "compressed");
        if (!compressedDir.exists()) compressedDir.mkdirs();

        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String imageName = entry.getKey();
            short[][][] imgOriginal = entry.getValue();


            try {
                // 1. CUANTIZACIÓN (Lossy)
                short[][][] imgQuantized = QuantitzationProcess.quantisize(imgOriginal);

                // 2. PREDICCIÓN (Lossless - DPCM + ZigZag)
                PredictorDPCM predictor = new PredictorDPCM();
                // Esto devuelve INT[][][] con valores siempre positivos (mapeados)
                int[][][] imgPredicted = predictor.aplicarPrediccio(imgQuantized);

                // 3. APLANAR DATOS (Flattening)
                // El codificador aritmético necesita una lista secuencial de símbolos, no una matriz 3D.
                java.util.List<Integer> symbols = new java.util.ArrayList<>();
                int bands = imgPredicted.length;
                int height = imgPredicted[0].length;
                int width = imgPredicted[0][0].length;

                for (int b = 0; b < bands; b++) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            symbols.add(imgPredicted[b][y][x]);
                        }
                    }
                }

                // 4. CODIFICACIÓN ARITMÉTICA (Entropy Coding)

                // a) Calcular tabla de frecuencias (necesaria para decodificar después)
                // En un caso real, esta tabla se debe guardar en la cabecera del archivo.
                java.util.List<Integer> cumFreq = ArithmeticCoder.computeCumFreq(symbols);

                // b) Iniciar codificador
                io.BitWriter bw = new io.BitWriter();
                ArithmeticCoder coder = new ArithmeticCoder();

                // c) Codificar símbolo a símbolo
                for (int symbol : symbols) {
                    coder.encodeSymbol(symbol, cumFreq, bw);
                }
                coder.finish(bw);

                // 5. GUARDAR EN DISCO (Archivo comprimido .ac)
                String compressedName = "Compressed_" + imageName.replace(".raw", ".ac");
                File fileOut = new File(compressedDir, compressedName);

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(fileOut)) {
                    byte[] compressedBytes = bw.getBuffer();
                    fos.write(compressedBytes);

                    // Estadísticas de compresión
                    long originalSize = bands * height * width * 2L; // 2 bytes por short
                    long compressedSize = compressedBytes.length;
                    double ratio = (double) originalSize / compressedSize;

                    System.out.println("   💾 Guardado: " + fileOut.getName());
                    System.out.println("   📊 Tamaño Original: " + originalSize + " bytes");
                    System.out.println("   📉 Tamaño Comprimido: " + compressedSize + " bytes");
                    System.out.printf("   🚀 Ratio de Compresión: %.2f : 1%n", ratio);
                }

                // 6. VERIFICACIÓN (Decodificación simulada para comprobar error)
                // Reconstruimos desde los datos predichos (simulando que hemos decodificado bien)
                short[][][] imgReconstructed = predictor.reconstruirDades(imgPredicted);

                // Descuantizamos (Paso inverso a la cuantización)
                short[][][] imgFinal = QuantitzationProcess.dequantisize(imgReconstructed);

                // Cálculo de error (Original vs Final)
                int pae = DistorsionMetrics.calculatePeakAbsoluteError(imgOriginal, imgFinal);
                double mse = DistorsionMetrics.calculateMSE(imgOriginal, imgFinal);

                System.out.println("   ✅ Verificación Completada:");
                System.out.println("      - PAE (Error Máximo Absoluto): " + pae);
                System.out.printf("      - MSE (Error Cuadrático Medio): %.4f%n", mse);

            } catch (Exception e) {
                System.err.println("❌ Error en el proceso de codificación de: " + imageName);
                e.printStackTrace();
            }
        }
    }
    public void decoder() {
        // La carpeta d'entrada és on vam guardar els comprimits
        File compressedDir = new File(inputFolder, "compressed");
        if (!compressedDir.exists() || !compressedDir.isDirectory()) {
            System.out.println("❌ No existeix la carpeta de comprimits: " + compressedDir.getAbsolutePath());
            return;
        }

        // La carpeta de sortida serà 'decoded'
        File decodedDir = new File(outputFolder, "decoded");
        if (!decodedDir.exists()) decodedDir.mkdirs();

        // Busquem fitxers .ac
        File[] files = compressedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ac"));
        if (files == null || files.length == 0) {
            System.out.println("⚠️ No s'han trobat fitxers comprimits (.ac).");
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            System.out.println("\n🔓 Descodificant: " + fileName);

            // Obrim DataInputStream per llegir el header binari
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

                // 1. LLEGIR EL HEADER
                RawImageConfig config = RawImageConfig.readHeader(dis);

                // Obtenim les mides i la Q
                int totalPixels = config.width * config.height * config.bands;

                // 2. RECONSTRUIR FREQÜÈNCIES ACUMULADES
                // El decoder aritmètic necessita List<Integer> de freqüències acumulades
                List<Integer> cumFreq = new ArrayList<>();
                int currentSum = 0;
                cumFreq.add(0); // cumFreq[0] és sempre 0

                // Convertim short[] frequencies (del header) a cumFreq
                for (short f : config.frequencies) {
                    // El '& 0xFFFF' tracta el short de Java com a valor positiu (sense signe)
                    int freqVal = f & 0xFFFF;
                    currentSum += freqVal;
                    cumFreq.add(currentSum);
                }

                // 3. LLEGIR EL BITSTREAM COMPRIMIT (La resta del fitxer)
                // dis.readAllBytes() llegeix els bytes que queden al flux
                byte[] compressedBytes = dis.readAllBytes();

                BitReader br = new BitReader(compressedBytes);
                ArithmeticCoder decoder = new ArithmeticCoder();
                decoder.initializeDecoder(br);

                // 4. DESCODIFICAR ELS SÍMBOLS I RECONSTRUIR LA MATRIU
                int[][][] imgPredicted = new int[config.bands][config.height][config.width];

                for (int b = 0; b < config.bands; b++) {
                    for (int y = 0; y < config.height; y++) {
                        for (int x = 0; x < config.width; x++) {
                            int symbol = decoder.decodeSymbol(cumFreq, br);
                            imgPredicted[b][y][x] = symbol;
                        }
                    }
                }

                // 5. DESPREDICCIÓ (Invers ZigZag + Invers DPCM)
                PredictorDPCM predictor = new PredictorDPCM();
                short[][][] imgReconstructed = predictor.reconstruirDades(imgPredicted);

                // 6. DESQUANTITZACIÓ (Amb el pas Q que hem llegit del header)
                // S'assumeix que QuantitzationProcess.dequantisize utilitza la Q correcta
                // o que es pot configurar la Q estàtica amb config.qStep
                // (Per ara, cridem la versió sense argument, assumint que la Q es fixa.)
                short[][][] imgFinal = QuantitzationProcess.dequantisize(imgReconstructed);

                // 7. GUARDAR IMATGE RAW RECONSTRUÏDA
                String outputName = "Decoded_" + fileName.replace(".ac", ".raw");
                String fullOutputPath = new File(decodedDir, outputName).getAbsolutePath();

                RawImageWriter.writeRaw(fullOutputPath, imgFinal, config);

                System.out.println("   💾 Imatge Recuperada: " + outputName);

            } catch (Exception e) {
                System.err.println("❌ Error fatal descodificant: " + fileName);
                System.err.println("   Possiblement corrupció de Header o Bitstream.");
                e.printStackTrace();
            }
        }
        System.out.println("✅ Procés de Descodificació Finalitzat.");
    }



}

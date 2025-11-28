package processor;

import config.RawImageConfig;
import stages.ArithmeticCoder;
import stages.PredictorDPCM;
import stages.QuantitzationProcess;
import utils.*;
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

import static utils.DistorsionMetrics.calculatePeakAbsoluteError;
import static utils.Utils.*;

public class ImageProcessor {

    private Map<String, short[][][]> Images;
    private File inputImage;

    public ImageProcessor(){
        Images = new HashMap<>();
    }

    public void uploadImage(String imagePath) {
        inputImage = new File(imagePath);
    }

    public void setOutputFolder(String outputPath) {
        File outputFolder = new File(outputPath);
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

    public void coder() {
        if (this.Images.isEmpty()) {
            System.out.println("❌ No hi ha imatges carregades per codificar.");
            return;
        }

        File compressedDir = new File(outputFolder, "compressed");
        if (!compressedDir.exists()) compressedDir.mkdirs();

        int qVal = QuantitzationProcess.Q_STEP;

        for (Map.Entry<String, short[][][]> entry : Images.entrySet()) {
            String imageName = entry.getKey();
            short[][][] imgOriginal = entry.getValue();

            System.out.println("\n🚀 Codificant: " + imageName + " (Q=" + qVal + ")");

            try {
                RawImageConfig config = parseConfigFromFilename(imageName);

                // 0. Deep Copy
                short[][][] imgToProcess = deepCopy(imgOriginal);

                // 1. Quantització
                short[][][] imgQuantized = QuantitzationProcess.quantisize(imgToProcess);

                // 2. Predicció
                PredictorDPCM predictor = new PredictorDPCM();
                int[][][] imgPredicted = predictor.aplicarPrediccio(imgQuantized);

                // 3. Aplanar i Histograma
                java.util.List<Integer> symbols = new java.util.ArrayList<>();

                int maxSymbols = 131072; //rang short

                // CORRECCIÓ: Ara fem servir int[] per comptar, no short[]
                int[] freqHistogram = new int[maxSymbols];

                int bands = imgPredicted.length;
                int height = imgPredicted[0].length;
                int width = imgPredicted[0][0].length;

                for (int b = 0; b < bands; b++) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int val = imgPredicted[b][y][x];
                            symbols.add(val);

                            // Comptem freqüència sense por al desbordament
                            if (val >= 0 && val < maxSymbols) {
                                freqHistogram[val]++;
                            }
                        }
                    }
                }

                // 4. Codificació Aritmètica
                java.util.List<Integer> cumFreq = ArithmeticCoder.computeCumFreq(symbols);
                BitWriter bw = new BitWriter();
                ArithmeticCoder coder = new ArithmeticCoder();

                for (int symbol : symbols) {
                    coder.encodeSymbol(symbol, cumFreq, bw);
                }
                coder.finish(bw);

                // 5. Guardar .ac
                String compressedName = "Compressed_" + imageName.replace(".raw", ".ac");
                File fileOut = new File(compressedDir, compressedName);

                // Ara passem l'int[] correctament
                config.setCompressionHeaderData(qVal, freqHistogram);

                try (java.io.DataOutputStream dos = new java.io.DataOutputStream(
                        new java.io.BufferedOutputStream(new java.io.FileOutputStream(fileOut)))) {

                    // Escriu Header (freqüències com INTs)
                    config.writeHeader(dos);

                    // Escriu Bits
                    dos.write(bw.getBuffer());

                    long originalSize = bands * height * width * 2L;
                    long compressedSize = fileOut.length();
                    double ratio = (double) originalSize / compressedSize;

                    System.out.println("   💾 Guardat: " + fileOut.getName());
                    System.out.println("   📦 Mida: " + compressedSize + " bytes");
                    System.out.printf("   📉 Rati: %.2f : 1%n", ratio);
                }

                // 6. Verificació...
                short[][][] imgReconstructed = predictor.reconstruirDades(imgPredicted);
                short[][][] imgFinal = QuantitzationProcess.dequantisize(imgReconstructed);
                int pae = DistorsionMetrics.calculatePeakAbsoluteError(imgOriginal, imgFinal);
                double mse = DistorsionMetrics.calculateMSE(imgOriginal, imgFinal);
                System.out.printf("   ✅ MSE: %.4f | PAE: %d%n", mse, pae);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void decoder() {
        // Asumimos que los archivos comprimidos están en la carpeta 'compressed' dentro del input o output configurado
        // Ajusta esta ruta si tu ChooseOperation define el inputFolder directamente como la carpeta de comprimidos.
        File compressedDir = new File(inputFolder.getAbsolutePath());

        // Si no encuentra .ac en la raíz, busca en /compressed (por compatibilidad con la estructura anterior)
        if (compressedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ac")).length == 0) {
            File subDir = new File(inputFolder, "compressed");
            if (subDir.exists()) compressedDir = subDir;
        }

        File decodedDir = new File(outputFolder, "decoded");
        if (!decodedDir.exists()) decodedDir.mkdirs();

        File[] files = compressedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ac"));
        if (files == null || files.length == 0) {
            System.out.println("⚠️ No se han encontrado archivos comprimidos (.ac) en: " + compressedDir.getAbsolutePath());
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            System.out.println("\n🔓 Descodificando: " + fileName);

            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

                // 1. LEER HEADER
                // Recuperamos dimensiones, Q y el histograma original
                RawImageConfig config = RawImageConfig.readHeader(dis);

                // 2. RECONSTRUIR FRECUENCIAS ACUMULADAS
                // Convertimos el histograma int[] a la lista acumulada que necesita el ArithmeticCoder
                List<Integer> cumFreq = new ArrayList<>();
                int currentSum = 0;
                cumFreq.add(0); // El inicio siempre es 0

                for (int freq : config.frequencies) {
                    currentSum += freq;
                    cumFreq.add(currentSum);
                }

                // 3. LEER BITSTREAM (El resto del archivo)
                byte[] compressedBytes = dis.readAllBytes();

                BitReader br = new BitReader(compressedBytes);
                ArithmeticCoder decoder = new ArithmeticCoder();
                decoder.initializeDecoder(br);

                // 4. DECODIFICAR SÍMBOLOS
                int[][][] imgPredicted = new int[config.bands][config.height][config.width];

                for (int b = 0; b < config.bands; b++) {
                    for (int y = 0; y < config.height; y++) {
                        for (int x = 0; x < config.width; x++) {
                            // Decodificamos un símbolo usando la tabla de frecuencias reconstruida
                            int symbol = decoder.decodeSymbol(cumFreq, br);
                            imgPredicted[b][y][x] = symbol;
                        }
                    }
                }

                // 5. DESPREDICCIÓN (Inverso DPCM + ZigZag)
                PredictorDPCM predictor = new PredictorDPCM();
                short[][][] imgReconstructed = predictor.reconstruirDades(imgPredicted);

                // 6. DESCUANTIZACIÓN
                // Usamos la lógica de descuantización.
                // Nota: Tu implementación actual de 'quantisize' guarda los valores ya multiplicados por Q (aproximados),
                // por lo que 'dequantisize' principalmente hace clamping.
                short[][][] imgFinal = QuantitzationProcess.dequantisize(imgReconstructed);

                // 7. GUARDAR IMAGEN RECONSTRUIDA
                String outputName = "Decoded_" + fileName.replace(".ac", ".raw");
                String fullOutputPath = new File(decodedDir, outputName).getAbsolutePath();

                RawImageWriter.writeRaw(fullOutputPath, imgFinal, config);

                System.out.println("   💾 Imagen Recuperada: " + outputName);
                System.out.println("   ⚙️ Parámetros recuperados: " + config.width + "x" + config.height + " Q=" + config.qStep);


            } catch (Exception e) {
                System.err.println("❌ Error fatal descodificando: " + fileName);
                e.printStackTrace();
            }
        }
        System.out.println("✅ Proceso de Descodificación Finalizado.");
    }

    public void compareOriginalWithDecoded() {
        if (this.Images.isEmpty()) {
            System.out.println("⚠️ No hay imágenes originales cargadas en memoria.");
            System.out.println("   Asegúrate de haber ejecutado 'uploadImages()' o la Opción 1 primero.");
            return;
        }

        File decodedDir = new File(outputFolder, "decoded");
        if (!decodedDir.exists() || !decodedDir.isDirectory()) {
            System.out.println("❌ No existe la carpeta de imágenes descodificadas: " + decodedDir.getAbsolutePath());
            System.out.println("   Ejecuta primero la Opción 10 (Descodificar).");
            return;
        }

        File[] decodedFiles = decodedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".raw"));
        if (decodedFiles == null || decodedFiles.length == 0) {
            System.out.println("⚠️ No se han encontrado imágenes descodificadas en: " + decodedDir.getAbsolutePath());
            return;
        }

        System.out.println("\n📊 Calculando Métricas (Original vs Descodificada):");
        System.out.println("---------------------------------------------------");

        for (File file : decodedFiles) {
            String decodedName = file.getName();

            // Reconstruir el nombre original eliminando prefijos agregados por el proceso
            // Decoded_Compressed_Nombre.raw -> Nombre.raw
            String originalName = decodedName.replace("Decoded_", "").replace("Compressed_", "");

            short[][][] originalImg = this.Images.get(originalName);

            if (originalImg != null) {
                try {
                    // Leemos la imagen descodificada del disco
                    RawImageConfig config = parseConfigFromFilename(decodedName);
                    short[][][] decodedImg = RawImageReader.readRaw(file.getAbsolutePath(), config);

                    // Calculamos métricas
                    double mse = DistorsionMetrics.calculateMSE(originalImg, decodedImg);
                    int pae = DistorsionMetrics.calculatePeakAbsoluteError(originalImg, decodedImg);

                    System.out.println("🔹 Imagen: " + originalName);
                    System.out.printf("   MSE: %.4f\n", mse);
                    System.out.printf("   PAE: %d\n", pae);
                    System.out.println("---------------------------------------------------");

                } catch (Exception e) {
                    System.err.println("❌ Error leyendo imagen descodificada: " + decodedName);
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ No se encontró la original en memoria para: " + decodedName + " (Se buscaba: " + originalName + ")");
            }
        }
    }

    private short[][][] deepCopy(short[][][] source) {
        int b = source.length;
        int h = source[0].length;
        int w = source[0][0].length;
        short[][][] dest = new short[b][h][w];
        for (int i = 0; i < b; i++) {
            for (int j = 0; j < h; j++) {
                System.arraycopy(source[i][j], 0, dest[i][j], 0, w);
            }
        }
        return dest;
    }

}

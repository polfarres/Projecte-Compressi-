package processor;
import image.Image;
import stages.PredictorDPCM;
import stages.Quantitzation;
import utils.*;

import java.io.IOException;
import java.util.Map;

import static utils.Utils.*;

public class ImageProcessor {

    private String inputImage;
    Image image = null;

    public void uploadImage(String imagePath) {
        inputImage = imagePath;
    } //✅

    public Image readImage() {

        Image image = null;

            try {
                assert this.inputImage != null;
                image = parseConfigFromFilename(this.inputImage);
                System.out.println();
                System.out.println("Image " + image.name + " read with config: ");
                image.printInfo();
                System.out.println("Image " + image.name + " processed and written to memory.");

            } catch (Exception e) {
                System.err.println("Error processant: " + this.inputImage + "no s'ha pogut llegir.");
                e.printStackTrace();
            }


        return image;
    } //✅

    public void calculateImageEntropyTest() { //✅

        image = readImage();
        double H = Entropy.imageEntropy(image);
        System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", image.name, H);

    } //✅

    public void calculateConditionalEntropyTest() {

        image = readImage();

        double Hcond = Entropy.conditionalEntropy(image);

        System.out.printf("Imagen: %s -> Entropía condicional H(R|L): %.4f bits%n", image.name, Hcond);

    } //✅

    public void calculateConditionalEntropy4PixelsTest() {

        image = readImage();

        // 1. Calcular distribución conjunta P(l, r)
        //Probabilidad condicionada del píxel vecino en sus 4 cardinalidades
        Map<String, Double> pJoint = Probability.jointProbability4(image.img);

        // 2. Calcular marginal P(l)
        Map<Integer, Double> pLeft = Probability.marginalLeft(pJoint);

        // 3. Calcular entropía condicional H(R|L)
        double Hcond = Entropy.conditionalEntropy(pJoint, pLeft);

        // 4. Mostrar resultados
        System.out.printf("Imagen: %s -> Entropía condicional 4 cardinalitats H(R|L): %.4f bits%n", image.name, Hcond);

    } //✅

    public void imageQuantitzationTest(int q) {

        image = readImage();

        Quantitzation quantitzation = Quantitzation.init(q);
        quantitzation.quanticiseDeadZone(image);
        System.out.println("Image " + image.name + " reduced.");

        image.name = "Q_" + q + "_" + image.name;

        writeResult(image);

    } //✅

    public void deQuantitzationTest() {

        image = readImage();

        int q = 0;

        try {
            q = Utils.readQuantization(image.name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Quantitzation quantitzation = Quantitzation.init(q);
        quantitzation.dequanticiseDeadZone(image);

        image.name = "de" + image.name;
        writeResult(image);

    } //✅

    public void predictionTest() {

        image = readImage();

        PredictorDPCM predictor = new PredictorDPCM();

        // 1. Aplicar la predicció: el resultat és la matriu de RESIDUS
        predictor.aplicarPrediccioPixelAnterior(image);
        image.name = "predicted_" + image.name;
        double H = Entropy.imageEntropy(image);
        System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", image.name, H);

    } //✅

    public void depredictionTest() {

        PredictorDPCM predictor = new PredictorDPCM();
        Image image = readImage();

        predictor.aplicarPrediccioPixelAnterior(image);
        predictor.desferPrediccioPixelAnterior(image);

        image.name = "depredicted_" + image.name;
        double H = Entropy.imageEntropy(image);
        System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", image.name, H);

    } //✅

    public void compressImage() {


    }

    /*

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
                Image config = Image.readHeader(dis);

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
                    Image config = parseConfigFromFilename(decodedName);
                    short[][][] decodedImg = RawImageReader.readRaw(file.getAbsolutePath(), config);

                    // Calculamos métricas
                    double mse = DistorsionMetrics.calculateMSE(originalImg, decodedImg);
                    int pae = calculatePeakAbsoluteError(originalImg, decodedImg);

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


 */
}

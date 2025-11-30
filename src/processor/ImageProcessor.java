package processor;
import image.Image;
import stages.ArithmeticCoder;
import stages.PredictorDPCM;
import stages.Quantitzation;
import utils.*;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.InputImageReader.readAC;
import static utils.Utils.*;

public class ImageProcessor {

    private String inputImage;

    public void uploadImage(String imagePath) {
        inputImage = imagePath;
        readImage();
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

        Image image = readImage();
        double H = Entropy.imageEntropy(image);
        System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", image.name, H);

    } //✅

    public void calculateConditionalEntropyTest() {

        Image image = readImage();
        double Hcond = Entropy.conditionalEntropy(image);
        System.out.printf("Imagen: %s -> Entropía condicional H(R|L): %.4f bits%n", image.name, Hcond);

    } //✅

    public void calculateConditionalEntropy4PixelsTest() {

        Image image = readImage();
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

        Image image = readImage();
        Quantitzation quantitzation = Quantitzation.init(q);
        quantitzation.quanticiseDeadZone(image);
        System.out.println("Image " + image.name + " reduced.");

        image.name = "Q_" + q + "_" + image.name;

        writeResult(image);

    } //✅

    public void deQuantitzationTest() {

        Image image = readImage();
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

        Image image = readImage();
        PredictorDPCM predictor = new PredictorDPCM();

        // 1. Aplicar la predicció: el resultat és la matriu de RESIDUS
        predictor.aplicarPrediccioPixelAnterior(image);
        image.name = "predicted_" + image.name;
        double H = Entropy.imageEntropy(image);
        System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", image.name, H);

    } //✅

    public void depredictionTest() {

        Image image = readImage();
        PredictorDPCM predictor = new PredictorDPCM();

        predictor.aplicarPrediccioPixelAnterior(image);
        predictor.desferPrediccioPixelAnterior(image);

        image.name = "depredicted_" + image.name;
        double H = Entropy.imageEntropy(image);
        System.out.printf("Imagen: %s -> Entropía total H(X): %.4f bits%n", image.name, H);

    } //✅

    public void compressImage() {

            PredictorDPCM predictor = new PredictorDPCM();
            Quantitzation quantitzator = new Quantitzation();
            ArithmeticCoder arithmeticCoder = new ArithmeticCoder();
            Image image = readImage();
            BitWriter bw = new BitWriter();

            // 1. Decorrelació DPCM
            predictor.aplicarPrediccioPixelAnterior(image);

            // 2. Quantització amb Q default
            quantitzator.quanticiseDeadZone(image);


            // 4. Codificació Aritmètica
            arithmeticCoder.encodeImage(image, bw);


            // 5. Escribim el fitxer comprimit
            System.out.println("Imatge " + image.name + " compressed.");
            writeCompressedImage(image, bw);
    }

    public void decoder() {

        PredictorDPCM predictor = new PredictorDPCM();
        Quantitzation quantitzator = new Quantitzation();
        ArithmeticCoder arithmeticCoder = new ArithmeticCoder();

        Image image = new Image(this.inputImage);
        BitReader br = new BitReader(readAC(image)); //Leemos el archivo comprimido extrayendo el header y los bytes


        // Inicializamos el decodificador aritmético
        arithmeticCoder.initializeDecoder(br);


            // 4. DECODIFICAR SÍMBOLOS
            image.img = new int[image.bands][image.height][image.width];

            for (int b = 0; b < image.bands; b++) {
                for (int y = 0; y < image.height; y++) {
                    for (int x = 0; x < image.width; x++) {
                        // Decodificamos un símbolo usando la tabla de frecuencias reconstruida
                        int symbol = arithmeticCoder.decodeSymbol(image.frequencies, br);
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

    /*
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

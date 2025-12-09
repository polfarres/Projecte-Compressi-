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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.InputImageReader.readAC;
import static utils.Utils.*;

public class ImageProcessor {

    private String inputImage;

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

    public Image readImageSequential() {
        Image image = null;
        try {
            assert this.inputImage != null;
            image = parseConfigFromFilename(this.inputImage);
        } catch (Exception e) {
            System.err.println("Error processant: " + this.inputImage + "no s'ha pogut llegir.");
            e.printStackTrace();
        }
        return image;
    }

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

        // 1. Quantització amb Q default
        quantitzator.quanticiseDeadZone(image);

        // 2. Decorrelació DPCM
        predictor.aplicarPrediccioPixelAnterior(image);


        // 4. Codificació Aritmètica
        arithmeticCoder.encodeImage(image, bw);


        // 5. Escribim el fitxer comprimit
        System.out.println("Imatge " + image.name + " compressed.");
        writeCompressedImage(image, bw);
    } //✅

    public void decoder() {

        PredictorDPCM predictor = new PredictorDPCM();
        Quantitzation quantitzator = new Quantitzation();
        ArithmeticCoder arithmeticCoder = new ArithmeticCoder();

        if (!this.inputImage.endsWith(".ac")) {
            System.out.println("WARNING: El formato de imagen no es .ac - Proporciona un archivo tipo .ac");
            return;
        }

        Image image = new Image(this.inputImage);
        BitReader br = new BitReader(readAC(image)); //Leemos el archivo comprimido extrayendo el header y los bytes

        // Inicializamos el decodificador aritmético
        arithmeticCoder.initializeDecoder(br);


        // Descodificamos la imagen
        arithmeticCoder.DecodeImage(image, br);

        // 5. DESPREDICCIÓN Inverso DPCM
        predictor.desferPrediccioPixelAnterior(image);

        // 6. DEQUANTITZACIÓ Inversa
        quantitzator.dequanticiseDeadZone(image);


        System.out.println("Imatge " + image.name + " decoded.");
        image.name = "decoded_" + image.name.replace(".ac", ".raw");
        writeResult(image);

    } //✅

    public void generateCurvesData() {



        System.out.println("Cargando imagen original de referencia...");
        Image originalImage = readImage();

        // Calcular total de píxeles para la fórmula de BPS
        long totalPixels = (long) originalImage.width * originalImage.height * originalImage.bands;

        System.out.println("\n========== GENERANDO DATOS (Q vs BPS vs PSNR) ==========");
        System.out.println("Imagen: " + originalImage.name);
        System.out.println("CSV Header: PSNR:bps bps:qStep");


        // 3. Bucle de Q desde 1 hasta 128
        // Puedes cambiar 'q+=1' a 'q+=5' para ir más rápido si solo quieres una vista previa
        for (int q = 1; q <= 50; q++) {
            try {

                // A. RECARGAR IMAGEN (Deep Copy)
                Image workingImage = readImageSequential();
                // --- ETAPA DE COMPRESIÓN (Simulación) ---

                // 1. Cuantización
                Quantitzation quant = Quantitzation.init(q);
                quant.quanticiseDeadZone(workingImage);

                // 2. Predicción DPCM
                PredictorDPCM predictor = new PredictorDPCM();
                predictor.aplicarPrediccioPixelAnterior(workingImage);

                // 3. Codificación Aritmética (Para obtener el tamaño real en bits)
                ArithmeticCoder coder = new ArithmeticCoder();
                BitWriter bw = new BitWriter();
                coder.encodeImage(workingImage, bw);

                // --- CÁLCULO DEL RATE (BPS - Bits Per Sample) ---
                // Le sumamos una estimación del header (aprox 50 bytes) para ser más realistas
                long headerBits = calcularBitsHeader(workingImage.frequencies);
                long compressedBits = ((long) bw.getSize() * 8) + headerBits;
                double bps = (double) compressedBits / totalPixels;

                // --- ETAPA DE RECONSTRUCCIÓN ---

                // 4. Despredicción
                predictor.desferPrediccioPixelAnterior(workingImage);

                // 5. Descuantización
                quant.dequanticiseDeadZone(workingImage);

                // --- CÁLCULO DE LA DISTORSIÓN (PSNR y MSE) ---
                // MODIFICADO: Ahora llamamos a utils.DistorsionMetrics pasando las matrices int[][][]
                double mse = DistorsionMetrics.calculateMSE(originalImage.img, workingImage.img);

                // Para el PSNR pasamos el MSE calculado y la profundidad de bits original
                double psnr = DistorsionMetrics.calculatePSNR(mse, originalImage.bitsPerSample);

                // B. IMPRIMIR RESULTADO CSV
                System.out.printf("%.4f;%.4f;%d%n", psnr, bps, q);

            } catch (Exception e) {
                System.err.println("Error procesando Q=" + q + ": " + e.getMessage());
            }
        }
        System.out.println("======================================");
    }

    public void generateCurvesDataImproved() {

        System.out.println("Cargando imagen original de referencia...");
        Image originalImage = readImage(); // Assegura't que tens aquest mètode accessible

        // Calcular total de píxeles per a la fórmula de BPS
        long totalPixels = (long) originalImage.width * originalImage.height * originalImage.bands;

        // Header fix (en bits):
        // Width(32) + Height(32) + Bands(32) + Bits(32) + Signed(8) + Endian(8) + QStep(32) = 176 bits.
        // Això són només 22 bytes. MOLT IMPORTANT: Ja no sumem la taula de freqüències!
        long STATIC_HEADER_BITS = 176;

        System.out.println("\n========== GENERANDO DATOS (Q vs BPS vs PSNR) [MODE ADAPTATIU] ==========");
        System.out.println("Imagen: " + originalImage.name);
        System.out.println("CSV Header: Q_Step;BPS;PSNR");

        // Bucle de QStep
        // Arribem fins a 100 o més perquè amb DeadZone es pot comprimir molt més sense trencar-ho tot.
        for (int q = 1; q <= 60; q++) {
            try {
                // A. RECARGAR IMAGEN (Deep Copy per no destrossar l'original)
                Image workingImage = readImageSequential();

                // --- ETAPA DE COMPRESIÓN ---

                // 1. Cuantización Dead Zone Millorada (La que hem fet abans)
                // Assegura't que Quantitzation té accés al Q actual
                Quantitzation quant = Quantitzation.init(q);
                quant.quanticiseDeadZoneImproved(workingImage);

                // 2. Predicción DPCM (Això genera molts zeros propers)
                PredictorDPCM predictor = new PredictorDPCM();
                predictor.aplicarPrediccioPixelAnterior(workingImage);

                // 3. Codificación Aritmética ADAPTATIVA
                // Important: encodeImage ara ha de fer servir AdaptiveFrequencyModel internament
                ArithmeticCoder coder = new ArithmeticCoder();
                BitWriter bw = new BitWriter();

                // Ja no calculem freqüències abans! El model s'adapta al vol.
                coder.encodeImage(workingImage, bw);

                // --- CÁLCULO DEL RATE (BPS) ---
                long compressedPayloadBits = (long) bw.getSize() * 8;
                long totalBits = compressedPayloadBits + STATIC_HEADER_BITS;

                double bps = (double) totalBits / totalPixels;

                // --- ETAPA DE RECONSTRUCCIÓN ---

                // 4. Despredicción
                predictor.desferPrediccioPixelAnterior(workingImage);

                // 5. Descuantización (Amb el centratge +Offset per guanyar PSNR)
                quant.dequanticiseDeadZoneImproved(workingImage);

                // --- CÁLCULO DE LA DISTORSIÓN (PSNR) ---
                double mse = DistorsionMetrics.calculateMSE(originalImage.img, workingImage.img);
                double psnr = DistorsionMetrics.calculatePSNR(mse, originalImage.bitsPerSample);

                // B. IMPRIMIR RESULTADO CSV (Utilitza punt o coma segons el teu Excel)
                // Format: QStep ; BPS ; PSNR
                System.out.printf("%d;%.4f;%.4f%n", q, bps, psnr);

            } catch (Exception e) {
                System.err.println("Error procesando Q=" + q + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("======================================");
    }

}

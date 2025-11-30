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
}

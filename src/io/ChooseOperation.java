package io;
import processor.ImageProcessor;

import java.util.Scanner;

public class ChooseOperation {

    private int option = 0;
    Scanner input = new Scanner(System.in);

    public void readOption() {



        System.out.println("Introdueix una opció (1-4): ");
        System.out.println("1.- Llegir Imatges ");
        System.out.println("2.- Calcular entropia Total de les imatges (ordre 0)");
        System.out.println("3.- Entropía condicionada grau (correlació de pixels)");
        System.out.println("4.- Calcular entropia condicionada 4 pixels propers (ordre 1)");
        System.out.println("5.- Quantització d'imatges");
        System.out.println("6.- DeQuantització d'imatges");
        System.out.println("7.- Predicció d'imatges (un cop aplicada qauntització previament)");
        System.out.println("8.- Despredicció (Reconstrucció) d'imatges");
        System.out.println("9.- Calcular Mètriques de Distorsió (MSE i PAE)"); // Unificada
        this.option = input.nextInt();


    }

    public void ExecuteCommand(String inputPath, String outputPath){
        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);
        Scanner Q;

        switch (this.option) {
            case 1:
                System.out.println("Procesamiento io de las imagenes en raw.");
                System.out.println("    -----------------------    ");
                processor.processAll();
                break;
            case 2:
                System.out.println("Entropía total de les imatges: ");
                System.out.println("    -----------------------    ");
                processor.calculateImageEntropy();
                break;
            case 3:
                System.out.println("Entropía condicionada grau (correlació de pixels) ");
                System.out.println("    -----------------------    ");
                processor.calculateConditionalEntropy();
                break;
            case 4:
                System.out.println("Entropía condicionada (correlació de 4 píxels propers) ");
                System.out.println("    -----------------------    ");
                System.out.println("   Work in progress   ");
                break;
            case 5:
                System.out.println("Quantització de imatges");
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols quantitzar?:  ");
                Q = new Scanner(System.in);
                outputPath += "/quantitzades";
                processor.imageQuantitzation(Q.nextInt(), outputPath);
                break;
            case 6:
                System.out.println("DeQuantització de imatges");
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols DeQuantitzar?:  ");
                Q = new Scanner(System.in);
                String InputPath = outputPath + "/quantitzades";
                outputPath += "/desquantització";
                processor.deQuantitzation(Q.nextInt(), InputPath ,outputPath);
                break;

            case 7:
                System.out.println("PREDICCIÓ D'IMATGES ");
                System.out.println("    -----------------------    ");
                String InputPathQuantitzades = outputPath + "/quantitzades"; // Entrada Q*.raw
                String OutputPathPrediction = outputPath + "/prediction";     // Sortida P_Q*.raw
                processor.prediction(InputPathQuantitzades, OutputPathPrediction);
                break;

            case 8:
                System.out.println("DESPREDICCIÓ (RECONSTRUCCIÓ) D'IMATGES");
                System.out.println("    -----------------------    ");
                // L'entrada és la carpeta amb els residus P_Q*.raw (Prediction)
                String InputPathResidus = outputPath + "/quantitzades";
                // La sortida és la imatge reconstruïda R_P_Q*.raw
                String OutputPathReconstruccio = outputPath + "/reconstruccio";
                processor.deprediction(InputPathResidus, OutputPathReconstruccio);
                break;

            case 9:
                System.out.println("CALCULANT MÈTRIQUES DE DISTORSIÓ (MSE i PAE)");
                System.out.println("    -----------------------    ");
                // Comparem: Imatges Originals (this.inputFolder) vs. Imatges Quantitzades
                String OriginalPath = inputPath; // Carpeta RAWs originals
                String CompressedPath = outputPath + "/quantitzades"; // <--- MODIFICAT
                processor.calculateDistortionMetrics(OriginalPath, CompressedPath);
                break;


        }
        input.close();

    }
}


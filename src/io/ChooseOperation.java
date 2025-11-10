package io;
import processor.ImageProcessor;

import java.util.Scanner;

public class ChooseOperation {

    private String inputOption; // Ara guardem l'entrada com a String
    Scanner input = new Scanner(System.in);
    private boolean isFinished = false;


    public void readOption() {
        System.out.println("Introdueix una opció (1-9 o 's'): "); // Actualitzem la llista
        System.out.println("1.- Llegir Imatges ");
        System.out.println("2.- Calcular entropia Total de les imatges (ordre 0)");
        System.out.println("3.- Entropía condicionada grau (correlació de pixels)");
        System.out.println("4.- Calcular entropia condicionada 4 pixels propers (ordre 1)");
        System.out.println("5.- Quantització d'imatges");
        System.out.println("6.- DeQuantització d'imatges");
        System.out.println("7.- Predicció d'imatges (un cop aplicada qauntització previament)");
        System.out.println("8.- Despredicció (Reconstrucció) d'imatges");
        System.out.println("9.- Calcular Mètriques de Distorsió (MSE i PAE) d'imatges quantitzades");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("s.- Surt de l'aplicació");

        // CORRECCIÓ CLAU: Llegim l'entrada com a String
        this.inputOption = input.next();
    }

    public void ExecuteCommand(String inputPath, String outputPath){

        if (this.inputOption.equalsIgnoreCase("s")) {
            this.isFinished = true;
            System.out.println("Sortint de l'aplicació. Adéu!");
            return;
        }

        int option;
        try {
            option = Integer.parseInt(this.inputOption);
        } catch (NumberFormatException e) {
            System.out.print("Opció invàlida. Introdueix un número (1-9) o 's'.\n");
            return;
        }



        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);
        processor.uploadImages();

        switch (option) {
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

                Scanner Q5 = new Scanner(System.in);


                outputPath += "/quantitzades";
                processor.setOutputFolder(outputPath);
                processor.imageQuantitzation(Q5.nextInt());
                break;
            case 6:
                System.out.println("DeQuantització de imatges");
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols DeQuantitzar?:  ");
                Scanner Q6 = new Scanner(System.in);

                processor.setInputFolder(outputPath + "/quantitzades");
                processor.setOutputFolder(outputPath+"/desquantització/Round_deQ_" + Q6.nextInt() + "_");

                processor.deQuantitzation(Q6.nextInt());
                break;

            case 7:
                System.out.println("PREDICCIÓ D'IMATGES ");
                System.out.println("    -----------------------    ");
                String InputPathQuantitzades = outputPath + "/quantitzades"; // Entrada Q*.raw
                String OutputPathPrediction = outputPath + "/prediction";     // Sortida P_Q*.raw

                processor.setInputFolder(outputPath+"/quantitzades");
                processor.setOutputFolder(outputPath + "/prediction");


                processor.prediction(InputPathQuantitzades, OutputPathPrediction);
                break;

            case 8:
                System.out.println("DESPREDICCIÓ (RECONSTRUCCIÓ) D'IMATGES");
                System.out.println("    -----------------------    ");
                // NOTA: Cal corregir aquesta ruta a /prediction per carregar els residus P_*
                String InputPathResidus = outputPath + "/prediction";
                String OutputPathReconstruccio = outputPath + "/reconstruccio";
                processor.deprediction(InputPathResidus, OutputPathReconstruccio);
                break;

            case 9:
                System.out.println("CALCULANT MÈTRIQUES DE DISTORSIÓ (MSE i PAE)");
                System.out.println("    -----------------------    ");
                String OriginalPath = inputPath; // Carpeta RAWs originals
                String CompressedPath = outputPath + "/quantitzades";
                processor.calculateDistortionMetrics(OriginalPath, CompressedPath);
                break;

            default:
                System.out.print("Opció " + option + " no reconeguda.\n");
                break;
        }

    }

    public boolean isFinished() {
        return isFinished;
    }
}
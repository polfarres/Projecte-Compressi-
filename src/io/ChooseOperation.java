package io;
import processor.ImageProcessor;

import java.util.Scanner;

public class ChooseOperation {

    private String inputOption; // Ara guardem l'entrada com a String
    Scanner input = new Scanner(System.in);
    private boolean isFinished = false;


    public void readOption() {
        System.out.println("Introdueix una opció (1-10 o 's'): ");
        System.out.println("1.- Llegir Imatges ");
        System.out.println("2.- Calcular entropia Total de les imatges (ordre 0)");
        System.out.println("3.- Entropía condicionada grau (correlació de pixels)");
        System.out.println("4.- (NO FA RES) Entropía condicionada 4 píxels");
        System.out.println("5.- Quantització d'imatges");
        System.out.println("6.- DeQuantització d'imatges");
        System.out.println("7.- Predicció DPCM (Calcula residus: Q*.raw -> P*.raw)");
        System.out.println("8.- Despredicció (Reconstrueix: P*.raw -> R*.raw)");
        System.out.println("9.- CODIFICACIÓ Completa (Quantitzar -> Predir -> Aritmètic)");
        System.out.println("10.- DESCODIFICACIÓ Completa (.ac -> R*.raw)");
        System.out.println("11.- MÉTRICAS DE DISTORSIÓN (Original vs Descodificada)");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("s.- Surt de l'aplicació");

        this.inputOption = input.next();
    }

    public void ExecuteCommand(String inputPath, String outputPath){

        if (this.inputOption.equalsIgnoreCase("s")) {
            this.isFinished = true;
            System.out.println("Sortint de l'aplicació. Adéu!");
            // Hem de tancar el Scanner quan sortim de l'aplicació
            input.close();
            return;
        }

        int option;
        try {
            option = Integer.parseInt(this.inputOption);
        } catch (NumberFormatException e) {
            System.out.print("❌ Opció invàlida. Introdueix un número (1-10) o 's'.\n");
            return;
        }


        // NOTA: Es bo crear el processador aquí, però les dades Q necessiten un Scanner nou.
        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);
        processor.uploadImages(); // Càrrega d'imatges originals

        Scanner Q_Scanner; // Scanner temporal per valors Q

        // Assegurem que l'input i l'output per defecte estiguin definits al processador
        processor.setInputFolder(inputPath);
        processor.setOutputFolder(outputPath);


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

                Q_Scanner = new Scanner(System.in);
                int q5 = Q_Scanner.nextInt();

                processor.setOutputFolder(outputPath + "/quantitzades");
                processor.imageQuantitzation(q5); // imageQuantitzation ha d'estar a ImageProcessor
                break;

            case 6:
                System.out.println("DeQuantització de imatges");
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols DeQuantitzar?:  ");
                Q_Scanner = new Scanner(System.in);

                int q6 = Q_Scanner.nextInt();

                // Llegeix dels fitxers que es van quantitzar (Opció 5)
                processor.setInputFolder(outputPath + "/quantitzades");
                // Guarda la sortida de la desquantització
                processor.setOutputFolder(outputPath + "/desquantització/Round_deQ_" + q6 + "_");

                processor.deQuantitzation(q6);
                break;

            case 7:
                System.out.println("PREDICCIÓ D'IMATGES ");
                System.out.println("    -----------------------    ");

                // INPUT: Imatges quantitzades (Q*.raw)
                processor.setInputFolder(outputPath + "/quantitzades");
                // OUTPUT: Residus (P*.raw)
                processor.setOutputFolder(outputPath + "/prediction");

                processor.prediction();
                break;

            case 8:
                System.out.println("DESPREDICCIÓ (RECONSTRUCCIÓ) D'IMATGES");
                System.out.println("    -----------------------    ");

                // CORRECCIÓ CLAU: INPUT són els residus generats per l'Opció 7
                processor.setInputFolder(outputPath + "/quantitzades");
                // OUTPUT: Imatges reconstruïdes (R*.raw)
                processor.setOutputFolder(outputPath + "/reconstruccio");

                processor.deprediction();
                break;

            case 9:
                System.out.println("CODIFICACIÓ COMPLETA (Entropy Coding)");
                System.out.println("    -----------------------    ");

                // La funció 'coder' carrega des de this.Images, per això l'uploadImages() inicial és clau.
                // Output: Fitxers comprimits (.ac)
                processor.setOutputFolder(outputPath + "/imatges-codificades");

                processor.coder();
                break;

            case 10:
                System.out.println("DESCODIFICACIÓ COMPLETA (.ac -> RAW)");
                System.out.println("    -----------------------    ");

                // INPUT: Fitxers comprimits (.ac)
                processor.setInputFolder(outputPath + "/imatges-codificades");
                // OUTPUT: Imatges Descodificades (Decoded*.raw)
                processor.setOutputFolder(outputPath + "/imatges-decodificades");

                processor.decoder();
                break;

            case 11:
                System.out.println("\n[11] CÁLCULO DE MÉTRICAS (MSE/PAE).");

                // Input (implícito): Imágenes originales en memoria.
                // OutputFolder configurado para buscar las descodificadas (Salida del paso 10)
                processor.setOutputFolder(outputPath + "/imatges-decodificades");

                processor.compareOriginalWithDecoded();
                break;


            default:
                System.out.print("❌ Opció " + option + " no reconeguda.\n");
                break;
        }

    }

    public boolean isFinished() {
        return isFinished;
    }
}
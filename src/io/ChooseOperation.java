package io;
import processor.ImageProcessor;

import java.util.Scanner;

public class ChooseOperation {

    private int option = 0;

    public void readOption() {

        Scanner input = new Scanner(System.in);

        System.out.println("Introdueix una opció (1-4): ");
        System.out.println("1.- Llegir Imatges ");
        System.out.println("2.- Calcular entropia Total de les imatges (ordre 0)");
        System.out.println("3.- Entropía condicionada grau (correlació de pixels)");
        System.out.println("4.- Calcular entropia condicionada (ordre 1)");
        this.option = input.nextInt();

        input.close();
    }

    public void ExecuteCommand(String inputPath, String outputPath){
        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);

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
                System.out.println("Entropía condicionada (correlació de pixels?) ");
                System.out.println("    -----------------------    ");
                break;
            default:
                System.out.print("Amore, t'has equivocat ");
                break;
        }

    }
}


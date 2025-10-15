package io;
import processor.ImageProcessor;

import java.util.Scanner;

public class ChooseOperation {

    private int option = 0;

    public void readOption() {

        Scanner input = new Scanner(System.in);

        System.out.print("Introdueix una opció (1-4): ");
        System.out.print("1.- Llegir Imatges ");
        System.out.print("2.- Calcular entropia Total de les imatges (ordre 0)");
        System.out.print("3.- Calculsr entropia condicionada pixel esquerra (ordre 1)");
        System.out.print("3.- Calculsr entropia condicionada pixel dreta (ordre 1)");
        int opcio = input.nextInt();

        this.option = opcio;

        input.close();
    }

    public void ExecuteCommand(String inputPath, String outputPath){


        switch (this.option) {
            case 1:
                ImageProcessor processor = new ImageProcessor(inputPath, outputPath);
                processor.processAll();
                break;
            case 2:
                System.out.print("Entropía total de les imatges: ");
                break;
            case 3:
                System.out.print("Entropía condicionada (pixel esquerra) ");
                break;
            case 4:
                System.out.print("Entropía condicionada (pixel dreta) ");
                break;
            default:
                System.out.print("Amore, t'has equivocat ");
                break;
        }

    }
}


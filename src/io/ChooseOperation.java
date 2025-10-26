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
                processor.imageQuantitzation(Q.nextInt(), outputPath);
                break;
            case 6:
                System.out.println("DeQuantització de imatges");
                System.out.println("    -----------------------    ");
                processor.deQuantitzation(inputPath ,outputPath);
                break;
            default:
                System.out.print("Amore, t'has equivocat ");
                break;


        }
        input.close();

    }
}


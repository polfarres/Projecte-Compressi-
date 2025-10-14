package io;


import java.util.Scanner;

public class ChooseOperation {

    private int option = 0;

    public void readOption() {

        Scanner input = new Scanner(System.in);

        System.out.println("Introdueix una opció (1-4):");
        System.out.println("1.- Llegir Imatge");
        System.out.println("2.- Calcular entropia (ordre 0)");
        System.out.println("3.- Calcular entropia condicionada (ordre 1)");

        System.out.print("Opció: ");
        int opcio = input.nextInt();

        this.option = opcio;

        input.close();
    }

    public int getOption(){
        return this.option;
    }
}


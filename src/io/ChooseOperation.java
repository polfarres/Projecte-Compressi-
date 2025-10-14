package io;


import java.util.Scanner;

public class ChooseOperation {

    private int option = 0;

    public void readOption() {

        Scanner input = new Scanner(System.in);

        System.out.print("Introdueix una opció (1-4): ");
        System.out.print("1.- Llegir Imatge ");
        System.out.print("2.- Calcular entropia (ordre 0)");
        System.out.print("3.- Calculsr entropia condicionada (ordre 1)");
        int opcio = input.nextInt();

        this.option = opcio;

        input.close();
    }

    public int getOption(){
        return this.option;
    }
}


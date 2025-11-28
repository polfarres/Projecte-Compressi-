package io;
import processor.ImageProcessor;

import java.util.Scanner;
import utils.TerminalUtils;

public class ChooseOperation {

    //Options varaibles
    private String inputOption;
    Scanner input = new Scanner(System.in);
    private boolean isFinished = false;

    //Spinner
    TerminalUtils.Spinner spinner = new TerminalUtils.Spinner();

    //Paths
    ImageProcessor imageProcessor = new ImageProcessor();

    //Menu optins
    private enum Mode { DEBUG, PRODUCTION }
    private Mode currentMode = Mode.DEBUG;


    public void readOption() {
        // First ask for mode selection
        String[] modes = new String[]{"Debug (development)", "Production (encode/decode/metrics)", "Exit"};
        int modeChoice = TerminalUtils.chooseOption("Selecciona el mode d'execució:", modes);

        if (modeChoice == 2) { // Exit
            this.inputOption = "exit";
            return;
        }

        currentMode = (modeChoice == 0) ? Mode.DEBUG : Mode.PRODUCTION;

        // Build menu based on mode
        String[] debugOptions = new String[]{
                "Llegir Imatges",
                "Calcular entropia Total de les imatges (ordre 0)",
                "Entropía condicionada grau (correlació de pixels)",
                "(NO FA RES) Entropía condicionada 4 píxels",
                "Quantització d'imatges",
                "DeQuantització d'imatges",
                "Predicció DPCM (Calcula residus: Q*.raw -> P*.raw)",
                "Despredicció (Reconstrueix: P*.raw -> R*.raw)",
                "s.- Go Back"
        };

        String[] prodOptions = new String[]{
                "CODIFICACIÓ Completa (Quantitzar -> Predir -> Aritmètic)",
                "DESCODIFICACIÓ Completa (.ac -> R*.raw)",
                "MÉTRICAS DE DISTORSIÓN (Original vs Descodificada)",
                "s.- Go Back"
        };

        String[] menu = (currentMode == Mode.DEBUG) ? debugOptions : prodOptions;

        int sel = TerminalUtils.chooseOption("Opcions (" + currentMode.name() + "):", menu);

        // Map selection back to inputOption string (numbers or 's')
        if (sel >= 0 && sel < menu.length - 1) {
            // menu entries don't include their numeric labels, so return the numeric choice

            this.inputOption = Integer.toString(sel + 1);
            if(currentMode == Mode.PRODUCTION){
                // Adjust for production mode offset
                this.inputOption = Integer.toString(sel + 9); // Opcions 9, 10, 11 en mode producció
            }
        } else {
            this.inputOption = "s"; // Exit chosen or fallback
        }
    }

    public void ExecuteCommand(){

        if (this.inputOption.equalsIgnoreCase("s")) {
            //Retornem al menú anterior
            return;
        }
        if (this.inputOption.equalsIgnoreCase("exit")) {
            this.isFinished = true;
            System.out.println("Sortint del programa...");
            input.close();
            return;
        }

        int option;
        try {
            option = Integer.parseInt(this.inputOption);
        } catch (NumberFormatException e) {
            System.out.print(TerminalUtils.RED + "Opció invàlida. Introdueix un número vàlid o 's'.\n" + TerminalUtils.RESET);
            return;
        }


        ImageProcessor processor = new ImageProcessor();
        Scanner Q_Scanner; // Scanner temporal per valors Q

        setInputImage();

        switch (option) {
            case 1:
                System.out.println(TerminalUtils.CYAN + "Procesamiento io de las imagenes en raw." + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                spinner.start("Processant imatges...");
                //processor.processAll();
                spinner.stop();
                break;
            case 2:
                System.out.println(TerminalUtils.YELLOW + "Entropía total de les imatges: " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                //processor.calculateImageEntropy();
                break;
            case 3:
                System.out.println(TerminalUtils.YELLOW + "Entropía condicionada grau (correlació de pixels) " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                //processor.calculateConditionalEntropy();
                break;
            case 4:
                System.out.println(TerminalUtils.YELLOW + "Entropía condicionada (correlació de 4 píxels propers) " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                System.out.println("   Work in progress   ");
                break;
            case 5:
                System.out.println(TerminalUtils.GREEN + "Quantització de imatges" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols quantitzar?:  ");

                Q_Scanner = new Scanner(System.in);
                int q5 = Q_Scanner.nextInt();

                //processor.setOutputFolder(outputPath + "/quantitzades");
                spinner.start("Quantitzant (Q=" + q5 + ")...");
                //processor.imageQuantitzation(q5); // imageQuantitzation ha d'estar a ImageProcessor
                spinner.stop();
                break;

            case 6:
                System.out.println(TerminalUtils.GREEN + "DeQuantització de imatges" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols DeQuantitzar?:  ");
                Q_Scanner = new Scanner(System.in);

                int q6 = Q_Scanner.nextInt();

                // Llegeix dels fitxers que es van quantitzar (Opció 5)
                //processor.setInputFolder(outputPath + "/quantitzades");
                // Guarda la sortida de la desquantització
                //processor.setOutputFolder(outputPath + "/desquantització/Round_deQ_" + q6 + "_");

                spinner.start("DeQuantitzant (Q=" + q6 + ")...");
                //processor.deQuantitzation(q6);
                spinner.stop();
                break;

            case 7:
                System.out.println(TerminalUtils.BLUE + "PREDICCIÓ D'IMATGES " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                // INPUT: Imatges quantitzades (Q*.raw)
                //processor.setInputFolder(outputPath + "/quantitzades");
                // OUTPUT: Residus (P*.raw)
                //processor.setOutputFolder(outputPath + "/prediction");

                spinner.start("Calculant residus (DPCM)...");
                //processor.prediction();
                spinner.stop();
                break;

            case 8:
                System.out.println(TerminalUtils.BLUE + "DESPREDICCIÓ (RECONSTRUCCIÓ) D'IMATGES" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                // CORRECCIÓ CLAU: INPUT són els residus generats per l'Opció 7
                //processor.setInputFolder(outputPath + "/quantitzades");
                // OUTPUT: Imatges reconstruïdes (R*.raw)
                //processor.setOutputFolder(outputPath + "/reconstruccio");

                spinner.start("Reconstruint imatges...");
                //processor.deprediction();
                spinner.stop();
                break;

            case 9:
                System.out.println(TerminalUtils.MAGENTA + "CODIFICACIÓ COMPLETA (Entropy Coding)" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                // La funció 'coder' carrega des de this.Images, per això l'uploadImages() inicial és clau.
                // Output: Fitxers comprimits (.ac)
                //processor.setOutputFolder(outputPath + "/imatges-codificades");

                spinner.start("Codificant imatges (entropy coding)...");
                //processor.coder();
                spinner.stop();
                break;

            case 10:
                System.out.println(TerminalUtils.MAGENTA + "DESCODIFICACIÓ COMPLETA (.ac -> RAW)" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                // INPUT: Fitxers comprimits (.ac)
                //processor.setInputFolder(outputPath + "/imatges-codificades");
                // OUTPUT: Imatges Descodificades (Decoded*.raw)
                //processor.setOutputFolder(outputPath + "/imatges-decodificades");

                spinner.start("Descodificant imatges...");
                //processor.decoder();
                spinner.stop();
                break;

            case 11:
                System.out.println(TerminalUtils.GREEN + "\n[11] CÁLCULO DE MÉTRICAS (MSE/PAE)." + TerminalUtils.RESET);

                // Input (implícit): Imatges originals en memòria.
                // OutputFolder configurat per buscar les descodificades (Sortida del pas 10)
                //processor.setOutputFolder(outputPath + "/imatges-decodificades");

                //processor.compareOriginalWithDecoded();
                break;


            default:
                System.out.print(TerminalUtils.RED + "Opció " + option + " no reconeguda.\n" + TerminalUtils.RESET);
                break;
        }

    }

    public void setInputImage(){
        System.out.println("Introdueix el path de la imatge d'entrada (raw): ");
        String path = input.nextLine();
        //imageProcessor.uploadImage(path);
    }

    public boolean isFinished() {
        return isFinished;
    }
}
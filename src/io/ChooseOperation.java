package io;
import image.Image;
import processor.ImageProcessor;

import java.util.Scanner;
import utils.TerminalUtils;

public class ChooseOperation {

    //Options varaibles
    private String inputOption;
    Scanner input = new Scanner(System.in);
    private boolean isFinished = false;

    //Spinner UI
    TerminalUtils.Spinner spinner = new TerminalUtils.Spinner();

    //Principal classe de procesamiento
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
                "Entropía condicionada 4 píxels cardinals",
                "Quantització d'imatges",
                "DeQuantització d'imatges",
                "Predicció DPCM (Calcula residus: Q*.raw -> P*.raw)",
                "Despredicció (Reconstrueix: P*.raw -> R*.raw)",
                "Press 's'.- Go Back"
        };

        String[] prodOptions = new String[]{
                "CODIFICACIÓ Completa (Decorrelació -> Quantització -> Codificació Aritmética: .ac)",
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
    } //✅

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

        Scanner Q_Scanner; // Scanner temporal per valors Q

        setInputImage();

        switch (option) {
            case 1:
                System.out.println(TerminalUtils.CYAN + "Lectura io de la imagen en raw." + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                Image img = imageProcessor.readImage();
                break;
            case 2:
                System.out.println(TerminalUtils.YELLOW + "Entropía total de la imatge: " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                imageProcessor.calculateImageEntropyTest();
                break;
            case 3:
                System.out.println(TerminalUtils.YELLOW + "Entropía condicionada grau (correlació de pixels) " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                spinner.start("Processant...");
                imageProcessor.calculateConditionalEntropyTest();
                spinner.stop();
                break;
            case 4:
                System.out.println(TerminalUtils.YELLOW + "Entropía condicionada (correlació de 4 píxels propers) " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                spinner.start("Processant...");
                imageProcessor.calculateConditionalEntropy4PixelsTest();
                spinner.stop();
                break;
            case 5:
                System.out.println(TerminalUtils.GREEN + "Quantització de imatges" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");
                System.out.print("    Quin es el valor de Q per el qual vols quantitzar?:  ");

                Q_Scanner = new Scanner(System.in);
                int q = Q_Scanner.nextInt();

                //processor.setOutputFolder(outputPath + "/quantitzades");
                spinner.start("Quantitzant (Q=" + q + ")...");
                imageProcessor.imageQuantitzationTest(q); // imageQuantitzation ha d'estar a ImageProcessor
                spinner.stop();
                break;

            case 6:
                System.out.println(TerminalUtils.GREEN + "DeQuantització de imatges" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                spinner.start("DeQuantitzant...");
                imageProcessor.deQuantitzationTest();
                spinner.stop();
                break;

            case 7:
                System.out.println(TerminalUtils.BLUE + "PREDICCIÓ D'IMATGES " + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                spinner.start("Calculant residus (DPCM)...");
                imageProcessor.predictionTest();
                spinner.stop();
                break;

            case 8:
                System.out.println(TerminalUtils.BLUE + "DESPREDICCIÓ (RECONSTRUCCIÓ) D'IMATGES" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                spinner.start("Reconstruint imatges...");
                imageProcessor.depredictionTest();
                spinner.stop();
                break;

            case 9:
                System.out.println(TerminalUtils.MAGENTA + "CODIFICACIÓ COMPLETA (Entropy Coding)" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                spinner.start("Codificant imatge (arithmetic encoding)...");
                imageProcessor.compressImage();
                spinner.stop();
                break;

            case 10:
                System.out.println(TerminalUtils.MAGENTA + "DESCODIFICACIÓ COMPLETA (.ac -> RAW)" + TerminalUtils.RESET);
                System.out.println("    -----------------------    ");

                spinner.start("Descodificant imatges...");
                imageProcessor.decoder();
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
        System.out.println("Introdueix el path de la imatge d'entrada (raw o ac): ");
        String path = input.nextLine();
        imageProcessor.uploadImage(path);
    }

    public boolean isFinished() {
        return isFinished;
    }
}
package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TerminalUtils {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String REVERSE = "\u001B[7m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String MAGENTA = "\u001B[35m";

    // Clear the screen (works on most ANSI terminals)
    public static void clearScreen() {
        System.out.print("\u001b[2J\u001b[H");
        System.out.flush();
    }

    public static int chooseOption(String prompt, String[] options) {
        int current = 0;

        // Fallback: ask for numeric input using Scanner
        clearScreen();
        System.out.println(BOLD + prompt + RESET + "\n");
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        System.out.println("\n(Type the option number and press Enter)");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = "";
            try {
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine().trim();
                } else {
                    return 0; // EOF
                }
            } catch (Exception e) {
                return 0;
            }
            if (line.length() == 0) continue;
            // allow entering the numeric index or 's' for exit
            if (line.equalsIgnoreCase("s")) return options.length - 1; // keep previous behavior
            try {
                int num = Integer.parseInt(line);
                if (num >= 1 && num <= options.length) return num - 1;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Entrada invàlida. Introdueix un número entre 1 i " + options.length + ".");
        }

    }

    // Simple spinner to indicate progress. Start in a new thread and call stop() to finish.
    public static class Spinner {
        private volatile boolean spinning = false;
        private Thread thread;

        public void start(final String message) {
            if (spinning) return;
            spinning = true;
            thread = new Thread(() -> {
                char[] spinChars = new char[]{'|', '/', '-', '\\'};
                int i = 0;
                try {
                    while (spinning) {
                        System.out.print("\r" + message + " " + spinChars[i++ % spinChars.length]);
                        System.out.flush();
                        Thread.sleep(120);
                    }
                } catch (InterruptedException ignored) {
                }
                System.out.print("\r" + message + " " + GREEN + "✓" + RESET + "\n");
                System.out.flush();
            });
            thread.setDaemon(true);
            thread.start();
        }

        public void stop() {
            spinning = false;
            if (thread != null) {
                try {
                    thread.join(500);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

}

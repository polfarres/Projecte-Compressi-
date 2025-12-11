package stages;

import image.Image;

public class Quantitzation {

        public static int Q_STEP = 10; // Valor per defecte

        public static Quantitzation init(int qStep) {
            if (qStep <= 0) throw new IllegalArgumentException("qStep must be > 0");

            Q_STEP = qStep;
            return new Quantitzation();
        }

    public void quanticiseDeadZone(Image image) {


            // ----- Quantizice function -----
        if (Q_STEP != 1){
        for (int b = 0; b < image.bands; b++) { // bands
            for (int x = 0; x < image.height; x++) { // width
                for (int y = 0; y < image.width; y++) { // height
                    int val = image.img[b][x][y];

                    // Formula de cuantización
                    int sign = Integer.signum(val);
                    int absVal = Math.abs(val);

                    int quantized;
                    if (absVal <= Q_STEP) {
                        quantized = 0;
                    } else {
                        quantized = sign * (absVal  / Q_STEP); // Hace un floor automático por ser Integer
                    }

                    image.img[b][x][y] = quantized;
                }
            }
        }
        }

    } //✅

    public void dequanticiseDeadZone(Image image) {
          if (Q_STEP != 1){
        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {
                for (int y = 0; y < image.width; y++) {

                    int q = image.img[b][x][y];
                    image.img[b][x][y] = q * Q_STEP;


                }
            }
        }
    }
    }

    public void quanticiseDeadZoneImproved(Image image) {
        if (Q_STEP != 1) {
            for (int b = 0; b < image.bands; b++) {
                for (int x = 0; x < image.height; x++) {
                    for (int y = 0; y < image.width; y++) {
                        int val = image.img[b][x][y];
                        int absVal = Math.abs(val);
                        int sign = Integer.signum(val);

                        int quantized;

                        // --- DEAD ZONE LOGIC ---
                        // Definim la zona morta. Si és menor que Q_STEP, es converteix en 0.
                        // Això crea una Dead Zone efectiva d'amplada 2 * Q_STEP (de -Q a +Q)
                        if (absVal < Q_STEP) {
                            quantized = 0;
                        } else {
                            // AQUI ESTÀ EL CANVI CLAU:
                            // No divideixis per 2*Q_STEP. Divideix per Q_STEP normal.
                            // Això manté la precisió en els valors alts.
                            quantized = sign * (absVal / Q_STEP);
                        }

                        image.img[b][x][y] = quantized;
                    }
                }
            }
        }
    }

    public void dequanticiseDeadZoneImproved(Image image) {
        if (Q_STEP != 1) {
            for (int b = 0; b < image.bands; b++) {
                for (int x = 0; x < image.height; x++) {
                    for (int y = 0; y < image.width; y++) {
                        int q = image.img[b][x][y];

                        if (q == 0) {
                            image.img[b][x][y] = 0;
                        } else {
                            int sign = Integer.signum(q);
                            int absQ = Math.abs(q);

                            // --- RECONSTRUCTION WITH OFFSET (CENTERING) ---
                            // Truc per guanyar PSNR: Reconstruir al centre de l'interval.
                            // Fórmula: (q * step) + (step / 2)
                            // Això redueix l'error mitjà quadràtic (MSE).
                            int reconstruction = (absQ * Q_STEP) + (int)(Q_STEP / 2.0);

                            image.img[b][x][y] = sign * reconstruction;
                        }
                    }
                }
            }
        }
    }

}

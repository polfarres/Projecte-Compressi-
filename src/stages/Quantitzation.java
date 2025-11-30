package stages;

import image.Image;

public class Quantitzation {

        public static int Q_STEP = 5;

        public static Quantitzation init(int qStep) {
            if (qStep <= 0) throw new IllegalArgumentException("qStep must be > 0");
            Q_STEP = qStep;
            return new Quantitzation();
        }

    public void quanticiseDeadZone(Image image) {


            // ----- Quantizice function -----
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

    } //✅

    public void dequanticiseDeadZone(Image image) {

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

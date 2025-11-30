package stages;

import image.Image;

public class PredictorDPCM {

    public void aplicarPrediccioPixelAnterior(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {

                // Para el primer píxel de cada fila no hay izquierdo → lo dejamos igual
                int prevPixel = image.img[b][x][0];

                for (int y = 1; y < image.width; y++) {

                    int actual = image.img[b][x][y];

                    // Predictor: píxel de la izquierda
                    int predicho = prevPixel;

                    // Calculamos el error de predicción
                    int error = actual - predicho;

                    // Guardamos el error en la imagen
                    image.img[b][x][y] = error;

                    // Actualizamos prevPixel usando el actual
                    prevPixel = actual;
                }
            }
        }
    }

    public void desferPrediccioPixelAnterior(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {

                // El primer píxel de cada fila NO se predijo → está tal cual
                int reconstructed = image.img[b][x][0];

                for (int y = 1; y < image.width; y++) {

                    int error = image.img[b][x][y];

                    // Predictor: píxel de la izquierda (ya reconstruido)
                    int predicho = reconstructed;

                    // Reconstrucción del valor real
                    int pixelReal = predicho + error;

                    // Guardamos el valor restaurado
                    image.img[b][x][y] = pixelReal;

                    // Actualizamos reconstructed con el píxel real recién restaurado
                    reconstructed = pixelReal;
                }
            }
        }
    }

    public void aplicarPrediccio3Veins(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {
                for (int y = 0; y < image.width; y++) {

                    int pred;

                    // Regiones de borde
                    if (x == 0 && y == 0) {
                        pred = 0; // primer píxel, sin vecinos
                    }
                    else if (x == 0) {
                        pred = image.img[b][x][y - 1]; // solo izquierda
                    }
                    else if (y == 0) {
                        pred = image.img[b][x - 1][y]; // solo arriba
                    }
                    else {
                        // Vecinos disponibles
                        int L  = image.img[b][x][y - 1];
                        int U  = image.img[b][x - 1][y];
                        int UL = image.img[b][x - 1][y - 1];

                        // Predictor LOCO-I
                        if (UL >= Math.max(L, U)) {
                            pred = Math.min(L, U);
                        }
                        else if (UL <= Math.min(L, U)) {
                            pred = Math.max(L, U);
                        } else {
                            pred = L + U - UL;
                        }
                    }

                    int actual = image.img[b][x][y];
                    int error = actual - pred;

                    image.img[b][x][y] = error;
                }
            }
        }
    } //Lo he pro y es una mierda, la entropia empeora

    public void desferPrediccio3Veins(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {
                for (int y = 0; y < image.width; y++) {

                    int pred;

                    if (x == 0 && y == 0) {
                        pred = 0;
                    }
                    else if (x == 0) {
                        pred = image.img[b][x][y - 1];
                    }
                    else if (y == 0) {
                        pred = image.img[b][x - 1][y];
                    }
                    else {
                        int L  = image.img[b][x][y - 1];
                        int U  = image.img[b][x - 1][y];
                        int UL = image.img[b][x - 1][y - 1];

                        if (UL >= Math.max(L, U)) {
                            pred = Math.min(L, U);
                        }
                        else if (UL <= Math.min(L, U)) {
                            pred = Math.max(L, U);
                        } else {
                            pred = L + U - UL;
                        }
                    }

                    int error = image.img[b][x][y];
                    int pixelReal = pred + error;

                    image.img[b][x][y] = pixelReal;
                }
            }
        }
    }



}
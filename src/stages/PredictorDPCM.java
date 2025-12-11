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

    public void aplicarlinealFunctionDPCM(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {

                int prev2Pixel = image.img[b][x][0];
                int prevPixel = image.img[b][x][1];

                for (int y = 2; y < image.width; y++) {

                    int actual = image.img[b][x][y];

                    int predicho = 2 * prevPixel - prev2Pixel;

                    int error = actual - predicho;

                    image.img[b][x][y] = error;

                    // Actualización correcta
                    prev2Pixel = prevPixel;
                    prevPixel = actual;
                }
            }
        }
    }

    public void desferLinealFunctionDPCM(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {

                int reconstructed2 = image.img[b][x][0];
                int reconstructed = image.img[b][x][1];

                for (int y = 2; y < image.width; y++) {

                    int error = image.img[b][x][y];

                    // Reconstrucción correcta:
                    int pixelReal = 2 * reconstructed - reconstructed2 + error;

                    image.img[b][x][y] = pixelReal;

                    // Actualización correcta
                    reconstructed2 = reconstructed;
                    reconstructed = pixelReal;
                }
            }
        }
    }

    public void aplicarMED(Image image) {

        for (int b = image.bands - 1; b >= 0; b--) {
            for (int x = image.height - 1; x >= 0; x--) {
                for (int y = image.width - 1; y >= 0; y--) {

                    int actual = image.img[b][x][y];

                    int A, B, C;

                    if (x == 0 || y ==0){
                        A = B = C = 0;
                    } else {
                        A = image.img[b][x][y - 1]; // Izquierda
                        B = image.img[b][x - 1][y]; // Arriba
                        C = image.img[b][x - 1][y - 1]; // Diagonal superior izquierda
                    }

                    int predicho;

                    if (C >= Math.max(A, B)) {
                        predicho = Math.min(A, B);
                    } else if (C <= Math.min(A, B)) {
                        predicho = Math.max(A, B);
                    } else {
                        predicho = A + B - C;
                    }

                    int error = actual - predicho;

                    image.img[b][x][y] = error;
                }
            }
        }

    }

    public void desferMED(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {
                for (int y = 0; y < image.width; y++) {

                    int error = image.img[b][x][y];

                    int A, B, C;

                    if (x == 0 || y ==0){
                        A = B = C = 0;
                    } else {
                        A = image.img[b][x][y - 1]; // Izquierda
                        B = image.img[b][x - 1][y]; // Arriba
                        C = image.img[b][x - 1][y - 1]; // Diagonal superior izquierda
                    }

                    int predicho;

                    if (C >= Math.max(A, B)) {
                        predicho = Math.min(A, B);
                    } else if (C <= Math.min(A, B)) {
                        predicho = Math.max(A, B);
                    } else {
                        predicho = A + B - C;
                    }

                    int pixelReal = predicho + error;

                    image.img[b][x][y] = pixelReal;
                }
            }
        }
    }

    public void aplicarLOCOI(Image image) {


        for (int b = image.bands - 1; b >= 0; b--) {
            for (int x = image.height - 1; x >= 0; x--) {
                for (int y = image.width - 1; y >= 0; y--) {

                    int actual = image.img[b][x][y];

                    int A, B, C;

                    if (x == 0 || y ==0){
                        A = B = C = 0;
                    } else {
                        A = image.img[b][x][y - 1]; // Izquierda
                        B = image.img[b][x - 1][y]; // Arriba
                        C = image.img[b][x - 1][y - 1]; // Diagonal superior izquierda
                    }

                    int predicho;

                    if (B == C) {
                        predicho = A;
                    } else if (A == C) {
                        predicho = B;
                    } else {
                        predicho = A + B - C;
                    }

                    predicho = Math.max(Math.min(predicho, Math.max(A, B)), Math.min(A, B));

                    int error = actual - predicho;

                    image.img[b][x][y] = error;
                }
            }
        }

    }

    public void desferLOCOI(Image image) {

        for (int b = 0; b < image.bands; b++) {
            for (int x = 0; x < image.height; x++) {
                for (int y = 0; y < image.width; y++) {

                    int error = image.img[b][x][y];

                    int A, B, C;

                    if (x == 0 || y ==0){
                        A = B = C = 0;
                    } else {
                        A = image.img[b][x][y - 1]; // Izquierda
                        B = image.img[b][x - 1][y]; // Arriba
                        C = image.img[b][x - 1][y - 1]; // Diagonal superior izquierda
                    }

                    int predicho;

                    if (B == C) {
                        predicho = A;
                    } else if (A == C) {
                        predicho = B;
                    } else {
                        predicho = A + B - C;
                    }

                    predicho = Math.max(Math.min(predicho, Math.max(A, B)), Math.min(A, B));

                    int pixelReal = predicho + error;

                    image.img[b][x][y] = pixelReal;
                }
            }
        }
    }

    public void aplicarGAP(Image image) {

        for (int b = image.bands - 1; b >= 0; b--) {
            for (int x = image.height - 1; x >= 2; x--) {
                for (int y = image.width - 2; y >= 2; y--) {
                    int N = image.img[b][x - 1][y];
                    int W = image.img[b][x][y - 1];
                    int NW = image.img[b][x - 1][y - 1];
                    int NE = image.img[b][x - 1][y + 1];
                    int WW = image.img[b][x][y - 2];
                    int NN = image.img[b][x - 2][y];
                    int NNE = image.img[b][x - 2][y + 1];

                    int gv = (Math.abs(W - WW) + Math.abs(N - NW) + Math.abs(N - NE));
                    int gh = (Math.abs(W - NW) + Math.abs(W - NN) + Math.abs(NE - NNE));

                    int predicho;

                    if ((gv - gh) > 80) {
                        predicho = W;
                    } else if ((gh - gv) < -80) {
                        predicho = N;
                    } else {
                        predicho = (W + N) / 2 + (NE - NW) / 4;

                        if ((gv - gh) > 32) {
                            predicho = (predicho + W) / 2;
                        } else if ((gv - gh) > 8) {
                            predicho = (3 * predicho + W) / 4;
                        } else if ((gv - gh) < -32) {
                            predicho = (predicho + N) / 2;
                        } else if ((gv - gh) < -8) {
                            predicho = (3 * predicho + N) / 4;

                        }
                    }

                    // Valor original
                    int real = image.img[b][x][y];

                    // Error que guardamos en la imagen
                    int error = real - predicho;

                    image.img[b][x][y] = error;


                }
            }

        }
    }

    public void desferGAP(Image image) {
        for (int b = 0 ; b < image.bands; b++) {
            for (int x = 2; x < image.height; x++) {
                for (int y = 2; y < image.width - 1; y++) {

                    int N = image.img[b][x - 1][y];
                    int W = image.img[b][x][y - 1];
                    int NW = image.img[b][x - 1][y - 1];
                    int NE = image.img[b][x - 1][y + 1];
                    int WW = image.img[b][x][y - 2];
                    int NN = image.img[b][x - 2][y];
                    int NNE = image.img[b][x - 2][y + 1];

                    int gv = (Math.abs(W - WW) + Math.abs(N - NW) + Math.abs(N - NE));
                    int gh = (Math.abs(W - NW) + Math.abs(W - NN) + Math.abs(NE - NNE));

                    int predicho;

                    if ((gv - gh) > 80) {
                        predicho = W;
                    } else if ((gh - gv) < -80) {
                        predicho = N;
                    } else {
                        predicho = (W + N) / 2 + (NE - NW) / 4;

                        if ((gv - gh) > 32) {
                            predicho = (predicho + W) / 2;
                        } else if ((gv - gh) > 8) {
                            predicho = (3 * predicho + W) / 4;
                        } else if ((gv - gh) < -32) {
                            predicho = (predicho + N) / 2;
                        } else if ((gv - gh) < -8) {
                            predicho = (3 * predicho + N) / 4;

                        }
                    }

                    // Valor original
                    int error = image.img[b][x][y];

                    // Error que guardamos en la imagen
                    int pixelReal = error + predicho;

                    image.img[b][x][y] = pixelReal;


                }
            }

        }
    }


    // --- BEST ---- //
    public void aplicarIMED(Image image) {
        aplicarMED(image);

        for (int b = 0; b < image.bands; b++) {

            // ---- FILA 0 (predicción horizontal) ----

            int prevPixel = image.img[b][0][0];

            for (int y = 1; y < image.width; y++) {

                int actual = image.img[b][0][y];
                int predicho = prevPixel;

                int error = actual - predicho;
                image.img[b][0][y] = error;

                prevPixel = actual;  // avanzar correctamente
            }


            // ---- COLUMNA 0 (predicción vertical) ----

            int prevPixelVertical = image.img[b][0][0];

            for (int x = 1; x < image.height; x++) {

                int actual = image.img[b][x][0];
                int predicho = prevPixelVertical;

                int error = actual - predicho;
                image.img[b][x][0] = error;

                prevPixelVertical = actual; // avanzar correctamente
            }
        }
    }

    public void desferIMED(Image image) {


        for (int b = 0; b < image.bands; b++) {

            // ---- FILA 0 ----
            int reconstructed = image.img[b][0][0];

            for (int y = 1; y < image.width; y++) {

                int error = image.img[b][0][y];
                int predicho = reconstructed;

                int pixelReal = predicho + error;

                image.img[b][0][y] = pixelReal;
                reconstructed = pixelReal; // avanzar correctamente
            }


            // ---- COLUMNA 0 ----
            int reconstructedVertical = image.img[b][0][0];

            for (int x = 1; x < image.height; x++) {

                int error = image.img[b][x][0];
                int predicho = reconstructedVertical;

                int pixelReal = predicho + error;

                image.img[b][x][0] = pixelReal;
                reconstructedVertical = pixelReal; // avanzar correctamente
            }
        }

        desferMED(image);
    }

}
package stages;

import image.Image;
import java.util.Arrays;

public class SmoothingKernel {

    // MEDIAN FILTER 8x8
    private static final int KERNEL_SIZE = 3;
    private static final int WINDOW_PIXELS = KERNEL_SIZE * KERNEL_SIZE;

    public void MedianFilter(Image img) {

        int width = img.width;
        int height = img.height;
        int bands = img.bands;

        int[][][] output = new int[bands][height][width];

        int edgex = KERNEL_SIZE / 2; // 4
        int edgey = KERNEL_SIZE / 2; // 4

        int[] window = new int[WINDOW_PIXELS];

        for (int b = 0; b < bands; b++) {

            // Inicializa salida con la imagen original (para bordes)
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    output[b][x][y] = img.img[b][x][y];
                }
            }

            for (int x = edgex; x < height - edgex; x++) {
                for (int y = edgey; y < width - edgey; y++) {

                    int i = 0;

                    // Construye la ventana 8x8
                    for (int fx = 0; fx < KERNEL_SIZE; fx++) {
                        for (int fy = 0; fy < KERNEL_SIZE; fy++) {
                            window[i] = img.img[b][x + fx - edgex][y + fy - edgey];
                            i++;
                        }
                    }

                    Arrays.sort(window);

                    output[b][x][y] = window[WINDOW_PIXELS / 2]; // Valor mediano
                }
            }
        }

        // Copia al objeto imagen
        for (int b = 0; b < bands; b++) {
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    img.img[b][x][y] = output[b][x][y];
                }
            }
        }
    }
}

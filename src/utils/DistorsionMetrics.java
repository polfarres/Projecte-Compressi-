package utils;

public class DistorsionMetrics {
    private static double mse = 0.0;


    public static double calculateMSE(int[][][] original, int[][][] compressed) {
        if (original == null || compressed == null || original.length == 0 || original[0].length == 0 || original[0][0].length == 0) {
            return 0.0;
        }

        int bands = original.length;
        int rows = original[0].length;
        int cols = original[0][0].length;
        long sumOfSquaredErrors = 0;
        int totalPixels = bands * rows * cols;

        for (int b = 0; b < bands; b++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int error = original[b][r][c] - compressed[b][r][c];
                    sumOfSquaredErrors += (long) error * error;
                }
            }
        }

        mse = (double) sumOfSquaredErrors / totalPixels;

        return mse;
    }

    public static int calculatePeakAbsoluteError(short[][][] original, short[][][] compressed) {

        int bands = original.length;
        int rows = original[0].length;
        int cols = original[0][0].length;
        int maxAbsoluteError = 0;

        for (int b = 0; b < bands; b++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int absoluteError = Math.abs(original[b][r][c] - compressed[b][r][c]);

                    if (absoluteError > maxAbsoluteError) {
                        maxAbsoluteError = absoluteError;
                    }
                }
            }
        }

        return maxAbsoluteError;
    }
    public static double calculatePSNR(double mse, int bitsPerSample) {
        if (mse == 0) return Double.POSITIVE_INFINITY;

        // El valor màxim possible (255 per a 8 bits, 65535 per a 16 bits)

        double maxPixelValue = Math.pow(2, bitsPerSample) - 1;

        return 10 * Math.log10((maxPixelValue * maxPixelValue) / mse);
    }


}

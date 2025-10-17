package processor;

import java.util.HashMap;
import java.util.Map;

public class CalculateProbability {

    public static Map<String, Double> jointProbability(short[][][] img) {
        Map<String, Integer> countMap = new HashMap<>();
        int height = img[0].length;
        int width = img[0][0].length;
        long totalPairs = 0;

        // Recorremos píxeles horizontales
        for (int b = 0;b < img.length; b++)
            for (int i = 0; i < height - 1; i++) {
                for (int j = 0; j < width - 1; j++) {
                    short left = img[b][i][j];
                    short right = img[b][i + 1][j];
                    String key = left + "_" + right;
                    countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    totalPairs++;
                }
            }

        // Convertimos a probabilidad
        Map<String, Double> probMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            probMap.put(entry.getKey(), entry.getValue() / (double) totalPairs);
        }

        return probMap;
    }


    public static Map<Short, Double> marginalLeft(Map<String, Double> jointProb) {
        Map<Short, Double> pLeft = new HashMap<>();

        for (String key : jointProb.keySet()) {
            String[] parts = key.split("_");
            Short left = (short) Integer.parseInt(parts[0]);
            double p = jointProb.get(key);
            pLeft.put(left, pLeft.getOrDefault(left, 0.0) + p);
        }

        return pLeft;
    }


    public static Map<Short, Double> pixelProbability(short[][][] img) {
        Map<Short, Integer> countMap = new HashMap<>();
        int height = img[0].length;
        int width = img[0][0].length;
        long totalPixels = 0;

        for (int b = 0; b < img.length; b++) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    short val = img[b][i][j];
                    countMap.put(val, countMap.getOrDefault(val, 0) + 1);
                    totalPixels++;
                }
            }
        }

        // Convertimos a probabilidades
        Map<Short, Double> probs = new HashMap<>();
        for (Map.Entry<Short, Integer> entry : countMap.entrySet()) {
            probs.put(entry.getKey(), entry.getValue() / (double) totalPixels);
        }

        return probs;
    }

}


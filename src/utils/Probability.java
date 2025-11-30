package utils;

import image.Image;

import java.util.HashMap;
import java.util.Map;

public class Probability {


    //Probabilidad condicionada del píxel vecino
    public static Map<String, Double> jointProbability(int[][][] img) {
        Map<String, Integer> countMap = new HashMap<>();
        int height = img[0].length;
        int width = img[0][0].length;
        long totalPairs = 0;

        // Recorremos píxeles horizontales
        for (int b = 0;b < img.length; b++)
            for (int i = 0; i < height - 1; i++) {
                for (int j = 0; j < width - 1; j++) {
                    int left = img[b][i][j];
                    int right = img[b][i + 1][j];
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

    //Probabilidad condicionada del píxeles vecinos en las 4 cardinalidades
    public static Map<String, Double> jointProbability4(int[][][] img) {

        Map<String, Integer> countMap = new HashMap<>();
        long totalPairs = 0;

        int bands = img.length;
        int height = img[0].length;
        int width = img[0][0].length;

        for (int b = 0; b < bands; b++) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {

                    int p = img[b][i][j];
                    String key = p + "_";

                    // NORTH
                    if (i > 0) {
                        int north = img[b][i - 1][j];
                        key = key + north + "_";
                    }
                    else {
                        key = key + 0 + "_";
                    }

                    // SOUTH
                    if (i < height - 1) {
                        int south = img[b][i + 1][j];
                        key = key + south + "_";
                    }
                    else {
                        key = key + 0 + "_";
                    }

                    // EAST
                    if (j < width - 1) {
                        int east = img[b][i][j + 1];
                        key = key + east + "_";
                    }
                    else {
                        key = key + 0 + "_";
                    }

                    // WEST
                    if (j > 0) {
                        int west = img[b][i][j - 1];
                        key = key + west;

                    }
                    else {
                        key = key + 0;
                    }

                    countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    totalPairs++;
                }
            }
        }

        // Convertimos a probabilidad
        Map<String, Double> probMap = new HashMap<>();
        for (var entry : countMap.entrySet()) {
            probMap.put(entry.getKey(), entry.getValue() / (double) totalPairs);
        }

        return probMap;
    }



    public static Map<Integer, Double> marginalLeft(Map<String, Double> jointProb) {
        Map<Integer, Double> pLeft = new HashMap<>();

        for (String key : jointProb.keySet()) {
            String[] parts = key.split("_");
            Integer left = Integer.parseInt(parts[0]);
            double p = jointProb.get(key);
            pLeft.put(left, pLeft.getOrDefault(left, 0.0) + p);
        }

        return pLeft;
    }


    public static Map<Integer, Double> pixelProbability(Image image) {
        Map<Integer, Double> countMap = new HashMap<>();

        int height = image.height;
        int width = image.width;
        int bands = image.bands;
        long totalPixels = height*width*image.bands;

        for (int b = 0; b < bands; b++) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Integer val = image.img[b][i][j];
                    countMap.put(val, countMap.getOrDefault(val, (double) 0) + 1);
                }
            }
        }

        // Convertimos a probabilidades
        Map<Integer, Double> probs = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : countMap.entrySet()) {
            probs.put(entry.getKey(), entry.getValue() / (double) totalPixels);
        }

        return probs;
    }

}


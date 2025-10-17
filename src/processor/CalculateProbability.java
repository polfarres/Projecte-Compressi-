package processor;

import java.util.HashMap;
import java.util.Map;

public class CalculateProbability {

    /**
     * Calcula la distribución conjunta P(l, r) de valores de píxel izquierdo y derecho.
     * @param img matriz [alto][ancho][canales]
     * @param channel canal a usar (0 = rojo / gris, 1 = verde, 2 = azul)
     * @return Mapa con clave "l_r" y valor probabilidad conjunta p(l, r)
     */
    public static Map<String, Double> jointProbability(short[][][] img, int channel) {
        Map<String, Integer> countMap = new HashMap<>();
        int height = img.length;
        int width = img[0].length;
        long totalPairs = 0;

        // Recorremos píxeles horizontales
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width - 1; j++) {
                int left = img[i][j][channel] & 0xFFFF;   // convertir short a int sin signo
                int right = img[i][j + 1][channel] & 0xFFFF;
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

    /**
     * Calcula la distribución marginal P(l) de los valores del píxel izquierdo.
     * @param jointProb mapa P(l,r)
     * @return mapa de P(l)
     */
    public static Map<Integer, Double> marginalLeft(Map<String, Double> jointProb) {
        Map<Integer, Double> pLeft = new HashMap<>();

        for (String key : jointProb.keySet()) {
            String[] parts = key.split("_");
            int left = Integer.parseInt(parts[0]);
            double p = jointProb.get(key);
            pLeft.put(left, pLeft.getOrDefault(left, 0.0) + p);
        }

        return pLeft;
    }

    /**
     * Calcula la distribución de probabilidad P(x) de los valores de píxel
     * @param img matriz [alto][ancho][canales]
     * @param channel canal a usar (0 = rojo / gris, 1 = verde, 2 = azul)
     * @return mapa de probabilidad P(x)
     */
    public static Map<Integer, Double> pixelProbability(short[][][] img, int channel) {
        Map<Integer, Integer> countMap = new HashMap<>();
        int height = img.length;
        int width = img[0].length;
        long totalPixels = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int val = img[i][j][channel] & 0xFFFF;
                countMap.put(val, countMap.getOrDefault(val, 0) + 1);
                totalPixels++;
            }
        }

        // Convertimos a probabilidades
        Map<Integer, Double> probMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            probMap.put(entry.getKey(), entry.getValue() / (double) totalPixels);
        }

        return probMap;
    }

}


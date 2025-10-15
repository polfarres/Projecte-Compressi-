
package processor.entropy;

import java.util.HashMap;
import java.util.Map;

public class EntropyOrder0 extends EntropyCalculator {

    @Override
    public double calculate(short[][][] img) {
        Map<Short, Double> probabilities = calculateProbability(img);
        return calculateEntropyFromProbabilities(probabilities);
    }

    private Map<Short, Double> calculateProbability(short[][][] img) {
        Map<Short, Integer> countMap = new HashMap<>();
        int total = 0;

        for (int b = 0; b < img.length; b++) {
            for (int y = 0; y < img[b].length; y++) {
                for (int x = 0; x < img[b][y].length; x++) {
                    short val = img[b][y][x];
                    countMap.put(val, countMap.getOrDefault(val, 0) + 1);
                    total++;
                }
            }
        }

        Map<Short, Double> probMap = new HashMap<>();
        for (Map.Entry<Short, Integer> entry : countMap.entrySet()) {
            probMap.put(entry.getKey(), entry.getValue() / (double) total);
        }

        return probMap;
    }
}

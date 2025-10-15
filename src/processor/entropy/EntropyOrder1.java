// EntropyOrder1.java
package processor.entropy;

import java.util.HashMap;
import java.util.Map;

public class EntropyOrder1 extends EntropyCalculator {

    @Override
    public double calculate(short[][][] img) {
        Map<String, Integer> pairCount = new HashMap<>();
        Map<Short, Integer> prevCount = new HashMap<>();
        int totalPairs = 0;

        for (int b = 0; b < img.length; b++) {
            for (int y = 0; y < img[b].length; y++) {
                for (int x = 1; x < img[b][y].length; x++) {
                    short prev = img[b][y][x - 1];
                    short curr = img[b][y][x];

                    String key = prev + "," + curr;
                    pairCount.put(key, pairCount.getOrDefault(key, 0) + 1);
                    prevCount.put(prev, prevCount.getOrDefault(prev, 0) + 1);
                    totalPairs++;
                }
            }
        }

        double conditionalEntropy = 0.0;
        for (Map.Entry<String, Integer> e : pairCount.entrySet()) {
            String[] parts = e.getKey().split(",");
            short prev = Short.parseShort(parts[0]);
            double jointProb = e.getValue() / (double) totalPairs;
            double prevProb = prevCount.get(prev) / (double) totalPairs;
            conditionalEntropy += -jointProb * log2(jointProb / prevProb);
        }

        return conditionalEntropy;
    }
}

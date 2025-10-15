package processor.entropy;

import java.util.Map;

public abstract class EntropyCalculator {

    protected double log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    protected double calculateEntropyFromProbabilities(Map<?, Double> probabilities) {
        double entropy = 0.0;
        for (double p : probabilities.values()) {
            if (p > 0)
                entropy += -p * log2(p);
        }
        return entropy;
    }

    public abstract double calculate(short[][][] img);
}

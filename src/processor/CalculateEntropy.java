package processor;

import java.util.Map;

public class CalculateEntropy {

    /**
     * Calcula la entropía H(X) = -sum p(x) log2(p(x))
     */
    public static double entropy(Map<Integer, Double> p) {
        double H = 0.0;
        for (double prob : p.values()) {
            if (prob > 0)
                H -= prob * (Math.log(prob) / Math.log(2));
        }
        return H;
    }

    /**
     * Calcula la entropía conjunta H(L,R) = -sum p(l,r) log2(p(l,r))
     */
    public static double jointEntropy(Map<String, Double> pJoint) {
        double H = 0.0;
        for (double prob : pJoint.values()) {
            if (prob > 0)
                H -= prob * (Math.log(prob) / Math.log(2));
        }
        return H;
    }

    /**
     * Calcula la entropía condicional H(R|L) = H(L,R) - H(L)
     */
    public static double conditionalEntropy(Map<String, Double> pJoint, Map<Integer, Double> pLeft) {
        double H_LR = jointEntropy(pJoint);
        double H_L = entropy(pLeft);
        return H_LR - H_L;
    }
}

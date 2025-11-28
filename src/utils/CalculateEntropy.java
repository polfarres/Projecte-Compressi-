package utils;

import java.util.Map;

public class CalculateEntropy {

    public static double entropy(Map<Short, Double> p) {
        //Calcula la entropía H(X) = -sum p(x) log2(p(x))
        double H = 0.0;
        for (double prob : p.values()) {
            if (prob > 0)
                H -= prob * (Math.log(prob) / Math.log(2));
        }
        return H;
    }


    public static double jointEntropy(Map<String, Double> pJoint) {
        //Calcula la entropía conjunta H(L,R) = -sum p(l,r) log2(p(l,r))
        double H = 0.0;
        for (double prob : pJoint.values()) {
            if (prob > 0)
                H -= prob * (Math.log(prob) / Math.log(2));
        }
        return H;
    }



    public static double conditionalEntropy(Map<String, Double> pJoint, Map<Short, Double> pLeft) {
        //Calcula la entropía condicional H(R|L) = H(R) - I(L,R) = H(R) - (H(L) + H(R) - H(L,R)) = **H(L) - H(L,R)**

        double H_LR = jointEntropy(pJoint);
        double H_L = entropy(pLeft);
        return H_LR - H_L;
    }
}

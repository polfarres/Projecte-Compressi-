package utils;

import image.Image;

import java.util.Map;

public class Entropy {

    public static double imageEntropy(Image image) {
        Map<Integer, Double> p = Probability.pixelProbability(image);
        return entropy(p);
    }

    public static double entropy(Map<Integer, Double> p) {

        //Calcula la entropía H(X) = -sum p(x) log2(p(x))
        double H = 0.0;
        for (double prob : p.values()) {
            if (prob > 0)
                H -= prob * (Math.log(prob) / Math.log(2));
        }
        return H;
    }

    public static double conditionalEntropy(Image image) {

        Map<String, Double> pJoint = Probability.jointProbability(image.img);
        Map<Integer, Double> pLeft = Probability.marginalLeft(pJoint);

        return Entropy.conditionalEntropy(pJoint, pLeft);
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



    public static double conditionalEntropy(Map<String, Double> pJoint, Map<Integer, Double> pLeft) {
        //Calcula la entropía condicional H(R|L) = H(R) - I(L,R) = H(R) - (H(L) + H(R) - H(L,R)) = **H(L) - H(L,R)**

        double H_LR = jointEntropy(pJoint);
        double H_L = entropy(pLeft);
        return H_LR - H_L;
    }
}

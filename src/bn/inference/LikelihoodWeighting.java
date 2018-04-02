package bn.inference;

import bn.core.*;

public class LikelihoodWeighting {
    private class WeightedSample{
        double weight;
        Assignment assignment;
        WeightedSample(){
            weight = 1;
            assignment = new Assignment();
        }
    }

    public Distribution likelihoodWeighting(BayesianNetwork bn, RandomVariable X, Assignment e, int N){
        Distribution Q = new Distribution(X);
        int[] weightedCounts = new int[X.getDomain().size()];


        for (int j = 1; j<=N; j++){
            WeightedSample weightedSample = weightedSample(bn, e);

        }

        Q.normalize();
        return Q;
    }

    private WeightedSample weightedSample(BayesianNetwork bn, Assignment e){
        WeightedSample weightedSample = new WeightedSample();


        for (RandomVariable X:bn.getVariableList()){
            if (e.containsKey(X)){
                weightedSample.assignment.put(X, e.get(X));
                weightedSample.weight *= bn.getProb(X, weightedSample.assignment);
            }else{
//                = getRandomSample()
            }
        }

        return weightedSample;
    }
}

package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;

public class LikelihoodWeighting implements Inferencer{
    private int samples;

    private class WeightedSample{
        Assignment assignment;
        Double weight;
        private WeightedSample(Assignment e){
            this.assignment = e;
            this.weight = 1.0;
        }
    }
    public LikelihoodWeighting(int samples){
        this.samples = samples;
    }


    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        Distribution W = new Distribution(X);
        for (Object ob: X.getDomain()){
            W.put(ob, 0.0);
        }

        for (int j=1; j<=samples; j++){
            WeightedSample weightedSample = Weighted_Sample(bn, e);

            W.put(weightedSample.assignment.get(X), W.get(weightedSample.assignment.get(X)) + weightedSample.weight);
        }

        W.normalize();
        return W;

    }

    private WeightedSample Weighted_Sample(BayesianNetwork bn, Assignment e){
        WeightedSample weightedSample = new WeightedSample(e.copy());
        for (RandomVariable xi: bn.getVariableListTopologicallySorted()){
            if (weightedSample.assignment.containsKey(xi)){
                weightedSample.assignment.set(xi, weightedSample.assignment.get(xi));
                weightedSample.weight *= bn.getProb(xi, weightedSample.assignment);
            }else{
                Distribution distribution = new Distribution(xi);
                for (Object ob: xi.getDomain()){
                    weightedSample.assignment.set(xi, ob);
                    distribution.put(ob, bn.getProb(xi, weightedSample.assignment));
                }


                weightedSample.assignment.set(xi, distribution.randomSample());


            }
        }

        return weightedSample;
    }

}

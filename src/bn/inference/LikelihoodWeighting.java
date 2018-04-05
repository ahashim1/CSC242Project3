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
        //  Contains an assignment and its associated weight
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
        //  Init empty dist over query variable
        Distribution W = new Distribution(X);

        //  Get domain of query and put in dist as 0
        for (Object ob: X.getDomain()){
            W.put(ob, 0.0);
        }

        //  For each sample, give it a weight and apply to distribution
        for (int j=1; j<=samples; j++){
            WeightedSample weightedSample = Weighted_Sample(bn, e);

            W.put(weightedSample.assignment.get(X), W.get(weightedSample.assignment.get(X)) + weightedSample.weight);
        }

        //  Normalize and send it
        W.normalize();
        return W;

    }

    private WeightedSample Weighted_Sample(BayesianNetwork bn, Assignment e){

        //  Create weighted sample from assignment with inital weight 1
        WeightedSample weightedSample = new WeightedSample(e.copy());
        //  Get all random variables in topological order
        for (RandomVariable xi: bn.getVariableListTopologicallySorted()){
            if (weightedSample.assignment.containsKey(xi)){
                //  If evidence variable, multiply weight by its liklihood
                weightedSample.assignment.set(xi, weightedSample.assignment.get(xi));
                weightedSample.weight *= bn.getProb(xi, weightedSample.assignment);
            }else{
                //  Else sample random variable according to samples from
                // parents
                Distribution distribution = new Distribution(xi);
                for (Object ob: xi.getDomain()){
                    weightedSample.assignment.set(xi, ob);
                    distribution.put(ob, bn.getProb(xi, weightedSample.assignment));
                }

                //  Update weighted sample
                weightedSample.assignment.set(xi, distribution.randomSample());


            }
        }

        return weightedSample;
    }

}

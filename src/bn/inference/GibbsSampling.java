package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class GibbsSampling {
    private int samples;
    public GibbsSampling(int samples){
        this.samples = samples;
    }
    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        Distribution distribution = new Distribution(X);
        //  Init empty distribution
        for (Object ob: X.getDomain()){
            //  Add domain of query variable with default values
            distribution.put(ob, 0.0);
        }

        //  Get list of random variables
        List<RandomVariable> Z = getNonEvidenceVariables(bn, e);
        Assignment x = e.copy();

        //  Randomly assign value to each variable
        initializeStateWithRandomValues(x, Z);

        for (int j=1; j<=samples; j++){
            for (RandomVariable Zi: Z){
                //  For each random variable in each sample,  add each sample
                // from Markov blanket
                sampleFromMarkovBlanket(Zi, x, bn);
                distribution.put(x.get(Zi), distribution.get(x.get(Zi)) + 1.0);
            }
        }
        //  Normalize and return
        distribution.normalize();
        return distribution;
    }

    private void sampleFromMarkovBlanket(RandomVariable Zi, Assignment x, BayesianNetwork bn){
        //  init empty distribution and make copy of assignment
        Distribution distribution = new Distribution(Zi);
        Assignment copy = x.copy();

        for (Object ob: Zi.getDomain()){
            //  For each object in random variable, make copy, get children,
            // and get initial probability of domain variable
            copy.set(Zi, ob);

            Set<RandomVariable> children = bn.getChildren(Zi);
            double product= bn.getProb(Zi, copy);

            for (RandomVariable child: children){
                //  For each child, multiply initial prob bny child's
                // probability
                product *= bn.getProb(child, copy);
            }

            //  Place final product in distribution
            distribution.put(ob, product);
        }
        //  Normalize and set assignment
        distribution.normalize();
        x.set(Zi, distribution.randomSample());
    }
    private void initializeStateWithRandomValues(Assignment x, List<RandomVariable> Z){
        //  Randomly assign each variable in the state
        for (RandomVariable Zi: Z){
            Object randomValue = Zi.getDomain().get(new Random().nextInt(Zi.getDomain().size()));
            x.set(Zi, randomValue);

        }
    }

    private List<RandomVariable> getNonEvidenceVariables(BayesianNetwork bn, Assignment e){
        //  Gets only random variables
        //  Start with all variables in network in topological order
        List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
        //  Init new list
        List<RandomVariable> nonEvidenceVars = new ArrayList<>();

        for (RandomVariable X: vars){
            //  If e does not contain the variable, add to list
            if (!e.containsKey(X)){
                nonEvidenceVars.add(X);

            }
        }

        return nonEvidenceVars;

    }

}

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
        for (Object ob: X.getDomain()){
            distribution.put(ob, 0.0);
        }

        List<RandomVariable> Z = getNonEvidenceVariables(bn, e);
        Assignment x = e.copy();

        initializeStateWithRandomValues(x, Z);

        for (int j=1; j<=samples; j++){
            for (RandomVariable Zi: Z){
                sampleFromMarkovBlanket(Zi, x, bn);
                distribution.put(x.get(Zi), distribution.get(x.get(Zi)) + 1.0);
            }
        }
        distribution.normalize();
        return distribution;
    }

    private void sampleFromMarkovBlanket(RandomVariable Zi, Assignment x, BayesianNetwork bn){
        Distribution distribution = new Distribution(Zi);
        Assignment copy = x.copy();

        for (Object ob: Zi.getDomain()){
            copy.set(Zi, ob);

            Set<RandomVariable> children = bn.getChildren(Zi);
            double product= bn.getProb(Zi, copy);

            for (RandomVariable child: children){


                product *= bn.getProb(child, copy);
            }

            distribution.put(ob, product);
        }

        distribution.normalize();
        x.set(Zi, distribution.randomSample());
    }
    private void initializeStateWithRandomValues(Assignment x, List<RandomVariable> Z){
        for (RandomVariable Zi: Z){
            Object randomValue = Zi.getDomain().get(new Random().nextInt(Zi.getDomain().size()));
            x.set(Zi, randomValue);

        }
    }

    private List<RandomVariable> getNonEvidenceVariables(BayesianNetwork bn, Assignment e){
        List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
        List<RandomVariable> nonEvidenceVars = new ArrayList<>();
        for (RandomVariable X: vars){
            if (!e.containsKey(X)){
                nonEvidenceVars.add(X);

            }
        }

        return nonEvidenceVars;

    }

}

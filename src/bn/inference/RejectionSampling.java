package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class RejectionSampling implements Inferencer {
    int samples;
    public RejectionSampling(int samples){
        this.samples = samples;
    }

    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        //  Init distribution and add domain of random variable
        Distribution Q = new Distribution(X);
        for (Object ob: X.getDomain()){
            Q.put(ob, 0);
        }

        //  For each sample...
        for (int j = 1; j <= samples; j++) {
            //  Get prior sample using network
            Assignment event = priorSample(bn);

            if (isConsistent(event, e)) {
                //  If prior sample does not contradict evidence variables,
                // increment assignment count
                double previous = Q.get(event.get(X));

                Q.put(event.get(X), previous + 1);

            }
        }
        //  Normalize
        Q.normalize();
        return Q;
    }



    private Assignment priorSample(BayesianNetwork bn) {
        //  Generates prior samples
        Assignment event = new Assignment();
        for (RandomVariable X: bn.getVariableListTopologicallySorted()){
            //  Get all random variables from network

            //  Init empty distribution
            Distribution distribution = new Distribution(X);
            for (Object ob: X.getDomain()){
                //  For domain of X, update each variable with random sample
                event.set(X, ob);
                distribution.put(ob, bn.getProb(X, event));
                //  Revert for next event
                event.remove(X);
            }

            //  Set random variable in distribution
            event.set(X, distribution.randomSample());
        }

        return event;
    }

    public boolean isConsistent(Assignment assignment, Assignment e){
        //  Returns whether two assignments are consistent
        for (Object ob: e.keySet()){
            if (!e.get(ob).toString().equals(assignment.get(ob).toString())){
                //  If any variables are not equal, return false
                return false;
            }

        }

        return true;
    }
}

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

        Distribution Q = new Distribution(X);
        for (Object ob: X.getDomain()){
            Q.put(ob, 0);
        }
        for (int j = 1; j <= samples; j++) {
            Assignment event = priorSample(bn);

            if (isConsistent(event, e)) {
                double previous = Q.get(event.get(X));

                Q.put(event.get(X), previous + 1);

            }
        }

        Q.normalize();
        return Q;
    }



    private Assignment priorSample(BayesianNetwork bn) {
        Assignment event = new Assignment();
        for (RandomVariable X: bn.getVariableListTopologicallySorted()){

            Distribution distribution = new Distribution(X);
            for (Object ob: X.getDomain()){
                event.set(X, ob);
                distribution.put(ob, bn.getProb(X, event));
                event.remove(X);
            }


            event.set(X, distribution.randomSample());
        }

        return event;
    }

    public boolean isConsistent(Assignment assignment, Assignment e){

        for (Object ob: e.keySet()){
            if (!e.get(ob).toString().equals(assignment.get(ob).toString())){
                return false;
            }

        }

        return true;
    }
}

package bn.inference;

import bn.core.*;
import java.util.List;
public class EnumerationInferencer implements Inferencer {
    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        Distribution Q = new Distribution(X);
        for (int x = 0; x<X.getDomain().size(); x++){
            Assignment newAssignment = new Assignment();
            newAssignment.set(X, X.getDomain().get(x));
            List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
            Q.put(X.getDomain().get(x), enumerateAll(bn, vars, newAssignment));
        }

        Q.normalize();
        return Q;
    }


    public double enumerateAll(BayesianNetwork bn, List<RandomVariable> vars, Assignment e){
        if (vars.isEmpty()){
            return 1.0;
        }

        // Might need to copy vars into a new list before doing this.
        RandomVariable Y = vars.get(0);
        vars.remove(0);

        if (e.containsKey(Y)){
            return bn.getProb(Y, e) * enumerateAll(bn, vars, e)
        }else{
            return sumAssignments(bn, Y, vars, e);
        }
    }

    public double sumAssignments(BayesianNetwork bn, RandomVariable Y, List<RandomVariable> vars, Assignment e){
        double ret = 0;
        for (int i = 0; i<Y.getDomain().size(); i++){
            Assignment y = new Assignment();
            y.set(Y, Y.getDomain().get(i));
            ret += bn.getProb(Y, y) * enumerateAll(bn, vars, y);
        }
        return ret;
    }


}

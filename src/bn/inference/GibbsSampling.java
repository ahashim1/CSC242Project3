package bn.inference;

import bn.core.*;

import java.util.ArrayList;
import java.util.List;

public class GibbsSampling {
    public Distribution gibbsAsk(BayesianNetwork bn, RandomVariable X, Assignment e, int N){
        int[] counts = new int[X.getDomain().size()];
        List<RandomVariable> Z = getNonEvidenceVariables(bn, e);
        Assignment x = e.copy();

        for (int j=1; j<=N; j++){
            for (RandomVariable Zi: Z){

            }
        }
    }

    private List<RandomVariable> getNonEvidenceVariables(BayesianNetwork bn, Assignment e){
        List<RandomVariable> vars = bn.getVariableList();
        List<RandomVariable> nonEvidenceVars = new ArrayList<>();
        for (RandomVariable X: vars){
            if (e.containsKey(X)){
                nonEvidenceVars.add(X);
            }
        }

        return nonEvidenceVars;

    }

}

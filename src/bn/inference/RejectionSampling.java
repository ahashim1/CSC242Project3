package bn.inference;

import bn.core.*;

public class RejectionSampling{


    public Distribution rejectionSampling(BayesianNetwork bn, RandomVariable X, Assignment e, int N){
        Distribution Q = new Distribution(X);
        int[] counts = new int[X.getDomain().size()];
        for (int j = 1; j <= N; j++){
            Assignment x = priorSample(bn);
            if (checkConsistent(x, e)){

            }
        }

        Q.normalize();
        return Q;
    }

    private Assignment priorSample(BayesianNetwork bn){
        return new Assignment();
    }
    private boolean checkConsistent(Assignment x, Assignment e){
        for (RandomVariable a: x.keySet()){
            for (RandomVariable b:e.keySet()){
                if (a.getName().equals(b.getName())){
                    // Inconsistent if the names are the same but they have different values
                    if (!x.get(a).equals(e.get(b))){
                        return false;
                    }
                }
            }
        }

        return true;
    }

}

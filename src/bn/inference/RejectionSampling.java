package bn.inference;

import bn.core.*;

public class RejectionSampling{


    public Distribution rejectionSampling(BayesianNetwork bn, RandomVariable X, Assignment e, int N){
        Distribution Q = new Distribution(X);
        int[] counts = new int[X.getDomain().size()];
        for (int j = 1; j <= N; j++){
            Assignment x = priorSample(bn);
            if (x.isConsistentWith(e)){

            }
        }

        Q.normalize();
        return Q;
    }
}

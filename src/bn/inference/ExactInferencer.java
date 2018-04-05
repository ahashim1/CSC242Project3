package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExactInferencer implements Inferencer {

    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e) {
        //  Init empty distribution over random var X
        Distribution Q = new Distribution(X);
        //  Over domain of X enumerate each value
        for (Object xi: X.getDomain()){
            //  Get evidence, add a single value of X
            Assignment e_xi = e.copy();
            e_xi.set(X, xi);
            //
            List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
            Q.put(xi, enumerateAll(bn, vars, e_xi));
        }
        //  Normalize and send it
        Q.normalize();
        return Q;
    }

    private double enumerateAll(BayesianNetwork bn, List<RandomVariable> vars, Assignment e){
        //  If no random variables, prob is 100%
        if (vars.isEmpty()){
            return 1.0;
        }
        //  Get first random variable from variable list (topological order)
        RandomVariable Y = vars.get(0);
        List<RandomVariable> rest = vars.subList(1, vars.size());

        //  If in evidence, return known Prob of Y given parents
        if (e.containsKey(Y)){
            return bn.getProb(Y, e) * enumerateAll(bn, rest, e);
        }else{
            //  Else return the sum over the full prob dist of y and return that
            double sum = 0.0;
            for (Object y: Y.getDomain()){
                Assignment e_y = e.copy();
                e_y.set(Y, y);
                sum += bn.getProb(Y, e_y) * enumerateAll(bn, rest, e_y);

            }

            return sum;

        }
    }

    public static void main(String[] args) {
        String[] myargs = {"alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
//        String[] myargs = {"aima-wet-grass.xml", "R", "S", "true"};

        args = myargs;
//        MAKE SURE I DELETE PREVIOUS TWO LINES BEFORE SUBMITTING

        if (args.length < 4) {
            System.err.println("Not enough arguments");
            return;
        }
        String filename = args[0];
        String queryVariable = args[1];
        String[] evidenceParameters = Arrays.copyOfRange(args, 2, args.length);
        if (evidenceParameters.length % 2 != 0) {
            System.err.println("You need to assign a boolean to each of your evidence parameters");
            return;
        }

        HashMap<String, String> evidenceVariables = new HashMap<>();
        for (int i = 0; i<evidenceParameters.length; i += 2){
            String evidenceVariable = evidenceParameters[i];
            String assignment = evidenceParameters[i+1];
            evidenceVariables.put(evidenceVariable, assignment);

        }

        String type;
        //  Decide file type

        if (filename.toLowerCase().endsWith(".bif")) {
            type = ".bif";
        } else if (filename.toLowerCase().endsWith(".xml")) {
            type = ".xml";
        } else {
            System.err.println("Invalid command;");
            return;
        }

        try {

            String path = "src/bn/examples/" + filename;

            BayesianNetwork bn;

            if (type.equals(".bif")) {
                BIFParser parser = new BIFParser(new FileInputStream(path));
                bn = parser.parseNetwork();
            } else {
                XMLBIFParser parser = new XMLBIFParser();
                bn = parser.readNetworkFromFile(path);
            }

            RandomVariable query = bn.getVariableByName(queryVariable);

            Assignment e = new Assignment();


            for (String key: evidenceVariables.keySet()){
                RandomVariable rv = bn.getVariableByName(key);
                e.put(rv, evidenceVariables.get(key));
            }

            long start = System.currentTimeMillis();
            ExactInferencer exactInferencer = new ExactInferencer();
            Distribution distribution = exactInferencer.ask(bn, query, e);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Problem: " + filename);
            System.out.println("Result: " + distribution.toString());
            System.out.println("Time elapsed (ms): " + elapsed);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

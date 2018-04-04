package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;

public class LikelihoodWeighting2 implements Inferencer{
    private int samples;

    private class WeightedSample{
        Assignment assignment;
        Double weight;
        private WeightedSample(Assignment e){
            this.assignment = e;
            this.weight = 1.0;
        }
    }
    public LikelihoodWeighting2(int samples){
        this.samples = samples;
    }


    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        Distribution W = new Distribution(X);
        for (Object ob: X.getDomain()){
            W.put(ob, 0.0);
        }

        for (int j=1; j<=samples; j++){
            WeightedSample weightedSample = Weighted_Sample(bn, e);

            W.put(weightedSample.assignment.get(X), W.get(weightedSample.assignment.get(X)) + weightedSample.weight);
        }

        W.normalize();
        return W;

    }

    private WeightedSample Weighted_Sample(BayesianNetwork bn, Assignment e){
        WeightedSample weightedSample = new WeightedSample(e.copy());
        for (RandomVariable xi: bn.getVariableListTopologicallySorted()){
            if (weightedSample.assignment.containsKey(xi)){
                weightedSample.assignment.set(xi, weightedSample.assignment.get(xi));
                weightedSample.weight *= bn.getProb(xi, weightedSample.assignment);
            }else{
                Distribution distribution = new Distribution(xi);
                for (Object ob: xi.getDomain()){
                    weightedSample.assignment.set(xi, ob);
                    distribution.put(ob, bn.getProb(xi, weightedSample.assignment));
                }


                weightedSample.assignment.set(xi, distribution.randomSample());


            }
        }

        return weightedSample;
    }

    public static void main(String[] args) {
        String[] myargs = {"100000", "aima-alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
//        String[] myargs = {"100000", "aima-wet-grass.xml", "R", "C", "true", "W", "true"};

        args = myargs;
//        MAKE SURE I DELETE PREVIOUS TWO LINES BEFORE SUBMITTING

        if (args.length < 4) {
            System.err.println("Not enough arguments");
            return;
        }
        int samples = Integer.parseInt(args[0]);

        String filename = args[1];
        String queryVariable = args[2];
        String[] evidenceParameters = Arrays.copyOfRange(args, 3, args.length);
        if (evidenceParameters.length % 2 != 0) {
            System.err.println("You need to assign a boolean to each of your evidence parameters");
            return;
        }


//        Can evidence variables have a value other than boolean?
        HashMap<String, String> evidenceVariables = new HashMap<>();
        for (int i = 0; i < evidenceParameters.length; i += 2) {
            String evidenceVariable = evidenceParameters[i];
            String assignment = evidenceParameters[i + 1];
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


            int i = 0;
            for (String key : evidenceVariables.keySet()) {
                RandomVariable rv = bn.getVariableByName(key);
                e.put(rv, evidenceVariables.get(key));
            }

            LikelihoodWeighting2 likelihoodWeighting2 = new LikelihoodWeighting2(samples);
            Distribution distribution = likelihoodWeighting2.ask(bn, query, e);
            System.out.println(distribution.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;

public class LikelihoodWeighting implements Inferencer  {
    private class WeightedSample{
        double weight;
        Assignment assignment;
        WeightedSample(){
            weight = 1;
            assignment = new Assignment();
        }
    }


    private int samples;
    public LikelihoodWeighting(int samples){
        this.samples = samples;
    }


    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        Distribution Q = new Distribution(X);

        for (Object ob: X.getDomain()){
            Q.put(ob, 0);
        }

        for (int j = 1; j<=samples; j++){
            WeightedSample weightedSample = weightedSample(bn, e);
            Q.put(weightedSample.assignment.get(X), Q.get(weightedSample.assignment.get(X)) + weightedSample.weight);
        }

        Q.normalize();
        return Q;
    }

    private WeightedSample weightedSample(BayesianNetwork bn, Assignment e){
        WeightedSample weightedSample = new WeightedSample();

        for (RandomVariable X:bn.getVariableListTopologicallySorted()){
            if (e.containsKey(X)){
                weightedSample.assignment.set(X, e.get(X));
                weightedSample.weight *= bn.getProb(X, weightedSample.assignment);
            }else{
                System.out.println("WHY");
                Distribution distribution = new Distribution(X);
                for (Object ob: X.getDomain()){
                    Assignment copy = e.copy();
                    copy.put(X, bn.getProb(X, copy));
                    distribution.put(ob, bn.getProb(X, copy));
                }

                distribution.normalize();
                weightedSample.assignment.set(X, distribution.randomSample());

            }
        }

        return weightedSample;
    }


    public static void main(String[] args) {
//        String[] myargs = {"1000000", "aima-alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
        String[] myargs = {"100000", "aima-wet-grass.xml", "R", "C", "true"};

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


        HashMap<String, Boolean> evidenceVariables = new HashMap<>();
        for (int i = 0; i < evidenceParameters.length; i += 2) {
            String evidenceVariable = evidenceParameters[i];
            String assignment = evidenceParameters[i + 1];
            if (assignment.equalsIgnoreCase("true") || assignment.equalsIgnoreCase("false")) {
                Boolean evidenceVariableAssignment = Boolean.parseBoolean(assignment);
                evidenceVariables.put(evidenceVariable, evidenceVariableAssignment);
            } else {
                System.err.println("The assignment for an evidence variable is not a boolean");
                return;
            }
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

            LikelihoodWeighting likelihoodWeighting = new LikelihoodWeighting(samples);
            Distribution distribution = likelihoodWeighting.ask(bn, query, e);
            System.out.println(distribution.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

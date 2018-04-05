package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;

public class ApproximateInferencer {
    public static void main(String[] args) {
        String[] myargs = {"GibbsSampling", "1000000", "aima-alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
//        String[] myargs = {"100000", "aima-wet-grass.xml", "R", "S", "true"};

        args = myargs;
//        MAKE SURE I DELETE PREVIOUS TWO LINES BEFORE SUBMITTING

        if (args.length < 5) {
            System.err.println("Not enough arguments");
            return;
        }

        String approxInferencer = args[0];
        int samples = Integer.parseInt(args[1]);

        String filename = args[2];
        String queryVariable = args[3];
        String[] evidenceParameters = Arrays.copyOfRange(args, 4, args.length);
        if (evidenceParameters.length % 2 != 0) {
            System.err.println("You need to assign a value to each of your evidence parameters");
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

            for (String key : evidenceVariables.keySet()) {
                RandomVariable rv = bn.getVariableByName(key);
                e.put(rv, evidenceVariables.get(key));
            }

            if (approxInferencer.equals("RejectionSampling")){
                long start = System.currentTimeMillis();
                RejectionSampling rejectionSampling = new RejectionSampling(samples);
                Distribution distribution = rejectionSampling.ask(bn, query, e);
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("Problem: " + filename);
                System.out.println("Number of samples: " + samples);
                System.out.println("Result: " + distribution.toString());
                System.out.println("Time elapsed (ms): " + elapsed);
            }else if (approxInferencer.equals("LikelihoodWeighting")){
                long start = System.currentTimeMillis();
                LikelihoodWeighting likelihoodWeighting = new LikelihoodWeighting(samples);
                Distribution distribution = likelihoodWeighting.ask(bn, query, e);
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("Problem: " + filename);
                System.out.println("Number of samples: " + samples);
                System.out.println("Result: " + distribution.toString());
                System.out.println("Time elapsed (ms): " + elapsed);
            }else if (approxInferencer.equals("GibbsSampling")){
                long start = System.currentTimeMillis();
                GibbsSampling gibbsSampling = new GibbsSampling(samples);
                Distribution distribution = gibbsSampling.ask(bn, query, e);
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("Problem: " + filename);
                System.out.println("Number of samples: " + samples);
                System.out.println("Result: " + distribution.toString());
                System.out.println("Time elapsed (ms): " + elapsed);
            }else{
                System.err.println("Not an approximate inferencer");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

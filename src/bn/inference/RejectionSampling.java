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

        System.out.println(Q.toString());
        Q.normalize();
        return Q;
    }

    private Object randomSample(Distribution distribution){
        System.out.println(distribution.toString());
        List<Object> keys = new ArrayList<Object>(distribution.keySet());
        double randomValue = Math.random();
        double sum = 0.0;
        for (int index = 0; index < keys.size(); index++) {
            sum += distribution.get(keys.get(index));
            if (randomValue <= sum)
                return keys.get(index);
        }
        return keys.get(keys.size() - 1);
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


            distribution.normalize();
            event.set(X, randomSample(distribution));
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

    public static void main(String[] args) {
//        String[] myargs = {"1000000", "aima-alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
        String[] myargs = {"100000", "aima-wet-grass.xml", "R", "S", "true"};

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

            RejectionSampling rejectionSampling = new RejectionSampling(samples);
            Distribution distribution = rejectionSampling.ask(bn, query, e);
            System.out.println(distribution.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

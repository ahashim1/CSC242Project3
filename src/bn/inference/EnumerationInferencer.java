package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
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


    private double enumerateAll(BayesianNetwork bn, List<RandomVariable> vars, Assignment e){
        if (vars.isEmpty()){
            return 1.0;
        }

        // Might need to copy vars into a new list before doing this.
        RandomVariable Y = vars.get(0);
        List<RandomVariable> rest = vars.subList(1, vars.size());

        if (e.containsKey(Y)){
            return bn.getProb(Y, e) * enumerateAll(bn, rest, e);
        }else{
            return sumAssignments(bn, Y, rest, e);
        }
    }

    private double sumAssignments(BayesianNetwork bn, RandomVariable Y, List<RandomVariable> vars, Assignment e){

        double sum = 0;
        for (int i = 0; i<Y.getDomain().size(); i++){
            Assignment y = e.copy();
            y.set(Y, Y.getDomain().get(i));

            sum += bn.getProb(Y, y) * enumerateAll(bn, vars, y);
        }
        return sum;
    }

    public static void main(String[] args) {
//        String[] myargs = {"aima-alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
        String[] myargs = {"aima-wet-grass.xml", "R", "S", "true"};

        args = myargs;
//        MAKE SURE I DELETE PREVIOUS TWO LINES BEFORE SUBMITTING

        if (args.length < 4) {
            System.err.println("Not enough arguments");
            return;
        }
        String filename = args[0];
        String queryVariable = args[1];
        String[] evidenceParameters = Arrays.copyOfRange(args, 2, args.length);
        if (evidenceParameters.length % 2 != 0){
            System.err.println("You need to assign a boolean to each of your evidence parameters");
            return;
        }


        HashMap<String, Boolean> evidenceVariables = new HashMap<>();
        for (int i = 0; i<evidenceParameters.length; i += 2){
            String evidenceVariable = evidenceParameters[i];
            String assignment = evidenceParameters[i+1];
            if (assignment.equalsIgnoreCase("true") || assignment.equalsIgnoreCase("false")){
                Boolean evidenceVariableAssignment = Boolean.parseBoolean(assignment);
                evidenceVariables.put(evidenceVariable, evidenceVariableAssignment);
            }else{
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
            for (String key: evidenceVariables.keySet()){
                RandomVariable rv = bn.getVariableByName(key);
                e.put(rv, evidenceVariables.get(key));
            }


            EnumerationInferencer enumerationInferencer = new EnumerationInferencer();
            Distribution distribution = enumerationInferencer.ask(bn, query, e);
            System.out.println(distribution.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}

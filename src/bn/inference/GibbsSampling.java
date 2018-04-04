package bn.inference;

import bn.core.*;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class GibbsSampling {
    private int samples;
    public GibbsSampling(int samples){
        this.samples = samples;
    }
    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){
        Distribution distribution = new Distribution(X);
        for (Object ob: X.getDomain()){
            distribution.put(ob, 0.0);
        }

        List<RandomVariable> Z = getNonEvidenceVariables(bn, e);
        Assignment x = e.copy();

        initializeStateWithRandomValues(x, Z);

        for (int j=1; j<=samples; j++){
            for (RandomVariable Zi: Z){
                sampleFromMarkovBlanket(Zi, x, bn);
                distribution.put(x.get(Zi), distribution.get(x.get(Zi)) + 1.0);
            }
        }
        distribution.normalize();
        return distribution;
    }

    private void sampleFromMarkovBlanket(RandomVariable Zi, Assignment x, BayesianNetwork bn){
        Distribution distribution = new Distribution(Zi);
        Assignment copy = x.copy();

        for (Object ob: Zi.getDomain()){
            copy.set(Zi, ob);

            Set<RandomVariable> children = bn.getChildren(Zi);
            double product= bn.getProb(Zi, copy);

            for (RandomVariable child: children){


                product *= bn.getProb(child, copy);
            }

            distribution.put(ob, product);
        }

        distribution.normalize();
        x.set(Zi, distribution.randomSample());
    }
    private void initializeStateWithRandomValues(Assignment x, List<RandomVariable> Z){
        for (RandomVariable Zi: Z){
            Object randomValue = Zi.getDomain().get(new Random().nextInt(Zi.getDomain().size()));
            x.set(Zi, randomValue);

        }
    }

    private List<RandomVariable> getNonEvidenceVariables(BayesianNetwork bn, Assignment e){
        List<RandomVariable> vars = bn.getVariableListTopologicallySorted();
        List<RandomVariable> nonEvidenceVars = new ArrayList<>();
        for (RandomVariable X: vars){
            if (!e.containsKey(X)){
                nonEvidenceVars.add(X);

            }
        }

        return nonEvidenceVars;

    }

    public static void main(String[] args) {
        String[] myargs = {"10000", "aima-alarm.xml", "B", "J", "true", "M", "true"};

        // wet grass example
//        String[] myargs = {"100000", "aima-wet-grass.xml", "R", "S", "true"};

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
            System.err.println("You need to assign a value to each of your evidence parameters");
            return;
        }


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

            GibbsSampling gibbsSampling = new GibbsSampling(samples);
            Distribution distribution = gibbsSampling.ask(bn, query, e);
            System.out.println(distribution.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

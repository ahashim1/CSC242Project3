package bn.core;

import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class Driver {
    //  Handles reading the command line arguments and runs everything

    public static void main(String[] args) {
        System.out.println("Enter your commands below:\n");

        String language = args[0];       // Desired language
        String inferencer = args[1];     // Desired inferencer

        String[] params;
        String fileName;

        if (!language.equals("java")) {
            System.out.println("This project will only work with java.");
        }

        int samples;

        //  Read and store the rest of the command
        switch (inferencer) {
            case "EnumerationInferencer":
                fileName = args[2];
                params = Arrays.copyOfRange(args, 3, args.length);
                break;

            case "RejectionSampling":
                samples = Integer.parseInt(args[2]);

                fileName = args[3];
                params = Arrays.copyOfRange(args, 4, args.length);
                break;

            case "LikelihoodWeighting":
                samples = Integer.parseInt(args[2]);

                fileName = args[3];
                params = Arrays.copyOfRange(args, 4, args.length);
                break;

            case "GibbsSampling":
                samples = Integer.parseInt(args[2]);

                fileName = args[3];
                params = Arrays.copyOfRange(args, 4, args.length);
                break;

            default:
                System.out.println("Invalid command.");
                return;
        }

        String type;
        //  Decide file type

        if (fileName.toLowerCase().endsWith(".bif")) {
            type = ".bif";
        } else if (fileName.toLowerCase().endsWith(".xml")) {
            type = ".xml";
        } else {
            System.out.println("Invalid command;");
            return;
        }

        //  Now deal with the parameters
        String queryName = params[0];
        String[] evidenceNames = new String[params.length/2];
        Boolean[] evidenceValues = new Boolean[params.length/2];

        for (int i = 1; i < params.length; i++) {
            if (i % 2 == 1) {
                evidenceNames[i / 2] = params[i];
            } else {
                evidenceValues[(i - 1) / 2] = Boolean.parseBoolean(params[i]);
            }
        }

        try {
            String path = "src/nb/examples/" + fileName;

            BayesianNetwork bn;

            if (type.equals(".bif")) {
                BIFParser parser = new BIFParser(new FileInputStream(path));
                bn = parser.parseNetwork();
            } else {
                XMLBIFParser parser = new XMLBIFParser();
                bn = parser.readNetworkFromFile(path);
            }

            RandomVariable query = bn.getVariableByName(queryName);

            Assignment A = new Assignment();
            RandomVariable[] evidenceList = new RandomVariable[evidenceValues
                    .length];
            for (int i = 0; i < evidenceList.length; i++) {
                evidenceList[i] = bn.getVariableByName(evidenceNames[i]);
            }

            if (inferencer.equals("EnumerationInferencer")) {
                System.out.println("This worked.");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}

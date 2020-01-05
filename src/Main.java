import classification.ID3;
import classification.KNN;
import data.DataManager;
import data.Mushroom;
import data.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main class to run program from.
 */

public class Main {
	public static void main(String[] args) {
        // first step - load data and convert to mushroom objects
        List<Mushroom> mushrooms = DataManager.LoadData();
        System.out.println("data.DataManager loaded " + mushrooms.size() + " mushrooms");

        // create test set of first 500 to compare
        List<Mushroom> first500 = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            first500.add(mushrooms.get(i));
        }

        // creating training set of every 5th data point, excluding first 500 for testing
        List<Mushroom> trainingSet = new ArrayList<>();
        for (int i = 500; i < mushrooms.size(); i += 5) {
            trainingSet.add(mushrooms.get(i));
        }
        System.out.println("Training set created with " + trainingSet.size() + " elements");

        System.out.println("\n*** Decision Tree Classification ***");
        // to test the classification code
        ID3 ID3 = new ID3(trainingSet, Mushroom.getAttributeList());
        Node decTree = ID3.getDecisionTree();

        System.out.println("Testing on: First 500 mushrooms of total data set");
        System.out.println(EvaluateDecTree.classify(decTree, first500));

        System.out.println("\nTesting on: Complete mushroom set");
        System.out.println(EvaluateDecTree.classify(decTree, mushrooms));


        System.out.println("\n*** k-Nearest Neighbor Classification ***");
        KNN kNN = new KNN(trainingSet, Mushroom.getAttributeList(), 3);

        // classifies a training set of mushrooms
        Map<Mushroom, Object> classifiedMush = kNN.classify();

        // checks the validity of the classifications on the training set
        Map<String, Integer> results = kNN.checkClassification(classifiedMush);

        // predicts the class labels of an unknown set based on the classifications of the training set
        Map<Mushroom, Object> predictedClasses = kNN.predictedClass(classifiedMush, first500);

        // checks the validity of the predictions
        Map<String, Integer> resultOfPrediction = kNN.checkClassification(predictedClasses);

        System.out.println("Classification by K-nearest neighbour:");
        for (Map.Entry<String, Integer> result : results.entrySet()) {
            String value = result.getKey();
            int number = result.getValue();
            System.out.println(value + " : " + number);
        }

        System.out.println("\nPrediction by K-nearest neighbour:");
        for (Map.Entry<String, Integer> result : resultOfPrediction.entrySet()) {
            String value = result.getKey();
            int number = result.getValue();
            System.out.println(value + " : " + number);
        }
    }

    private static String printChildren(Node node) {
	    if(node.getChildren().isEmpty()) return "";

	    StringBuilder toReturn = new StringBuilder();
	    for(Node child : node.getChildren()) {
	        toReturn.append("\n").append(child.toString()).append("\n");
	        toReturn.append(printChildren(child));
        }

        return toReturn.toString();
    }
}
package classification;

import data.Mushroom;
import enums.Class_Label;

import java.util.*;

public class KNN {
    private List<Mushroom> mushrooms;
    private List<Object> listOfAttributes;
    private int k;

    public KNN(List<Mushroom> mushrooms, List<Object> listOfAttributes, int k) {
        this.mushrooms = mushrooms;
        this.listOfAttributes = listOfAttributes;
        this.k = k;
    }

    /**
     *  Runs through each mushroom, finds its K nearest neighbours and checks the class label of them and sets its own as the majority
     * @return a mapping from mushroom to classified class label
     */
    public Map<Mushroom, Object> classify() {
        Map<Mushroom, Object> classifiedMush = new HashMap<>();

        for(Mushroom mushroom : mushrooms) {
            List<Mushroom> kNN = findKNN(mushroom);
            int edible = 0;
            for(Mushroom neighbour : kNN){
                if(neighbour.getAttributeValue(Class_Label.class).equals(Class_Label.edible)) {
                    edible++;
                }
            }

            if(edible > (kNN.size()/2)) {
                classifiedMush.put(mushroom, Class_Label.edible);
            } else {
                classifiedMush.put(mushroom, Class_Label.poisonous);
            }
        }

        return classifiedMush;
    }

    /**
     * Iterates through each mushroom to predict the class label of and looks at all the nearest neighbours in the training set.
     * Then it sets the predicted class label to be the class label of the majority of the neighbours
     * @param trainedMushrooms - the classified mushrooms used as a basis for prediction
     * @param mushroomsToCompare - a list of mushrooms to predict the class label of
     * @return - a mapping from mushroom to predicted class label
     */
    public Map<Mushroom, Object> predictedClass(Map<Mushroom, Object> trainedMushrooms, List<Mushroom> mushroomsToCompare){
        Map<Mushroom, Object> results = new HashMap<>();
        for(Mushroom mushroomPred : mushroomsToCompare){
            int ed = 0;
            int pois = 0;
            for(Map.Entry<Mushroom, Object> entry : trainedMushrooms.entrySet()){
                Mushroom mush = entry.getKey();
                Object classLabel = entry.getValue();
                double dist = euclideanDistance(mushroomPred, mush);
                if(dist <= 2 ) {
                    if(classLabel.equals(Class_Label.edible)) ed++;
                    else pois++;
                }
            }

            if(ed > pois) {
                results.put(mushroomPred, Class_Label.edible);
            } else results.put(mushroomPred, Class_Label.poisonous);
        }
        return results;
    }

    /**
     * Checks the class label of the classification with the actual class label and increments the appropriate counter based on the result
     * @param classifiedMush - a mapping of mushrooms to the classified class label
     * @return - a mapping of number of true positives, true negatives, false positives and false negatives
     */
    public Map<String, Integer> checkClassification(Map<Mushroom, Object> classifiedMush){
        Map<String, Integer> results = new HashMap<>();
        int truePos = 0;
        int trueNeg = 0;
        int falsePos = 0;
        int falseNeg = 0;

        for(Map.Entry<Mushroom, Object> entry : classifiedMush.entrySet()){
            Object trueClass = entry.getKey().getAttributeValue(Class_Label.class);
            Object classClass = entry.getValue();
            if(trueClass.equals(classClass) && trueClass.equals(Class_Label.edible)) truePos++;
            else if(trueClass.equals(classClass) && trueClass.equals(Class_Label.poisonous)) trueNeg++;
            else if(!trueClass.equals(classClass) && trueClass.equals(Class_Label.poisonous)) falsePos++;
            else if(!trueClass.equals(classClass) && trueClass.equals(Class_Label.edible)) falseNeg++;
        }

        results.put("True Positives", truePos);
        results.put("True Negatives", trueNeg);
        results.put("False Positives", falsePos);
        results.put("False Negatives", falseNeg);
        return results;
    }

    /**
     * Compares all mushrooms other mushrooms in the trianing set with the passed mushroom and finds its K nearest neighbours
     * @param mushroom - the mushroom to find nearest neighbours for
     * @return - a list of the K nearest neighbours
     */
    private List<Mushroom> findKNN(Mushroom mushroom){
        Map<Double, Mushroom> distances = new TreeMap<>();
        List<Mushroom> kNN = new ArrayList<>();

        for(Mushroom mush : mushrooms){
            if(!mushroom.equals(mush)) {
                double dist = euclideanDistance(mushroom, mush);
                distances.put(dist, mush);
            }
        }

        int count = 0;
        for(Map.Entry<Double, Mushroom> entry : distances.entrySet()){
            if(count < k) kNN.add(entry.getValue());
            count++;
        }

        return kNN;
    }

    /**
     *
     * @param mushroomInFocus - mushroom we are looking from
     * @param mushroomToCompare - mushroom we are looking at
     * @return - the euclidean distance between the two mushrooms
     */
    private double euclideanDistance(Mushroom mushroomInFocus, Mushroom mushroomToCompare){
        int count = 0;
        for(Object attribute : listOfAttributes) {
            Object value1 = mushroomInFocus.getAttributeValue(attribute);
            Object value2 = mushroomToCompare.getAttributeValue(attribute);
            if(!value1.equals(value2)) count++;
        }

        double dist = Math.sqrt(count);
        return dist;
    }
}

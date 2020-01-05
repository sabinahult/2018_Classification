package classification;

import data.DataManager;
import data.Mushroom;
import data.Node;
import enums.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
//The Gain Ratio addition
public class C45 {
    private List<Object> global_attributes_list;
    private Node root;
    private double gainRatioAverage;


    public C45(List<Mushroom> dataset, List<Object> attributes_list) {
        this.global_attributes_list = attributes_list;
        root = generateDecisionTree(dataset, global_attributes_list);
        root.setParent(root);
        gainRatioAverage = calculateInfoAverage(informationGain(dataset, attributes_list));
    }

    public Node getDecisionTree() {
        return root;
    }

    // should return root node
    private Node generateDecisionTree(List<Mushroom> data, List<Object> attributes_list) {
        //Creating a data.Node
        Node node = new Node(data, attributes_list);

        // if all tuples have same class label, return node with that class label
        if(calculateEntropyForSet(data) == 0.0) {
            node.setLabel(data.get(0).getAttributeValue(Class_Label.class));
            return node;
        }

        // if there's no attributes, return node with majority class label
        if(global_attributes_list.isEmpty()) {
            node.setLabel(getMajorityClassLabel(data));
            return node;
        }

        // get best attribute to split by
        Object splitting_criterion = attributeSelection(data, attributes_list);

        // ***GainRatio thing: if no attributes information gain is bigger than the average,
        // then return the node with the majority class label
        if(splitting_criterion == null){
            node.setLabel(getMajorityClassLabel(data));
            return node;
        }
        //ID3 algorithm continues
        node.setSplittingCriterion(splitting_criterion);
        global_attributes_list.remove(splitting_criterion);

        // partition data into sets per value and add mushrooms with that value...
        Map<Object, List<Mushroom>> partitions = new HashMap<>();
        for(Object value : ((Class) splitting_criterion).getEnumConstants()) {
            // looking at one value for the attribute selected as splitting criterion
            partitions.put(value, new ArrayList<>());
            for(Mushroom mush : data) {
                if(mush.getAttributeValue(splitting_criterion) == value) {
                    partitions.get(value).add(mush);
                }
            }
        }

        // create children
        for(Object value : partitions.keySet()) {
            if(partitions.get(value).isEmpty()) {
                // creating leaf node with empty att list, because it's not needed further
                Node leaf = new Node(partitions.get(value), new ArrayList<>());
                // setting class label of leaf to majority
                leaf.setLabel(getMajorityClassLabel(data));
                leaf.setValue(value);
                leaf.setParent(node);
                node.addChild(leaf);
            } else {
                // recursive magic... we hope!!
                Node child = generateDecisionTree(partitions.get(value), global_attributes_list);
                child.setValue(value);
                child.setParent(node);
                node.addChild(child);
            }
        }
        return node;
    }


    public Object getMajorityClassLabel(List<Mushroom> data) {
        int edible = 0;
        int poisonous = 0;
        for(Mushroom mush : data) {
            if(mush.getAttributeValue(Class_Label.class).equals(Class_Label.edible)) edible++;
            else poisonous++;
        }

        if(edible > poisonous) return Class_Label.edible;
        else return Class_Label.poisonous;
    }



    public Object attributeSelection(List<Mushroom> dataset, List<Object> attributes) {

        // getting the information gain values for each attribute
        HashMap<Object, Double> gainPerAttribute = informationGain(dataset, attributes);

        // saving the split info calculation for each attribute.
        HashMap<Object, Double> splitInfo = new HashMap<>();


        for(Object att : attributes) {

            // to calculate split Ratio, we need to know how many tuples there are in total for an attribute
            double tupleTotal = 0.0;

            // to keep track of how many tuples there are for each value of the attribute (ratio calculation)
            HashMap<Object, Double> tuplesForEachValue = new HashMap<>();

            // looking at one attribute
            for(Object value : ((Class) att).getEnumConstants()) {

                // looking at one attribute value and at their class label (how many are p and how many are e)
                double poisonous = DataManager.CountClassInstancesBasedOnAttributeValue(dataset,
                        att, value, Class_Label.poisonous);
                double edible = DataManager.CountClassInstancesBasedOnAttributeValue(dataset,
                        att, value, Class_Label.edible);

                // how many mushrooms with this specific attribute value in total ( p. 338 Info-attribute(D) )
                double totalAmountWithAttValue = poisonous + edible;

                // store the total amount of tuples for 1 value in a map
                tuplesForEachValue.put(value, totalAmountWithAttValue);

                // increment for the sake of the GainRatio calculation, to get how many tuples there are for the attribute
                tupleTotal =  tupleTotal +totalAmountWithAttValue;
            }

            // if there are no tuples for that attribute left, then give it a bad score
            if(tupleTotal == 0) {
                splitInfo.put(att, -1.0);
            }
            else {
                // lets do the split calculation p. 341 in the book
                double totalSplitInfo = 1;
                for (Object value : tuplesForEachValue.keySet()) {
                    double split = splitRatio(tuplesForEachValue.get(value), tupleTotal);
                    totalSplitInfo = totalSplitInfo * split;
                }
                splitInfo.put(att, totalSplitInfo);
            }
        }
        // return the attribute with the highest information gain
        return gainRatioCalculation(gainPerAttribute, splitInfo);
    }

    // returns the attribute we need to split by
    private Object gainRatioCalculation(HashMap<Object, Double> gainPerAttribute, HashMap<Object,Double> splitInfo){

        // keep track of max gain
        double maxGainRatio = 0.0;
        // get the average gain constraint
        double averageGain = gainRatioAverage ;

        // gain ratio map
        HashMap<Double, Object> gainRatio = new HashMap<>();

        // calculate the gainRatio for the attributes with a score that is higher than or equal to the average information gain
        for(Object att: gainPerAttribute.keySet()){
            if(gainPerAttribute.get(att) >= averageGain){
                double ratio = gainPerAttribute.get(att)/splitInfo.get(att);
                gainRatio.put(ratio, att);
                if(maxGainRatio < ratio) maxGainRatio = ratio;
            }
        }
        //if there is no gain, return null
        if(maxGainRatio == 0.0) return null;

        return gainRatio.get(maxGainRatio);
    }

    // calculating the informationGain similar to the ID3 attributeSelection method, but
    // returns a map instead of an attribute
    private HashMap<Object, Double> informationGain(List<Mushroom> dataset, List<Object> attributes) {

        double entropy = calculateEntropyForSet(dataset);

        //calculating the information gain for each attribute
        HashMap<Object, Double> gainPerAttribute = new HashMap<>();

        for (Object att : attributes) {

            // looking at one attribute
            double infoGain;
            double infoForAttribute = 0;
            for (Object value : ((Class) att).getEnumConstants()) {

                // looking at one attribute value and at their class label (how many are p and how many are e)
                double poisonous = DataManager.CountClassInstancesBasedOnAttributeValue(dataset,
                        att, value, Class_Label.poisonous);
                double edible = DataManager.CountClassInstancesBasedOnAttributeValue(dataset,
                        att, value, Class_Label.edible);

                // how many mushrooms with this specific attribute value in total ( p. 338 Info-attribute(D) )
                double totalAmountWithAttValue = poisonous + edible;

                double infoForPoisonous = 0.0;
                if (poisonous > 0) { // if the number is 0 then there's no information to be gained from this
                    infoForPoisonous = (-(poisonous / totalAmountWithAttValue
                            * logBase2(poisonous / totalAmountWithAttValue)));
                }

                double infoForEdible = 0.0;
                if (edible > 0) { // if the number is 0 then there's no information to be gained from this
                    infoForEdible = (-(edible / totalAmountWithAttValue
                            * logBase2(edible / totalAmountWithAttValue)));
                }

                double infoPerValue = (totalAmountWithAttValue / dataset.size()) * (infoForPoisonous + infoForEdible);

                infoForAttribute += infoPerValue;
            }

            // p. 339 Gain(attribute) - how much will splitting by this attribute lower the amount of entropy
            infoGain = entropy - infoForAttribute;
            gainPerAttribute.put(att, infoGain);
        }
        return gainPerAttribute;
    }


    // split info calculation
    private double splitRatio(double tupleAmount, double tupleTotal){
        //to make sure we do not take the log of zero
        if(tupleAmount == 0.0){
            return 1;
        }
        return (-(tupleAmount/tupleTotal)*logBase2(tupleAmount/tupleTotal));
    }


    // for use in calculating information gain we need the entropy for the entire set on that class label
    // p. 338 Info(D)
    private double calculateEntropyForSet(List<Mushroom> dataset) {
        int size = dataset.size();
        double poisonous = 0;
        double edible = 0;

        for(Mushroom tuple : dataset) {
            if(tuple.getAttributeValue(Class_Label.class).equals(Class_Label.edible)) {
                edible++;
            }

            if(tuple.getAttributeValue(Class_Label.class).equals(Class_Label.poisonous)) {
                poisonous++;
            }
        }

        // if one is 0, then don't do calculation
        if(poisonous == 0.0 || edible == 0.0) return 0.0;

        return (-(poisonous / size) * logBase2(poisonous / size)) +
                (-(edible / size) * logBase2(edible / size));
    }

    // for use in entropy calculation
    private double logBase2(double amount) {
        return Math.log(amount) / Math.log(2);
    }

    // average informationGain for all attributes
    private double calculateInfoAverage(HashMap<Object, Double> gainPerAttribute){
        double totalgain = 0.0;
        int attributeNumber = 0;

        for(Object att: gainPerAttribute.keySet()){
            double n = gainPerAttribute.get(att);
            totalgain = totalgain + n;
            attributeNumber++;
        }
        // if one of these are zero, then there is no point in calculating the average
        if(totalgain == 0.0 || attributeNumber == 0) {
            return 0.0;
        }
        return totalgain/attributeNumber;
    }

    public Object classify(Node node, Mushroom mushroom){
        Object classLabel = findClass(mushroom, node);
        return classLabel;
    }

    private Class_Label findClass(Mushroom mushroom, Node node) {
        Object splitAttribute = node.getSplitting_criterion();
        if(splitAttribute != null) {
            Object value = mushroom.getAttributeValue(splitAttribute);
            for (Node child : node.getChildren()) {
                if (value.equals(child.getValue()) && !node.isLeaf()) {
                    return findClass(mushroom, child);
                }
            }
        }
        return node.getM_Class();
    }
}

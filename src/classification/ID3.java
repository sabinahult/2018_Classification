package classification;

import data.DataManager;
import data.Mushroom;
import data.Node;
import enums.*;
import java.util.*;

@SuppressWarnings("Duplicates")
public class ID3 {
    private List<Object> global_attributes_list;
    private Node root;


    public ID3(List<Mushroom> dataset, List<Object> attributes_list) {
        this.global_attributes_list = attributes_list;
        root = generateDecisionTree(dataset, global_attributes_list);
        root.setParent(root);
    }

    public Node getDecisionTree() {
        return root;
    }

    // return root node
    private Node generateDecisionTree(List<Mushroom> data, List<Object> attributes_list) {
        Node node = new Node(data, attributes_list);

        // if all tuples have same class label, return node with that class label
        if(calculateEntropyForSet(data) == 0.0) {
            node.setLabel(data.get(0).getAttributeValue(Class_Label.class));
            node.setLeaf();
            return node;
        }

        // if there's no attributes, return node with majority class label
        if(global_attributes_list.isEmpty()) {
            node.setLabel(getMajorityClassLabel(data));
            node.setLeaf();
            return node;
        }

        // get best attribute to split by
        Object splitting_criterion = attributeSelection(data, attributes_list);
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
                leaf.setLeaf();
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


    public static Object attributeSelection(List<Mushroom> dataset, List<Object> attributes) {
        double entropy = calculateEntropyForSet(dataset);

        HashMap<Double, Object> gainPerAttribute = new HashMap<>();
        double maxGain = 0.0;

        for(Object att : attributes) {
            // looking at one attribute
            double infoGain;
            double infoForAttribute = 0;
            for(Object value : ((Class) att).getEnumConstants()) {

                // looking at one attribute value and at their class label (how many are p and how many are e)
                double poisonous = DataManager.CountClassInstancesBasedOnAttributeValue(dataset,
                        att, value, Class_Label.poisonous);
                double edible = DataManager.CountClassInstancesBasedOnAttributeValue(dataset,
                        att, value, Class_Label.edible);

                // how many mushrooms with this specific attribute value in total ( p. 338 Info-attribute(D) )
                double totalAmountWithAttValue = poisonous + edible;

                double infoForPoisonous = 0.0;
                if(poisonous > 0) { // if the number is 0 then there's no information to be gained from this
                    infoForPoisonous = (-(poisonous/totalAmountWithAttValue
                            * logBase2(poisonous/totalAmountWithAttValue)));
                }

                double infoForEdible = 0.0;
                if(edible > 0) { // if the number is 0 then there's no information to be gained from this
                    infoForEdible = (-(edible/totalAmountWithAttValue
                            * logBase2(edible/totalAmountWithAttValue)));
                }

                double infoPerValue = (totalAmountWithAttValue / dataset.size()) * (infoForPoisonous + infoForEdible);

                infoForAttribute += infoPerValue;
            }

            // p. 339 Gain(attribute) - how much will splitting by this attribute lower the amount of entropy
            infoGain = entropy - infoForAttribute;
            gainPerAttribute.put(infoGain, att);

            // set the max gain for easy retrieval in the map
            if(infoGain > maxGain) maxGain = infoGain;
        }

        // return the attribute with the highest information gain
        return gainPerAttribute.get(maxGain);
    }

    // for use in calculating information gain we need the entropy for the entire set on that class label
    // p. 338 Info(D)
    private static double calculateEntropyForSet(List<Mushroom> dataset) {
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
    private static double logBase2(double amount) {
        return Math.log(amount) / Math.log(2);
    }

    /**
     * Classifies an unknown mushroom based on the trained decision tree.
     * @param node root node of decision tree
     * @param mushroom mushroom to classify as either edible or poisonous
     * @return the predicted class label of the mushroom (either edible or poisonous)
     */
    public static Object classify(Node node, Mushroom mushroom){
            Object classLabel = findClass(mushroom, node);
            return classLabel;
        }

    private static Class_Label findClass(Mushroom mushroom, Node node) {
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
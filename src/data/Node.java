package data;

import enums.Class_Label;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private Node parent = null;
    private List<Node> children;
    // the partitioned data set
    private List<Mushroom> data;
    // list of attributes minus the attribute we split on to get here
    private List<Object> attributes;
    // class label gets set when node is a isLeaf node
    private Class_Label m_Class = null;
    // the attribute we split on to get to this node
    private Object splitting_criterion;
    // the attribute value that got us to this node
    private Object value;

    private boolean isLeaf;

    public Node(List<Mushroom> data, List<Object> attributes) {
        this.attributes = attributes;
        this.data = data;
        children = new ArrayList<>();
        isLeaf = false;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    // set class label
    public void setLabel(Object m_Class) {
        if(m_Class.equals(Class_Label.edible)) {
            this.m_Class = Class_Label.edible;
        }

        if(m_Class.equals(Class_Label.poisonous)) {
            this.m_Class = Class_Label.poisonous;
        }
    }

    // label node with splitting criterion
    public void setSplittingCriterion(Object splittingCriterion) {
        splitting_criterion = splittingCriterion;
    }

    public Object getSplitting_criterion() {
        return splitting_criterion;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public List<Node> getChildren() {
        return children;
    }

    public Class_Label getM_Class() {
        return m_Class;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Marks the current node as a leaf node
     */
    public void setLeaf() { isLeaf = true; }

    public boolean isLeaf() { return isLeaf; }

    @Override
    public String toString() {
        return "Attribute: " + parent.getSplitting_criterion().toString() +
                "\nValue: " + value +
                "\nNumber of mushrooms: " + data.size() +
                "\nNumber of children: " + children.size() +
                "\nNext splitting criterion: " + splitting_criterion +
                "\nCLASS LABEL: " + m_Class;
    }
}

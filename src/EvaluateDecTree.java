import classification.ID3;
import data.Mushroom;
import data.Node;
import enums.Class_Label;
import java.util.List;

public class EvaluateDecTree {

    public static String classify(Node node, List<Mushroom> testdata) {
        double total = testdata.size();
        double truePos = 0;
        double trueNeg = 0;
        double falsePos = 0;
        double falseNeg = 0;
        double trueTotal, falseTotal,truePosPerc, trueNegPerc, falsePosPerc, falseNegPerc, trueTotalPerc, falseTotalPerc;

        for(Mushroom mush : testdata) {
            Object prediction = ID3.classify(node, mush);
            Object actual = mush.getAttributeValue(Class_Label.class);

            if(prediction.equals(Class_Label.edible)) {
                if(actual.equals(Class_Label.edible)) truePos++;
                if(actual.equals(Class_Label.poisonous)) falsePos++;
            }

            if(prediction.equals(Class_Label.poisonous)) {
                if(actual.equals(Class_Label.edible)) falseNeg++;
                if(actual.equals(Class_Label.poisonous)) trueNeg++;

            }
        }

        trueTotal = trueNeg + truePos;
        falseTotal = falseNeg + falsePos;

        truePosPerc = (truePos / total) * 100;
        trueNegPerc = (trueNeg / total) * 100;
        falsePosPerc = (falsePos / total) * 100;
        falseNegPerc = (falseNeg / total) * 100;

        trueTotalPerc = (trueTotal / total) * 100;
        falseTotalPerc = (falseTotal / total) * 100;

        StringBuilder result = new StringBuilder();
        result.append(String.format("\nTrue Pos: %.2f", truePosPerc)).append("%");
        result.append(String.format("\nTrue Neg: %.2f", trueNegPerc)).append("%");
        result.append(String.format("\nTotal True: %.2f", trueTotalPerc)).append("%\n");
        result.append(String.format("\nFalse Pos: %.2f", falsePosPerc)).append("%");
        result.append(String.format("\nFalse Neg: %.2f", falseNegPerc)).append("%");
        result.append(String.format("\nTotal Neg: %.2f", falseTotalPerc)).append("%\n");

        return result.toString();
    }
}

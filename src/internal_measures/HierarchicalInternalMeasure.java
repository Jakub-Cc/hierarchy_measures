package internal_measures;

import basic_hierarchy.common.Constants;
import basic_hierarchy.common.Utils;
import basic_hierarchy.implementation.BasicHierarchy;
import basic_hierarchy.implementation.BasicNode;
import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Instance;
import basic_hierarchy.interfaces.Node;
import basic_hierarchy.test.TestCommon;
import common.CommonQualityMeasure;

import java.util.HashMap;
import java.util.LinkedList;

public class HierarchicalInternalMeasure extends CommonQualityMeasure {
    private CommonQualityMeasure internalMeasure;

    private HierarchicalInternalMeasure(){}

    public HierarchicalInternalMeasure(CommonQualityMeasure internalMeasure) {
        this.internalMeasure = internalMeasure;
    }

    @Override
    public double getMeasure(Hierarchy h) {
        LinkedList<Node> divisionNodes = new LinkedList<>();
        double weightedSumOfDivisionNodeMeasures = 0.0;
        double sumOfWeights = 0.0;

        for(Node n: h.getGroups())
        {
            if(!n.getChildren().isEmpty() && (n.getChildren().size() + n.getNodeInstances().size()) > 1)
            {
                divisionNodes.add(n);
            }
        }

        for(Node n: divisionNodes)
        {
            LinkedList<Node> nodesToCalculateInternalMeasure = new LinkedList<>();
            BasicNode artificialRoot = new BasicNode(Constants.ROOT_ID, null, new LinkedList<Node>(), new LinkedList<Instance>(), false);
            nodesToCalculateInternalMeasure.add(artificialRoot);
            int childCounter = 0;
            int numberOfInstances = 0;

            for(Node ch: n.getChildren())
            {
                Node child = new BasicNode(TestCommon.getIDOfChildCluster(Constants.ROOT_ID, (childCounter++)),
                        artificialRoot, new LinkedList<Node>(), ch.getNodeInstances(), false);
                artificialRoot.addChild(child);
                nodesToCalculateInternalMeasure.add(child);
                numberOfInstances += ch.getNodeInstances().size();
            }

            for(Instance i: n.getNodeInstances())
            {
                LinkedList<Instance> nodeContent = new LinkedList<>();
                nodeContent.add(i);
                Node instanceNode = new BasicNode(TestCommon.getIDOfChildCluster(Constants.ROOT_ID, (childCounter++)),
                        artificialRoot, new LinkedList<Node>(), nodeContent, false);
                artificialRoot.addChild(instanceNode);
                nodesToCalculateInternalMeasure.add(instanceNode);
                numberOfInstances++;
            }
            Hierarchy artificialHierarchy = new BasicHierarchy(artificialRoot, nodesToCalculateInternalMeasure, new HashMap<String, Integer>(), numberOfInstances);

            double weight = n.getSubtreeInstances().size();
            weightedSumOfDivisionNodeMeasures += weight*this.internalMeasure.getMeasure(artificialHierarchy);
            sumOfWeights += weight;

//            System.out.println("artificial:");
//            artificialHierarchy.printTree();
        }
        return (sumOfWeights == 0.0)? this.internalMeasure.getNotDesiredValue(): weightedSumOfDivisionNodeMeasures/sumOfWeights;
    }

    @Override
    public double getDesiredValue() {
        return predictHIMValue(this.internalMeasure.getDesiredValue());
    }

    @Override
    public double getNotDesiredValue() {
        return predictHIMValue(this.internalMeasure.getNotDesiredValue());
    }

    private double predictHIMValue(double value) {
        if(value > 0) {
            return Double.MAX_VALUE;
        }
        else if(value < 0) {
            return Double.MIN_VALUE;
        }
        return 0.0;
    }
}

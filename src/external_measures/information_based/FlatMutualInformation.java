package external_measures.information_based;

import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Node;
import common.Utils;
import interfaces.DistanceMeasure;

/**
 * Implemented based on http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 */
public class FlatMutualInformation extends FlatEntropy {
    public FlatMutualInformation()
    {
        super(2.0);
    }

    public FlatMutualInformation(double logBase)
    {
        super(logBase);
    }

    @Override
    public double getMeasure(Hierarchy h, DistanceMeasure dist) {
        int overallNumberOfObjects = h.getNumberOfInstances();
        double measure = 0.0;
        for(Node n: h.getGroups())
        {
            int nodeInstancesCount = n.getNodeInstances().size();
            for(String c: h.getClasses())
            {
                int classInstancesWithinNodeCount = Utils.getClassInstancesWithinNode(n, c, false, false).size();
                int classCount = h.getClassCount(c, false);
                measure += (
                            (classInstancesWithinNodeCount/(double)overallNumberOfObjects)
                            * (
                               Math.log(
                                        (overallNumberOfObjects*classInstancesWithinNodeCount)
                                        /(double)(nodeInstancesCount*classCount)
                                       )/this.baseLogarithm
                              )
                            );
            }
        }
        return measure;
    }
}

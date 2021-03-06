package hierarchy_measures.external_measures.information_based;

import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Node;

/**
 * Normalized version of Mutual Information is more robust to reach better
 * values when number of nodes increases
 */
public class FlatNormalizedMutualInformation extends FlatEntropy {
	private FlatMutualInformation mIIndex = null;

	public FlatNormalizedMutualInformation() {
		super(2.0);
		mIIndex = new FlatMutualInformation(2.0);
	}

	public FlatNormalizedMutualInformation(double logBase) {
		super(logBase);
		mIIndex = new FlatMutualInformation(logBase);
	}

	@Override
	public double getMeasure(Hierarchy h) {
		double mutualInformation = mIIndex.getMeasure(h);

		String[] classes = h.getClasses();
		int[] classesCount = new int[h.getNumberOfClasses()];
		for (int i = 0; i < classesCount.length; i++) {
			classesCount[i] = h.getParticularClassCount(classes[i], false);
		}
		double classEntropy = calculateEntropy(classesCount, h.getOverallNumberOfInstances());

		Node[] nodes = h.getGroups();
		int[] nodesCount = new int[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			nodesCount[i] = nodes[i].getNodeInstances().size();
		}
		double nodeEntropy = calculateEntropy(nodesCount, h.getOverallNumberOfInstances());

		if ((classEntropy + nodeEntropy) != 0)
			return mutualInformation / ((classEntropy + nodeEntropy) / 2.0);

		return Double.NaN;
	}

	@Override
	public double getDesiredValue() {
		return 1.0;
	}

	@Override
	public double getNotDesiredValue() {
		return 0;
	}

	private double calculateEntropy(int[] count, int overallNumberOfInstances) {
		double entropy = 0.0;
		for (int i = 0; i < count.length; i++) {
			if (count[i] != 0) {
				double factor = count[i] / (double) overallNumberOfInstances;
				entropy += factor * Math.log(factor) / this.baseLogarithm;
			}
		}
		return (-1) * entropy;
	}
}

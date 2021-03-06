package hierarchy_measures.common;

import java.util.ArrayList;

import basic_hierarchy.interfaces.Hierarchy;
import hierarchy_measures.interfaces.QualityMeasure;
import hierarchy_measures.internal_measures.statistics.AvgWithStdev;

public abstract class CommonQualityMeasure implements QualityMeasure {

	@Override
	public AvgWithStdev getMeasure(ArrayList<Hierarchy> hierarchies, boolean calculatePopulationStdev) {
		double[] values = new double[hierarchies.size()];
		for (int i = 0; i < hierarchies.size(); i++) {
			values[i] = this.getMeasure(hierarchies.get(i));
		}
		return new AvgWithStdev(Utils.mean(values), Utils.stdev(values, calculatePopulationStdev));
	}

}

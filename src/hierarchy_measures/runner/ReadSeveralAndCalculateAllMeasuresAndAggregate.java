package hierarchy_measures.runner;

import static hierarchy_measures.common.Consts.MSG_DONE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import basic_hierarchy.interfaces.Hierarchy;
import hierarchy_measures.distance_measures.Euclidean;
import hierarchy_measures.external_measures.AdaptedFmeasure;
import hierarchy_measures.external_measures.information_based.FlatEntropy1;
import hierarchy_measures.external_measures.information_based.FlatEntropy2;
import hierarchy_measures.external_measures.information_based.FlatInformationGain;
import hierarchy_measures.external_measures.information_based.FlatMutualInformation;
import hierarchy_measures.external_measures.information_based.FlatNormalizedMutualInformation;
import hierarchy_measures.external_measures.purity.FlatClusterPurity;
import hierarchy_measures.external_measures.purity.HierarchicalClassPurity;
import hierarchy_measures.external_measures.statistical_hypothesis.FlatHypotheses;
import hierarchy_measures.external_measures.statistical_hypothesis.Fmeasure;
import hierarchy_measures.external_measures.statistical_hypothesis.FowlkesMallowsIndex;
import hierarchy_measures.external_measures.statistical_hypothesis.JaccardIndex;
import hierarchy_measures.external_measures.statistical_hypothesis.PartialOrderHypotheses;
import hierarchy_measures.external_measures.statistical_hypothesis.RandIndex;
import hierarchy_measures.interfaces.DistanceMeasure;
import hierarchy_measures.interfaces.QualityMeasure;
import hierarchy_measures.internal_measures.FlatCalinskiHarabasz;
import hierarchy_measures.internal_measures.FlatDaviesBouldin;
import hierarchy_measures.internal_measures.FlatDunn1;
import hierarchy_measures.internal_measures.FlatDunn2;
import hierarchy_measures.internal_measures.FlatDunn3;
import hierarchy_measures.internal_measures.FlatDunn4;
import hierarchy_measures.internal_measures.FlatWithinBetweenIndex;
import hierarchy_measures.internal_measures.HierarchicalInternalMeasure;
import hierarchy_measures.internal_measures.VarianceDeviation;
import hierarchy_measures.internal_measures.VarianceDeviation2;
import hierarchy_measures.internal_measures.statistics.AvgPathLength;
import hierarchy_measures.internal_measures.statistics.AvgWithStdev;
import hierarchy_measures.internal_measures.statistics.CommonStatistic;
import hierarchy_measures.internal_measures.statistics.Height;
import hierarchy_measures.internal_measures.statistics.NumberOfLeaves;
import hierarchy_measures.internal_measures.statistics.NumberOfNodes;
import hierarchy_measures.internal_measures.statistics.histogram.ChildPerNodePerLevel;
import hierarchy_measures.internal_measures.statistics.histogram.CommonPerLevelHistogram;
import hierarchy_measures.internal_measures.statistics.histogram.HistogramOfNumberOfChildren;
import hierarchy_measures.internal_measures.statistics.histogram.InstancesPerLevel;
import hierarchy_measures.internal_measures.statistics.histogram.LeavesPerLevel;
import hierarchy_measures.internal_measures.statistics.histogram.NodesPerLevel;

//TODO: przerobic ta klase tak, ze bedzie ona normalna klasa ze stanem i funkcja run, ktora bedzie sie odpalalo z NOWEJ klasy CL_ReadSeveralAndCalculateAllMeasuresAndAggregate
// dokladnie tak jak zrobilem z klasa CL_ReadSeveralAndCalculateQualityMeasuresSeparately
public class ReadSeveralAndCalculateAllMeasuresAndAggregate extends CommonReadSeveralAndCalculate {

	private static final Logger log = LogManager.getLogger(ReadSeveralAndCalculateAllMeasuresAndAggregate.class);

	public static void main(String[] args) {
		// parameters
		boolean useSubtree = true;
		boolean withClassAttribute = true;
		double logBase = 2.0;
		double varianceDeviationAlpha = 1.0;
		String resultFilePath = "measuresAndStatictics.csv";

		ArrayList<Hierarchy> hierarchies = loadHierarchies(args, useSubtree, withClassAttribute);

		DistanceMeasure measure = new Euclidean();

		HashMap<String, CommonStatistic> basicStatistics = createBasicStatics();
		AvgPathLength apt = new AvgPathLength();

		HashMap<String, QualityMeasure> qualityMeasures = getQualityMeasureHashMap(logBase, varianceDeviationAlpha,
				measure);

		HashMap<String, CommonPerLevelHistogram> histograms = getHistogramsHashMap();

		try {
			writeHeaderIfEmptyFile(withClassAttribute, resultFilePath, true, false);

			try (BufferedWriter resultFile = new BufferedWriter(new FileWriter(resultFilePath, true))) {
				resultFile.append(Boolean.toString(useSubtree) + ";");
			}
			log.info("Calculating..");

			calculateAndSaveAllBasicStatistics(resultFilePath, hierarchies, basicStatistics, apt);
			calculateAndSaveAllFlatInternalMeasures(resultFilePath, hierarchies, qualityMeasures);
			if (withClassAttribute)
				calculateAndSaveAllExternalMeasures(resultFilePath, hierarchies, qualityMeasures);
			calculateAndSaveAllHIMVariants(resultFilePath, hierarchies, qualityMeasures);
			calculateAndSaveAllHistogramicMeasures(resultFilePath, hierarchies, histograms);

		} catch (IOException e) {
			log.error(e);
		}
	}

	private static void calculateAndSaveAllHistogramicMeasures(String resultFilePath, ArrayList<Hierarchy> hierarchies,
			HashMap<String, CommonPerLevelHistogram> histograms) throws IOException {
		log.info("Nodes per level histogram..");
		try (BufferedWriter resultFileSpacing = new BufferedWriter(new FileWriter(resultFilePath, true))) {
			resultFileSpacing.append("%n%n");
		}
		calculateAndWriteHistogramicValues("Nodes per level histogram", hierarchies, histograms,
				NodesPerLevel.class.getName(), resultFilePath);
		log.info("Done.%nLeaves per level histogram..");
		calculateAndWriteHistogramicValues("Leaves per level histogram", hierarchies, histograms,
				LeavesPerLevel.class.getName(), resultFilePath);
		log.info("Done.%nInstances per level histogram..");
		calculateAndWriteHistogramicValues("Instances per level histogram", hierarchies, histograms,
				InstancesPerLevel.class.getName(), resultFilePath);
		log.info("Done.%nChild per node per level histogram..");
		calculateAndWriteHistogramicValues("Child per node per level histogram", hierarchies, histograms,
				ChildPerNodePerLevel.class.getName(), resultFilePath);
		log.info("Done.%nHistogram of number of children..");
		calculateAndWriteHistogramicValues("Histogram of number of children", hierarchies, histograms,
				HistogramOfNumberOfChildren.class.getName(), resultFilePath);
		log.info(MSG_DONE);
	}

	private static void calculateAndSaveAllHIMVariants(String resultFilePath, ArrayList<Hierarchy> hierarchies,
			HashMap<String, QualityMeasure> qualityMeasures) throws IOException {
		log.info("Done.%nHIM + FlatWithinBetweenIndex..");
		calculateAndSaveQualityMeasure(
				HierarchicalInternalMeasure.class.getName() + FlatWithinBetweenIndex.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nHIM + FlatDunn1..");
		calculateAndSaveQualityMeasure(HierarchicalInternalMeasure.class.getName() + FlatDunn1.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nHIM + FlatDunn2..");
		calculateAndSaveQualityMeasure(HierarchicalInternalMeasure.class.getName() + FlatDunn2.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nHIM + FlatDunn3..");
		calculateAndSaveQualityMeasure(HierarchicalInternalMeasure.class.getName() + FlatDunn3.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nHIM + FlatDunn4..");
		calculateAndSaveQualityMeasure(HierarchicalInternalMeasure.class.getName() + FlatDunn4.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nHIM + FlatDaviesBouldin..");
		calculateAndSaveQualityMeasure(HierarchicalInternalMeasure.class.getName() + FlatDaviesBouldin.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nHIM + FlatCalinskiHarabasz..");
		calculateAndSaveQualityMeasure(
				HierarchicalInternalMeasure.class.getName() + FlatCalinskiHarabasz.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info(MSG_DONE);
	}

	private static void calculateAndSaveAllExternalMeasures(String resultFilePath, ArrayList<Hierarchy> hierarchies,
			HashMap<String, QualityMeasure> qualityMeasures) throws IOException {
		log.info("Flat cluster purity..");
		calculateAndSaveQualityMeasure(FlatClusterPurity.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nHierarchcal purity..");
		calculateAndSaveQualityMeasure(HierarchicalClassPurity.class.getName(), hierarchies, qualityMeasures,
				resultFilePath);
		log.info("Done.%nFmasure with flat hypotheses..");
		calculateAndSaveQualityMeasure(Fmeasure.class.getName() + FlatHypotheses.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nFmeasure with partial order hypotheses..");
		calculateAndSaveQualityMeasure(Fmeasure.class.getName() + PartialOrderHypotheses.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nAdapted Fmeasure with instances inheritance..");
		calculateAndSaveQualityMeasure(AdaptedFmeasure.class.getName() + Boolean.toString(true), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nAdapted Fmeasure with NO instances inheritance..");
		calculateAndSaveQualityMeasure(AdaptedFmeasure.class.getName() + Boolean.toString(false), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nFowlkes Mallows with flat hypotheses..");
		calculateAndSaveQualityMeasure(FowlkesMallowsIndex.class.getName() + FlatHypotheses.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFowlkes Mallows with partial order hypotheses..");
		calculateAndSaveQualityMeasure(FowlkesMallowsIndex.class.getName() + PartialOrderHypotheses.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nRand with flat hypotheses..");
		calculateAndSaveQualityMeasure(RandIndex.class.getName() + FlatHypotheses.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nRand with partial order hypotheses..");
		calculateAndSaveQualityMeasure(RandIndex.class.getName() + PartialOrderHypotheses.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nJaccard with flat hypotheses..");
		calculateAndSaveQualityMeasure(JaccardIndex.class.getName() + FlatHypotheses.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nJaccard with partial order hypotheses..");
		calculateAndSaveQualityMeasure(JaccardIndex.class.getName() + PartialOrderHypotheses.class.getName(),
				hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat entropy 1..");
		calculateAndSaveQualityMeasure(FlatEntropy1.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat entropy 2..");
		calculateAndSaveQualityMeasure(FlatEntropy2.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat information gain with flat entropy 1..");
		calculateAndSaveQualityMeasure(FlatInformationGain.class.getName() + FlatEntropy1.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nFlat information gain with flat entropy 2..");
		calculateAndSaveQualityMeasure(FlatInformationGain.class.getName() + FlatEntropy2.class.getName(), hierarchies,
				qualityMeasures, resultFilePath);
		log.info("Done.%nFlat mutual information..");
		calculateAndSaveQualityMeasure(FlatMutualInformation.class.getName(), hierarchies, qualityMeasures,
				resultFilePath);
		log.info("Done.%nFlat normalized mutual information..");
		calculateAndSaveQualityMeasure(FlatNormalizedMutualInformation.class.getName(), hierarchies, qualityMeasures,
				resultFilePath);
		log.info(MSG_DONE);
	}

	private static void calculateAndSaveAllFlatInternalMeasures(String resultFilePath, ArrayList<Hierarchy> hierarchies,
			HashMap<String, QualityMeasure> qualityMeasures) throws IOException {
		log.info("Variance deviation..");
		calculateAndSaveQualityMeasure(VarianceDeviation.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nVariance deviation 2..");
		calculateAndSaveQualityMeasure(VarianceDeviation2.class.getName(), hierarchies, qualityMeasures,
				resultFilePath);
		log.info("Done.%nFlat within between..");
		calculateAndSaveQualityMeasure(FlatWithinBetweenIndex.class.getName(), hierarchies, qualityMeasures,
				resultFilePath);
		log.info("Done.%nFlat Dunn 1..");
		calculateAndSaveQualityMeasure(FlatDunn1.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat Dunn 2..");
		calculateAndSaveQualityMeasure(FlatDunn2.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat Dunn 3..");
		calculateAndSaveQualityMeasure(FlatDunn3.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat Dunn 4..");
		calculateAndSaveQualityMeasure(FlatDunn4.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat Davies-Bouldin..");
		calculateAndSaveQualityMeasure(FlatDaviesBouldin.class.getName(), hierarchies, qualityMeasures, resultFilePath);
		log.info("Done.%nFlat Calinski-Charabasz..");
		calculateAndSaveQualityMeasure(FlatCalinskiHarabasz.class.getName(), hierarchies, qualityMeasures,
				resultFilePath);
		log.info(MSG_DONE);
	}

	private static void calculateAndSaveAllBasicStatistics(String resultFilePath, ArrayList<Hierarchy> hierarchies,
			HashMap<String, CommonStatistic> basicStatistics, AvgPathLength apt) throws IOException {
		log.info("Number of nodes..");
		calculateAndSaveBasicStatistics(NumberOfNodes.class.getName(), hierarchies, basicStatistics, resultFilePath);
		log.info("Done.%nNumber of leaves..");
		calculateAndSaveBasicStatistics(NumberOfLeaves.class.getName(), hierarchies, basicStatistics, resultFilePath);
		log.info("Done.%nHeight..");
		calculateAndSaveBasicStatistics(Height.class.getName(), hierarchies, basicStatistics, resultFilePath);
		log.info("Done.%nAvg path length..");
		try (BufferedWriter resultFileAvgPathLength = new BufferedWriter(new FileWriter(resultFilePath, true))) {
			resultFileAvgPathLength.append(getAvgAndStdevInOutputFormat(apt.calculate(hierarchies, false)));
			log.info(MSG_DONE);
		}
	}

	private static void calculateAndSaveQualityMeasure(String nameOfTheQualityMeasure, ArrayList<Hierarchy> hierarchies,
			HashMap<String, QualityMeasure> qualityMeasures, String resultFilePath) throws IOException {
		try (BufferedWriter resultFile = new BufferedWriter(new FileWriter(resultFilePath, true))) {
			resultFile.append(getAvgAndStdevInOutputFormat(
					qualityMeasures.get(nameOfTheQualityMeasure).getMeasure(hierarchies, false)));
		}
	}

	private static void calculateAndSaveBasicStatistics(String nameOfTheStatistic, ArrayList<Hierarchy> hierarchies,
			HashMap<String, CommonStatistic> basicStatistics, String resultFilePath) throws IOException {
		try (BufferedWriter resultFile = new BufferedWriter(new FileWriter(resultFilePath, true))) {
			resultFile.append(getAvgAndStdevInOutputFormat(
					basicStatistics.get(nameOfTheStatistic).calculate(hierarchies, false)));
		}
	}

	private static void calculateAndWriteHistogramicValues(String histogramName, ArrayList<Hierarchy> hierarchies,
			HashMap<String, CommonPerLevelHistogram> perLevelHistograms, String histogramicMeasureName,
			String resultFilePath) throws IOException {
		try (BufferedWriter resultFile = new BufferedWriter(new FileWriter(resultFilePath, true))) {
			AvgWithStdev[] histogram = perLevelHistograms.get(histogramicMeasureName).calculate(hierarchies, false);

			StringBuilder histBin = new StringBuilder("");
			StringBuilder histAvg = new StringBuilder("");
			StringBuilder histStdev = new StringBuilder("");
			for (int i = 0; i < histogram.length; i++) {
				histBin.append(i + ";");
				histAvg.append(histogram[i].getAvg() + ";");
				histStdev.append(histogram[i].getStdev() + ";");
			}
			histBin.append("%n");
			histAvg.append("%n");
			histStdev.append("%n");
			resultFile.append(histogramName + "%n");
			resultFile.append(histBin.toString());
			resultFile.append(histAvg.toString());
			resultFile.append(histStdev.toString());
			resultFile.append("%n");
		}
	}

	private static String getAvgAndStdevInOutputFormat(AvgWithStdev values) {
		return values.getAvg() + ";" + values.getStdev() + ";";
	}
}

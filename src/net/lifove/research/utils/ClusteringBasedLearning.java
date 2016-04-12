package net.lifove.research.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import net.lifove.research.utils.WekaUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import com.google.common.primitives.Doubles;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveRange;

public class ClusteringBasedLearning {
	
	final int folds = 2;
	final int repeat= 500;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ClusteringBasedLearning().run(args);
	}

	String dataPath = "";
	String projectName = "";
	String groupName = "";
	int numSelectedMaxClusters = 1;
	int numSelectedMinClusters = 1;
	int numAllClustersGenerated = -1;
	int percentile = 50;
	
	String selectedAttributeIndices;
	
	public enum cutoffType {
		MEAN,MEDIAN,MAXMIN,P10,P25,P75,P90
	}
	
	public void run(String[] args) {
		
		// load parameters
		dataPath = args[0];
		groupName = args[1];
		projectName = args[2];
		String classAttributeName = args[3];
		String positiveLabel = args[4];
		numSelectedMaxClusters = Integer.parseInt(args[5]);
		numSelectedMinClusters = Integer.parseInt(args[6]);
		percentile = Integer.parseInt(args[7]);
		boolean generateNewArffOnly = Boolean.parseBoolean(args[8]);
		boolean generateLPUSourceArff = Boolean.parseBoolean(args[9]);
		String strCuttOffType = args[10];
		boolean applyFeatureSelection = Boolean.parseBoolean(args[11]);
		boolean computeFeatureSelectionOnly = args[12]==null?false:Boolean.parseBoolean(args[12]);
		
		// load arff file
		Instances instances = WekaUtils.loadArff(dataPath, classAttributeName);
		
		// create HashMaps that contain cluster information and list of instance indices
		HashMap<Integer,ClusterInfo> numPositivelyAgreedFeatures = new HashMap<Integer,ClusterInfo>(); // key: instance index, value: ClusterInfo
		HashMap<Integer,String> instIdxByAgreedFeatureNum = new HashMap<Integer,String>();// key: numAgreedFeatures value; list of instance indices, instance index starts from 1

		// Retrieve each instance and fill HashMaps
		for(int instIdx=0;instIdx<instances.numInstances();instIdx++){
			int numAgreed = 0;
			String associationKey = "";
			for(int attrIdx=0;attrIdx<instances.numAttributes()-1;attrIdx++){
				if(attrIdx==instances.classIndex())
					continue;
				
				double attributeValue = instances.instance(instIdx).value(attrIdx);
				
				double attributeCutoffValue = getCutoffValue(strCuttOffType,instances,attrIdx);
				
				if(attributeValue>attributeCutoffValue){
					associationKey += attrIdx + "-";
					numAgreed++;
				}
			}
			ClusterInfo clusterInfo = new ClusterInfo();
			clusterInfo.numOfAgreedFeautures = numAgreed;
			clusterInfo.associatedFeatures = associationKey;
			//instances.attribute(labelName).indexOfValue(posClassValue);
			clusterInfo.originalLabel = WekaUtils.getStringValueOfInstanceLabel(instances,instIdx);
			numPositivelyAgreedFeatures.put(instIdx, clusterInfo);
			if(instIdxByAgreedFeatureNum.containsKey(numAgreed))
				instIdxByAgreedFeatureNum.put(numAgreed,instIdxByAgreedFeatureNum.get(numAgreed) + (instIdx+1) +",");
			else{
				instIdxByAgreedFeatureNum.put(numAgreed,(instIdx+1)+",");
			}
		}
		
		// get the number of all clusters
		numAllClustersGenerated = instIdxByAgreedFeatureNum.size();
		
		// if there are no specified number of clusters, just use half and half.
		if(numSelectedMaxClusters==-1 && numSelectedMinClusters==-1){
			numSelectedMaxClusters = numAllClustersGenerated/2;
			numSelectedMinClusters = numAllClustersGenerated-numSelectedMaxClusters;
		}
				
		// get instance index in top `n' clusters, n=numSelectedMaxClusters
		// instIdx starts from 1
		String instIdxWithMaxAgreedNum = getInstancesWithMaxAgreedNum(instIdxByAgreedFeatureNum,instances.numAttributes());
		// get instance index in bottom `n' clusters, n=numSelectedMinClusters
		// instIdx starts from 1
		String instIdxWithMinAgreedNum = getInstancesWithMinAgreedNum(instIdxByAgreedFeatureNum,instances.numAttributes());
		
		// feature selection
		//selectedAttributeIndices = getselectedAttributeIndices(numPositivelyAgreedFeatures,
		//		instIdxByAgreedFeatureNum,
		//		numSelectedMaxClusters,numSelectedMinClusters,
		//		instances.classIndex());
		int nthBestConflictScore = 1;
		selectedAttributeIndices = getSelectedAttriuteIndicesBasedOnConflictScore(instances,instIdxWithMaxAgreedNum,instIdxWithMinAgreedNum,instances.classIndex(),nthBestConflictScore);
		
		if(!computeFeatureSelectionOnly){
			// generate and label the training dataset from clusters
			Instances rawSourceInstances = generateAndLabelNewSourceData(instances,instIdxWithMaxAgreedNum,instIdxWithMinAgreedNum,positiveLabel,false);
			Instances newSourceInstances = null;
			// do prediction or save dataset files
			// don't conduct STDP in case that selected clusters are greater than the the number of all clusters.
			if(!generateNewArffOnly){
				if(numAllClustersGenerated < numSelectedMaxClusters + numSelectedMinClusters){
					System.exit(0);
				}
				
				Instances tarInstances = new Instances(instances);
				
				// initial feature selection
				if(applyFeatureSelection){
					newSourceInstances = WekaUtils.getInstancesByRemovingSpecificAttributes(rawSourceInstances,
							this.selectedAttributeIndices, true);
					tarInstances = WekaUtils.getInstancesByRemovingSpecificAttributes(instances,
							this.selectedAttributeIndices, true);
					
					//System.out.println(selectedAttributeIndices);
				}
				
				//--------------------
				// instance selection
				//--------------------
				while(true){
					
					// instance selection based on instances whose all feature values are greater than mean attribute value (buggy) or less than / same as mean attribute value 
					newSourceInstances = selectInstances(newSourceInstances,tarInstances, positiveLabel);
					int numBuggyNewInstances = WekaUtils.getNumInstancesByClass(newSourceInstances, positiveLabel);
					
					//if(nthBestConflictScore==selectedAttributeIndices.split(",").length-1){ // if n-th is the last two feature
					//newSourceInstances = WekaUtils.getInstancesByRemovingSpecificAttributes(rawSourceInstances, selectedAttributeIndices, true);
					//tarInstances = WekaUtils.getInstancesByRemovingSpecificAttributes(instances, selectedAttributeIndices, true);;
						//break;
					//}
					
					if(numBuggyNewInstances==0 || newSourceInstances.numInstances()-numBuggyNewInstances==0){
						// select less features to get more instances, repeat this until both buggy and clean instances are selected.
						selectedAttributeIndices = getSelectedAttriuteIndicesBasedOnConflictScore(instances,instIdxWithMaxAgreedNum,instIdxWithMinAgreedNum,instances.classIndex(),++nthBestConflictScore);
						//System.out.println(selectedAttributeIndices);
						 
						if(applyFeatureSelection){
							newSourceInstances = WekaUtils.getInstancesByRemovingSpecificAttributes(rawSourceInstances,
									this.selectedAttributeIndices, true);
							tarInstances = WekaUtils.getInstancesByRemovingSpecificAttributes(instances,
									this.selectedAttributeIndices, true);
						}
						continue;
					}
					break;
				}
				
				//System.out.println("training dataset ready!");
				// run cross-prediction
				System.out.println(groupName + "," + projectName + "," + SimpleCrossPredictor.crossPredictionOnTheSameSplit(newSourceInstances, tarInstances, instances, positiveLabel, repeat, folds));
				//crossPredictionOnTheSameSplit(newSourceInstances,instances,positiveLabel,applyFeatureSelection);
			}
			else{
				FileUtil.writeAFile(newSourceInstances.toString(), "data" + File.separator + groupName + File.separator + projectName + ".arff");
				if(generateLPUSourceArff){
					Instances newLPUSourceInstances = generateAndLabelNewSourceData(instances,instIdxWithMaxAgreedNum,instIdxWithMinAgreedNum,positiveLabel,true);
					FileUtil.writeAFile(newLPUSourceInstances.toString(), "data" + File.separator + groupName + File.separator + projectName + "_LPU.arff");
				}
			}
		}
	}
	
	private Instances selectInstances(Instances newSourceInstances, Instances tarInstances, String strPosLabel) {
		
		String instanceIndicesToBeRemoved = "";
		for(int instIdx=0;instIdx<newSourceInstances.numInstances();instIdx++){
			for(int attrIdx=0;attrIdx<newSourceInstances.numAttributes();attrIdx++){
				if(attrIdx==newSourceInstances.classIndex())
					continue;
				
				//double attrMean = tarInstances.attributeStats(attrIdx).numericStats.mean;
				double attrMedian = tarInstances.kthSmallestValue(attrIdx, tarInstances.numInstances()/2);
				
						// if buggy
				if(newSourceInstances.instance(instIdx).value(newSourceInstances.classIndex())==WekaUtils.getClassValueIndex(newSourceInstances, strPosLabel)){
					if(newSourceInstances.instance(instIdx).value(attrIdx)<=attrMedian){
						instanceIndicesToBeRemoved += (instIdx+1) + ",";
						break;
					}
				}
				else{ // if clean instance
					if(newSourceInstances.instance(instIdx).value(attrIdx)>attrMedian){
						instanceIndicesToBeRemoved += (instIdx+1) + ",";
						break;
					}
				}
			}
		}
		
		return WekaUtils.getInstancesByRemovingSpecificInstances(newSourceInstances,instanceIndicesToBeRemoved,false);
	}

	/**
	 * get selected attribute indices based on conflict score
	 * Conflict score is computed by (# of instances whose feature values > Mean in top clusters)/# of instances in top clusters
	 * @param instances
	 * @param instIdxWithMaxAgreedNum
	 * @param instIdxWithMinAgreedNum
	 * @param classIndex
	 * @return
	 */
	private String getSelectedAttriuteIndicesBasedOnConflictScore(
			Instances instances, String instIdxWithMaxAgreedNum,String instIdxWithMinAgreedNum, int classIndex,int nthBestConflictScore) {
		
		String selectedFeatures = "";
		String[] instIndicesInTopClusters = instIdxWithMaxAgreedNum.split(",");
		String[] instIndicesInBottomClusters = instIdxWithMinAgreedNum.split(",");
		double[] conflictScore = new double[instances.numAttributes()-1];
		
		for(int attrIdx=0;attrIdx<instances.numAttributes();attrIdx++){	// attrIdx starts from 0
			if(attrIdx==classIndex)
				continue;
			
			double attrMedian = instances.kthSmallestValue(attrIdx, instances.numInstances()/2);//instances.attributeStats(attrIdx).numericStats.mean;
			int conflictCounter = 0;
			// conflicts on top clusters
			for(int i=0;i<instIndicesInTopClusters.length;i++){
				int instIdx = Integer.parseInt(instIndicesInTopClusters[i])-1; // index in instIndicesInTopClusters starts from 1
				if(instances.get(instIdx).value(attrIdx)<=attrMedian)
					conflictCounter++;
			}
			// conflicts on bottom clusters
			for(int i=0;i<instIndicesInBottomClusters.length;i++){
				int instIdx = Integer.parseInt(instIndicesInBottomClusters[i])-1; // index in instIndicesInTopClusters starts from 1
				if(instances.get(instIdx).value(attrIdx)>attrMedian)
					conflictCounter++;
			}
			
			conflictScore[attrIdx] = (double)conflictCounter/(instIndicesInTopClusters.length+instIndicesInBottomClusters.length);
		}
		
		double cutoffOfConflictScore = getMinCscore(conflictScore);//getBestFeatureConflictScoreCutoff(conflictScore,instances,instIdxWithMaxAgreedNum,instIdxWithMinAgreedNum,classIndex,nthBestConflictScore);
		//double cutoffOfConflictScore = getBestFeatureConflictScoreCutoff(conflictScore,instances,instIdxWithMaxAgreedNum,instIdxWithMinAgreedNum,classIndex,nthBestConflictScore);
		
		//System.out.println(cutoffPercentileForConflictScore);
		for(int attrIdx=0;attrIdx<conflictScore.length;attrIdx++){
			if(conflictScore[attrIdx] <= cutoffOfConflictScore)// || conflictScore[attrIdx]==0.0)
				selectedFeatures+= (attrIdx+1) +",";
		}
		
		return selectedFeatures + (classIndex + 1);
	}

	private double getMinCscore(double[] conflictScore) {
		
		return (new DescriptiveStatistics(conflictScore)).getMin();
	}

	/**
	 * get the best conflict score cutoff based on minimizing conflict score in both instance and feature conflict score
	 * @param featureConflictScores // array index starts from 0
	 * @param instances
	 * @param instIdxWithMaxAgreedNum
	 * @param instIdxWithMinAgreedNum
	 * @param classIndex
	 * @return
	 */
	private double getBestFeatureConflictScoreCutoff(double[] featureConflictScores,Instances instances,
			String instIdxWithMaxAgreedNum, String instIdxWithMinAgreedNum,
			int classIndex,int nthBestConflictScore) {
		
		HashMap<Double,Double> totalConflictScores = new HashMap<Double,Double>(); // key: total conflict score, value: "currentConflictScoreCutoff" 
		
		// compute total conflict scores
		ArrayList<Double> computedConflictScoreCutoff = new ArrayList<Double>(); // to skip already processed score
		for(int attrIdx=0;attrIdx<featureConflictScores.length;attrIdx++){ 
			// to avoid recomputing total conflict score in the case that feature conflict score is same
			if(computedConflictScoreCutoff.contains(featureConflictScores[attrIdx])){
				continue;
			}
			
			computedConflictScoreCutoff.add(featureConflictScores[attrIdx]);

			ArrayList<Integer> selectedAttributes = new ArrayList<Integer>(); // selected features by using currentFeatureConflictScoreCutoff
			// selected attributes for the current conflict score cutoff
			selectedAttributes = getSelectedAttributes(instances,featureConflictScores,featureConflictScores[attrIdx]);
			//if(selectedAttributes.size()==1) // at least 2 features required for this conflict score based approach
			//	continue;

			// Compute instance conflict score using selected attributes by the current conflictScoreCutoff
			// (1) count conflicts from top clusters
			String[] instIndicesInTopClusters = instIdxWithMaxAgreedNum.split(",");
			int numConflictAttributes = 0;
			int numAllAttributes = 0;
			for(int i = 0;i<instIndicesInTopClusters.length;i++){
				int instIdx = Integer.parseInt(instIndicesInTopClusters[i])-1; // index in instIndicesInTopClusters starts from 1
				
				for(int selectedAttrIdx:selectedAttributes){
					double attrMedian = instances.kthSmallestValue(selectedAttrIdx, instances.numInstances()/2);//instances.attributeStats(attrIdx).numericStats.mean;
					if(instances.get(instIdx).value(selectedAttrIdx) <=attrMedian){
						numConflictAttributes++;
						//break;
					}
					numAllAttributes++;
				}
			}
			// (2) count conflicts from bottom clusters
			String[] instIndicesInBottomClusters = instIdxWithMinAgreedNum.split(",");
			for(int i = 0;i<instIndicesInBottomClusters.length;i++){
				int instIdx = Integer.parseInt(instIndicesInBottomClusters[i])-1; // index in instIndicesInTopClusters starts from 1
				
				for(int selectedAttrIdx:selectedAttributes){
					double attrMedian = instances.kthSmallestValue(selectedAttrIdx, instances.numInstances()/2);//instances.attributeStats(attrIdx).numericStats.mean;
					if(instances.get(instIdx).value(selectedAttrIdx) > attrMedian){
						numConflictAttributes++;
						//break;
					}	
					numAllAttributes++;
				}
			}
			
			// (3) compute 
			double InstanceConflictScore = ((double)numConflictAttributes/numAllAttributes);//(instIndicesInTopClusters.length+instIndicesInBottomClusters.length); // always > 0
			
			// compute the total conflict score and put it into totalConflictScores
			//totalConflictScores.put(((instances.numAttributes())-selectedAttributes.size())*(currentFeatureConflictScoreCutoff + InstanceConflictScore)/2,currentFeatureConflictScoreCutoff);
			totalConflictScores.put(((featureConflictScores[attrIdx] + InstanceConflictScore)/2)/selectedAttributes.size(),featureConflictScores[attrIdx]);
			//System.out.println(selectedAttributes.size() + "," + featureConflictScores[attrIdx] + "," + InstanceConflictScore + "," + ((featureConflictScores[attrIdx] + InstanceConflictScore)/2));
			
		}
		
		// find minimum total conflict scores and its key is the best cutoff for the feature conflict score.
		SortedSet<Double> keys = new TreeSet<Double>(totalConflictScores.keySet()); // keys are in ascending order.
		
		double bestCutoff = -1;
		int nth=0;
		
		/*for(Double key:keys){
			System.out.println(key + " " + totalConflictScores.get(key));
		}*/
		
		for(Double key:keys){
			bestCutoff = totalConflictScores.get(key);
			nth++;
			if(nthBestConflictScore==nth)
				break;
		}
		
		return bestCutoff;
	}

	private ArrayList<Integer> getSelectedAttributes(Instances instances,
			double[] featureConflictScores,double cutoff) {
		
		ArrayList<Integer> selectedFeatures = new ArrayList<Integer>();
		for(int attrIdx=0;attrIdx<featureConflictScores.length;attrIdx++){
			if(featureConflictScores[attrIdx] <= cutoff)// || conflictScore[attrIdx]==0.0)
				selectedFeatures.add(attrIdx);
		}
		return selectedFeatures;
	}

	public String getSelectedAttributeIndices(){
		return selectedAttributeIndices;
	}

	/**
	 * Feature selection based on positively (=more than the cutoff feature value) agreed features
	 * @param numPositivelyAgreedFeatures
	 * @param instIdxByAgreedFeatureNum
	 * @param numSelectedMaxClusters
	 * @param numSelectedMinClusters
	 * @param classIndex
	 * @return string value of selected attribute indices. Please, note, for string index value, the first index starts from 1.
	 */
	private String getselectedAttributeIndices(
			HashMap<Integer, ClusterInfo> numPositivelyAgreedFeatures,	// key: instance index
			HashMap<Integer, String> instIdxByAgreedFeatureNum,			// key: numAgreedFeatures
			int numSelectedMaxClusters,
			int numSelectedMinClusters,
			int classIndex) {

		// maxKeys, minKeys
		
		ArrayList<Integer> commonAttributeIndices = null;
		for(int maxKey:maxKeys){
			String[] instanceIndices = instIdxByAgreedFeatureNum.get(maxKey).split(",");
			for(int i=0;i < instanceIndices.length;i++){
				
				if(commonAttributeIndices == null){
					// initial assignment for commonAttributeIndices.
					// after that we remove attribute that was not agreed in other instances.
					commonAttributeIndices = new ArrayList<Integer>();
					// the attribute indices of an instance
					String[] attrIndices = numPositivelyAgreedFeatures.get((Integer.parseInt(instanceIndices[i])-1)).associatedFeatures.split("-");
					// For the instance, put all features, whose value is greater than the cutoff, in commonAttributeIndices
					for(int itrForAttrIndices=0;itrForAttrIndices<attrIndices.length;itrForAttrIndices++){
						commonAttributeIndices.add(Integer.parseInt(attrIndices[itrForAttrIndices]));
					}
				}else{
					ArrayList<Integer> newCommonAttributeIndices = new ArrayList<Integer>();
					// the attribute indices of an instance
					String[] attrIndices = numPositivelyAgreedFeatures.get((Integer.parseInt(instanceIndices[i])-1)).associatedFeatures.split("-");
					// For the instance, put all features, whose value is greater than the cutoff, in commonAttributeIndices
					for(int itrForAttrIndices=0;itrForAttrIndices<attrIndices.length;itrForAttrIndices++){
						int attrIndex = Integer.parseInt(attrIndices[itrForAttrIndices]);
						// Put an attribute index, that already contains in commonAttributeIndices, in newCommonAttributeIndices.
						// By this execution, only attributes, that are greater than the cutoff, in all instances in this loop remains.
						// So, size of newCommonAttributeIndices is getting smaller.
						if(commonAttributeIndices.contains(attrIndex))
							newCommonAttributeIndices.add(attrIndex);
					}
					commonAttributeIndices = newCommonAttributeIndices;
				}
			}
		}
		
		// find and remove from commonAttributeIndices if there are positively agree feature in botton clusters
		for(int minKey:minKeys){
			String[] instanceIndices = instIdxByAgreedFeatureNum.get(minKey).split(",");
			for(int i=0;i < instanceIndices.length;i++){
				String associatedFeatures = numPositivelyAgreedFeatures.get((Integer.parseInt(instanceIndices[i])-1)).associatedFeatures;
				if(associatedFeatures.equals(""))
					break;
				String[] attrIndices = associatedFeatures.split("-");
				for(int itrForAttrIndices=0;itrForAttrIndices<attrIndices.length;itrForAttrIndices++){
					int attrIndex = Integer.parseInt(attrIndices[itrForAttrIndices]);
					commonAttributeIndices.remove((Integer)attrIndex);
				}
			}
		}
		
		String strIndices = "";
		
		// convert into String. for String index, the first index starts from 1. So need to add one for all attribute index
		for(int value:commonAttributeIndices)
			strIndices += (value+1) + ",";
		
		return strIndices +  (classIndex+1);
	}

	private double getCutoffValue(String strCuttOffType,Instances instances,int attrIdx) {
		DescriptiveStatistics stat = null;
		switch(cutoffType.valueOf(strCuttOffType)){
			case MEAN:
				return instances.attributeStats(attrIdx).numericStats.mean;
			case MEDIAN:
				 stat = new DescriptiveStatistics(instances.attributeToDoubleArray(attrIdx));
				return stat.getPercentile(50);
			case MAXMIN:
				return (instances.attributeStats(attrIdx).numericStats.max+
						instances.attributeStats(attrIdx).numericStats.min)/2;
			case P10:
				stat = new DescriptiveStatistics(instances.attributeToDoubleArray(attrIdx));
				return stat.getPercentile(10);
			case P25:
				stat = new DescriptiveStatistics(instances.attributeToDoubleArray(attrIdx));
				return stat.getPercentile(25);
			case P75:
				stat = new DescriptiveStatistics(instances.attributeToDoubleArray(attrIdx));
				return stat.getPercentile(75);
			case P90:
				stat = new DescriptiveStatistics(instances.attributeToDoubleArray(attrIdx));
				return stat.getPercentile(90);
		}
		
		return instances.attributeStats(attrIdx).numericStats.mean;
	}

	ArrayList<Integer> maxKeys = new ArrayList<Integer>(); // key for instances with the same agreed features.
	private String getInstancesWithMaxAgreedNum(
			HashMap<Integer, String> instIdxByAgreedFeatureNum,int numFeatures) {

		int clusterCount = 0;
		for(int key=numFeatures;key>=0;key--){
			if(instIdxByAgreedFeatureNum.containsKey(key)){
				maxKeys.add(key);
				if(clusterCount==numSelectedMaxClusters-1)
					break;
				clusterCount++;
			}
		}
		
		return getInstIdxByAgreedFeatureNum(instIdxByAgreedFeatureNum,maxKeys);
	}

	ArrayList<Integer> minKeys = new ArrayList<Integer>();
	private String getInstancesWithMinAgreedNum(
			HashMap<Integer, String> instIdxByAgreedFeatureNum,int numFeatures) {
		
		int clusterCount = 0;
		
		for(int key=0;key<=numFeatures;key++){
			if(instIdxByAgreedFeatureNum.containsKey(key)){
				minKeys.add(key);
				if(clusterCount==numSelectedMinClusters-1)
					break;
				clusterCount++;
			}
		}
		
		return getInstIdxByAgreedFeatureNum(instIdxByAgreedFeatureNum,minKeys);
	}

	private String getInstIdxByAgreedFeatureNum(HashMap<Integer, String> instIdxByAgreedFeatureNum,ArrayList<Integer> keys) {
		String indices = "";
		
		for(int key:keys){
			indices += instIdxByAgreedFeatureNum.get(key);
		}
		
		return indices;
	}

	int newPosInstCount = 0;
	int newNegInstCount = 0;
	
	private Instances generateAndLabelNewSourceData(Instances instances,String selectedInstanceIdxForBuggy,String selectedInstanceIdxForClean,String posLabel,boolean forLPUSource) {
		
		Instances newInstances = null;
		
		RemoveRange instFilter = new RemoveRange();
		RemoveRange instFilter2 = new RemoveRange();
		instFilter.setInstancesIndices(selectedInstanceIdxForBuggy);
		instFilter.setInvertSelection(true);
		
		if(!forLPUSource){
			instFilter2.setInstancesIndices(selectedInstanceIdxForClean);
			instFilter2.setInvertSelection(true);
		}else{
			instFilter2.setInstancesIndices(selectedInstanceIdxForBuggy);
			instFilter2.setInvertSelection(false);
		}
		
		try {
			instFilter.setInputFormat(instances);
			Instances newPosInstances = Filter.useFilter(instances, instFilter);
			for(int instIdx=0;instIdx<newPosInstances.numInstances();instIdx++){
				newPosInstances.get(instIdx).setClassValue(posLabel);
			}
			
			newPosInstCount = newPosInstances.numInstances();
			
			instFilter2.setInputFormat(instances);
			Instances newNegInstances = Filter.useFilter(instances, instFilter2);
			for(int instIdx=0;instIdx<newNegInstances.numInstances();instIdx++){
				newNegInstances.get(instIdx).setClassValue(WekaUtils.getNegClassStringValue(newNegInstances, instances.classAttribute().name(), posLabel));
			}
			
			newNegInstCount = newNegInstances.numInstances();
			
			newInstances = new Instances(newPosInstances);
			
			for(int instIdx=0;instIdx<newNegInstances.numInstances();instIdx++){
				newInstances.add(newNegInstances.get(instIdx));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return newInstances;
	}
}

class ClusterInfo{
	int numOfAgreedFeautures = 0;
	String associatedFeatures = "";
	String originalLabel = "";
}

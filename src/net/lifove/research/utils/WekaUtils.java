package net.lifove.research.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.apache.commons.math3.util.MathUtils;

import com.google.common.primitives.Doubles;

import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.evaluation.TwoClassStats;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Standardize;
import weka.filters.unsupervised.instance.RemoveRange;

public class WekaUtils {
	static final public String strPos = "buggy";
	static final public String strNeg = "clean"; 
	static final public String labelName = "label";
	static public double dblPosValue = 0;
	static public double dblNegValue = 1;

	static public void main(String[] args){
		if(args[0].equals("saveCrossValidationFold")){
			if(args.length==6)
				saveCrossValidationFold(args[1], args[2], args[3],Integer.parseInt(args[4]),Integer.parseInt(args[5]));
			else
				System.out.println("Please, specify a method name as the first option!");

			System.exit(0);
		}

		if(args[0].equals("predictionResultsFromEnsemble")){
			if(args.length==4)
				predictionResultsFromEnsemble(args[1],args[2],args[3],WekaUtils.strPos);
			else
				System.out.println("Please, specify a method name as the first option!");

			System.exit(0);
		}

		System.out.println("Please, specify a method name as the first option and addition options!");
	}

	/**
	 * Write an arff file with instances
	 * @param instances
	 * @param targetFileName
	 */
	static public void writeADataFile(Instances instances,String targetFileName){
		try {
			File file= new File(targetFileName);
			if(file.exists()){
				return;
			}

			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);

			dos.write((instances.toString()).getBytes());

			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("FileName: " + targetFileName);
			System.exit(0);
		} 
	}

	/**
	 * Create a list of attributes for the given number of attributes
	 * @param numOfAttributes
	 * @return ArrayList of Attribute
	 */
	static public ArrayList<Attribute> createAttributeInfoForClassfication(long numOfAttributes){
		// create attribute information
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// add attributes from matchedAttribute
		for(int i=0; i<numOfAttributes-1;i++){
			Attribute attribute = new Attribute("attr" + (i+1));
			attributes.add(attribute);
		}

		//add label as the last attribute
		ArrayList<String> labels = new ArrayList<String>();
		labels.add(strPos);
		labels.add(strNeg);
		Attribute label = new Attribute(labelName, labels);
		attributes.add(label);

		dblPosValue = attributes.get(attributes.size()-1).indexOfValue(strPos);
		dblNegValue = attributes.get(attributes.size()-1).indexOfValue(strNeg);

		return attributes;
	}

	public static Measures predictionResultsFromEnsemble(String predictionInfo,String pathForPredictions1,String pathForPredictions2,String buggyLabel){

		Measures measures1 = getMeauresFromDetailedResults(pathForPredictions1);
		Measures measures2 = getMeauresFromDetailedResults(pathForPredictions2);

		Measures measures = getMeauresFromDetailedResults(pathForPredictions1,pathForPredictions2);

		double precision1 = ArrayListUtil.getMedian(measures1.getPrecisions());
		double precision2 = ArrayListUtil.getMedian(measures2.getPrecisions());
		double precisionEnsemble = ArrayListUtil.getMedian(measures.getPrecisions());
		double recall1 = ArrayListUtil.getMedian(measures1.getRecalls());
		double recall2 = ArrayListUtil.getMedian(measures2.getRecalls());
		double recallEnsemble = ArrayListUtil.getMedian(measures.getRecalls());
		double fmeasure1 = ArrayListUtil.getMedian(measures1.getFmeasures());
		double fmeasure2 = ArrayListUtil.getMedian(measures2.getFmeasures());
		double fmeasureEnsemble = ArrayListUtil.getMedian(measures.getFmeasures());

		System.out.println(predictionInfo + "," + precision1 + "," +precision2 +","+precisionEnsemble + "," +
				recall1 +","+recall2+","+recallEnsemble +"," +
				fmeasure1 + "," +fmeasure2 + "," + fmeasureEnsemble);

		return measures;
	}

	private static Measures getMeauresFromDetailedResults(
			String pathForPredictions1,String pathForPredictions2) {

		Measures measures = new Measures();
		int TP=0,FP=0,TN=0,FN=0;
		boolean skipFirst = true;
		ArrayList<String> lines1 = FileUtil.getLines(pathForPredictions1, false);
		ArrayList<String> lines2 = FileUtil.getLines(pathForPredictions2, false);

		for(int i=0;i<lines1.size();i++){
			if(lines1.get(i).contains("===")){

				if(skipFirst){
					skipFirst = false;
				}else{
					double precision = (double)TP/(TP+FP);
					if(Double.isNaN(precision))
						precision=0.0;
					double recall = (double)TP/(TP+FN);
					if(Double.isNaN(recall))
						recall=0.0;
					double fmeasure = (double)2*(precision*recall)/(double)(precision+recall);

					if(Double.isNaN(fmeasure)){
						fmeasure=0.0;
					}

					measures.setFmeasure(fmeasure);
					measures.setPrecision(precision);
					measures.setRecall(recall);


					TP = 0;
					FP = 0;
					TN = 0;
					FN = 0;
				}
			}else{
				String[] splitLine1 = lines1.get(i).split(",");
				int length1 = splitLine1.length;
				String actual1 = splitLine1[length1-2];
				String predicted1 = splitLine1[length1-1];

				String[] splitLine2 = lines2.get(i).split(",");
				int length2 = splitLine2.length;
				String actual2 = splitLine2[length2-2];
				String predicted2 = splitLine2[length2-1];

				if(!actual1.equals(actual2)){
					System.out.println("Data is not cnsistent. Actual labels that should be same are different");
					System.exit(0);
				}

				if(actual1.equals(WekaUtils.strPos)){
					if(actual1.equals(predicted1) && actual1.equals(predicted2))
						TP++;
					else
						FN++;

				}else{
					if(!actual1.equals(predicted1) && !actual1.equals(predicted2) )
						FP++;
					else
						TN++;
				}
			}
		}

		double precision = (double)TP/(TP+FP);
		if(Double.isNaN(precision))
			precision=0.0;
		double recall = (double)TP/(TP+FN);
		if(Double.isNaN(recall))
			recall=0.0;
		double fmeasure = 2*(precision*recall)/(precision+recall);

		if(Double.isNaN(fmeasure)){
			fmeasure=0.0;
		}

		measures.setFmeasure(fmeasure);
		measures.setPrecision(precision);
		measures.setRecall(recall);

		return measures;
	}

	private static Measures getMeauresFromDetailedResults(
			String pathForPredictions1) {

		Measures measures = new Measures();
		int TP=0,FP=0,TN=0,FN=0;
		boolean skipFirst = true;
		for(String line:FileUtil.getLines(pathForPredictions1, false)){
			if(line.contains("===")){

				if(skipFirst){
					skipFirst = false;
				}else{
					double precision = (double)TP/(TP+FP);
					if(Double.isNaN(precision))
						precision=0.0;
					double recall = (double)TP/(TP+FN);
					if(Double.isNaN(recall))
						recall=0.0;

					double fmeasure = (double)2*(precision*recall)/(double)(precision+recall);

					if(Double.isNaN(fmeasure)){
						fmeasure=0.0;
					}

					measures.setFmeasure(fmeasure);
					measures.setPrecision(precision);
					measures.setRecall(recall);


					TP = 0;
					FP = 0;
					TN = 0;
					FN = 0;
				}
			}else{
				String[] splitLine = line.split(",");
				int length = splitLine.length;
				String actual = splitLine[length-2];
				String predicted = splitLine[length-1];

				if(actual.equals(WekaUtils.strPos)){
					if(actual.equals(predicted))
						TP++;
					else
						FN++;

				}else{
					if(actual.equals(predicted))
						TN++;
					else
						FP++;
				}
			}
		}

		double precision = (double)TP/(TP+FP);
		if(Double.isNaN(precision))
			precision=0.0;
		double recall = (double)TP/(TP+FN);
		if(Double.isNaN(recall))
			recall=0.0;
		double fmeasure = 2*(precision*recall)/(precision+recall);

		if(Double.isNaN(fmeasure)){
			fmeasure=0.0;
		}

		measures.setFmeasure(fmeasure);
		measures.setPrecision(precision);
		measures.setRecall(recall);

		return measures;
	}
	
	
	static String[] buggyLabels = {"buggy","TRUE"};
	public static String getPosLabel(Instances instances) {
		
		for(int i=0; i < instances.classAttribute().numValues();i++){
			String value = instances.classAttribute().value(i);
			
			for(String posValue:buggyLabels){
				if(value.equals(posValue))
					return value;
			}
		}
		
		return null;
	}

	public static void generateRDataForBeanPlotsFromArff(String arffPath,String saveTo,String className,String strBuggyLabel){
		Instances instances = loadArff(arffPath,className);

		// save csv file.
		ArrayList<String> lines = new ArrayList<String>();

		// csv file format: metrics_{buggy,clean}, metric value
		lines.add("metric,Label,value");

		for(int attrIdx=0;attrIdx<instances.numAttributes();attrIdx++){
			// skip class attribute
			if(attrIdx == instances.classIndex())
				continue;
			for(int instIdx=0;instIdx<instances.numInstances();instIdx++){
				String label = instances.instance(instIdx).stringValue(instances.classIndex());
				if(label.equals(strBuggyLabel))
					label=strPos;
				else
					label=strNeg;
				lines.add("M" + String.format("%02d", attrIdx+1) + "," + label + "," + instances.instance(instIdx).value(attrIdx));
				//lines.add("M" + String.format("%03d", attrIdx+1) + ",all," + instances.instance(instIdx).value(attrIdx));

			}
		}

		FileUtil.writeAFile(lines, saveTo);

	}

	/**
	 * Generate independent arff files for each fold of n-fold corss validation
	 * @param dataPath directory where an arff file is
	 * @param dataFileName arff file name
	 * @param savingDir directory name for the arff for each fold
	 * @param repeat number of repetition
	 * @param folds n-fold
	 */
	static void saveCrossValidationFold(String dataPath,  String dataFileName, String savingDir,int repeat,int folds){

		Instances instances = loadArff(dataPath + File.separator + dataFileName);

		File createdDir = new File(savingDir);
		if(!createdDir.exists()){
			if(!(new File(savingDir).mkdirs())){
				System.err.println(savingDir +" is not created");
				System.exit(0);
			}
		}

		for(int i=0;i<repeat;i++){

			instances.randomize(new Random(i)); 
			instances.stratify(folds);

			for(int n=0;n<folds;n++){
				Instances testInstances = instances.testCV(folds, n);
				FileUtil.writeAFile(testInstances + "", savingDir + File.separator + dataFileName.replace(".arff", "") + "_" + i + "_" + n + ".arff");
			}
		}
	}

	/**
	 * Create a list of attributes for the given number of attributes
	 * @param list of attribute names as String
	 * @return ArrayList of Attribute
	 */
	static public ArrayList<Attribute> createAttributeInfoWithAttributeNamesForClassfication(String listOfAttributes){
		// create attribute information
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		String[] attributeNames = listOfAttributes.split(",");

		// add attributes from matchedAttribute
		for(int i=0; i<attributeNames.length;i++){
			Attribute attribute = new Attribute(attributeNames[i]);
			attributes.add(attribute);
		}

		//add label as the last attribute
		ArrayList<String> labels = new ArrayList<String>();
		labels.add(strPos);
		labels.add(strNeg);
		Attribute label = new Attribute(labelName, labels);
		attributes.add(label);

		dblPosValue = attributes.get(attributes.size()-1).indexOfValue(strPos);
		dblNegValue = attributes.get(attributes.size()-1).indexOfValue(strNeg);

		return attributes;
	}

	/**
	 * Normalize data set values ranged from 0 to 1
	 * @param data
	 * @return normalized instances
	 */
	static public Instances applyNormalize(Instances data){
		Instances normalizedData=null;
		// normalize data
		Normalize normalize = new Normalize();
		try {
			normalize.setInputFormat(data);
			normalizedData = Filter.useFilter(data, normalize);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return normalizedData;
	}

	/**
	 * Standardize data set mean and std of values of each attribute as 0 and 1 respectively
	 * @param data
	 * @return Standardize (z-score normalization) instances
	 */
	static public Instances applyStandardize(Instances data){
		Instances standardizedData=null;
		// standardize data
		Standardize standardize = new Standardize();
		try {
			standardize.setInputFormat(data);
			standardizedData = Filter.useFilter(data, standardize);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return standardizedData;
	}

	/**
	 * Normalize data set values by Log filter
	 * @param data
	 * @return normalized instances
	 */
	static public Instances applyNormalizeByLog(Instances data){	
		int classIndex = data.classIndex();

		for(int curAttrIdx = 0; curAttrIdx<data.numAttributes();curAttrIdx++){
			if(curAttrIdx == classIndex) continue;

			for(int curInstIdx = 0; curInstIdx < data.numInstances(); curInstIdx++){
				double value = data.instance(curInstIdx).value(curAttrIdx);

				if (value<=0.000001)
					value = 0.000001;
				Math.log(value);

				data.instance(curInstIdx).setValue(curAttrIdx, value);
			}
		}
		return data;
	}

	/**
	 * Apply PCA for instances
	 * @param data
	 * @return Instances
	 */
	static public Instances ApplyPCA(Instances data){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		PrincipalComponents eval = new PrincipalComponents();
		filter.setEvaluator(eval);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}

	/**
	 * Feature selection by GainRatioAttributeEval
	 * @param data
	 * @return newData with selected attributes
	 */
	static public Instances featrueSelectionByGainRatioAttributeEval(Instances data){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		GainRatioAttributeEval eval = new GainRatioAttributeEval();
		Ranker search = new Ranker();
		search.setThreshold(-1.7976931348623157E308);
		search.setNumToSelect(-1);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}

	/**
	 * Feature selection by CfsSubsetEval
	 * @param data
	 * @return newData with selected attributes
	 */
	static public Instances featrueSelectionByCfsSubsetEval(Instances data){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();
		//search.ssetSearchBackwards(false);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}

	/**
	 * Feature selection filter by CfsSubsetEval
	 * @param data
	 * @return AttributeSelection filter
	 */
	static public AttributeSelection getAttributesSelectionFilterByCfsSubsetEval(Instances data){

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();
		//search.ssetSearchBackwards(false);
		filter.setEvaluator(eval);
		filter.setSearch(search);

		try {
			filter.setInputFormat(data);

			// generate new data
			//newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filter;
	}

	/**
	 * Return a matrix in the format of 2-d double array from instances
	 * @param instances
	 * @return matrix in 2d double array
	 */
	static public double[][] getMatrixFromInstances(Instances instances){
		double[][] matrix = new double[instances.numInstances()][instances.numAttributes()-1];
		int indexOfClassLabel = instances.classIndex();

		for(int i=0;i<instances.numInstances();i++){
			// ignore class label
			if(i==indexOfClassLabel)
				continue;

			for(int attrIndex=0;attrIndex<instances.numAttributes()-1;attrIndex++){
				matrix[i][attrIndex] = instances.get(i).value(attrIndex);
			}
		}

		return matrix;
	}

	/**
	 * Get String labels from the instances
	 * @param instances
	 * @param sourceLabel
	 * @param srcBuggyLabel
	 * @return ArrayList of labels
	 */
	static public ArrayList<String> getArrayListOfLabels(Instances instances,
			String sourceLabel, String srcBuggyLabel) {
		ArrayList<String> labels = new ArrayList<String>();
		int classIndex = instances.classIndex();
		double buggyValue = WekaUtils.getClassValueIndex(instances, srcBuggyLabel);

		for(int i = 0; i < instances.numInstances() ;i++){
			labels.add(instances.get(i).value(classIndex)==buggyValue?strPos:strNeg);
		}

		return labels;
	}

	/**
	 * Get class value index for the given class name
	 * @param instances
	 * @param className
	 * @return int
	 */
	static public int getClassValueIndex(Instances instances, String labelStrValue){
		return instances.attribute(instances.classIndex()).indexOfValue(labelStrValue);
	}

	/**
	 * Get num of instnaces with a specific lavel value
	 * @param instances
	 * @param className
	 * @return int
	 */
	static public int getNumInstancesByClass(Instances instances, String labelStrValue){
		return instances.attributeStats(instances.classAttribute().index()).nominalCounts[WekaUtils.getClassValueIndex(instances, labelStrValue)];
	}

	/**
	 * Get string value of a negative class for the given positive class
	 * @param instances
	 * @param labelName
	 * @param posClassValue
	 * @return null
	 */
	static public String getNegClassStringValue(Instances instances,String labelName,String posClassValue){
		if(instances.attribute(labelName).numValues()==2){
			int posIndex = instances.attribute(labelName).indexOfValue(posClassValue);
			if(posIndex==0)
				return instances.attribute(labelName).value(1);
			else
				return instances.attribute(labelName).value(0);
		}
		else{

			System.exit(0);
		}
		return null;
	}

	/**
	 * Get label value of an instance
	 * @param instances
	 * @param instance index
	 * @return string label of an instance
	 */
	static public String getStringValueOfInstanceLabel(Instances instances,int intanceIndex){
		return instances.instance(intanceIndex).stringValue(instances.classIndex());
	}

	/**
	 * Get instances with a specific label (e.g, only buggy instances)
	 * @param instances
	 * @param posLabelStrValue Always String value of positive label.
	 * @param isPosTarget If positive is our target label (getting only all positive instances?), it should be true, otherwise false.
	 * @return instances of a specific label
	 */
	static public Instances getInstancesBySpecificLabel(Instances instances,String posLabelStrValue,boolean isPosTarget){
		Instances newInstances = new Instances(instances);
		int poslabelIndex = getClassValueIndex(newInstances,posLabelStrValue);

		ArrayList<Instance> intancesToRemove = new ArrayList<Instance>();

		for(Instance instanceToInspect:newInstances){
			double curLabelIndex = instanceToInspect.value(instanceToInspect.classIndex());

			if(isPosTarget){
				if(curLabelIndex!=poslabelIndex)
					intancesToRemove.add(instanceToInspect);
			}else{
				if(curLabelIndex==poslabelIndex)
					intancesToRemove.add(instanceToInspect);
			}
		}

		newInstances.removeAll(intancesToRemove);

		return newInstances;
	}

	/**
	 * Get instances by removing specific instances
	 * @param instances
	 * @param instance indices (e.g., 1,3,4) first index is 1
	 * @param option for invert selection
	 * @return selected instances
	 */
	static public Instances getInstancesByRemovingSpecificInstances(Instances instances,String instanceIndices,boolean invertSelection){
		Instances newInstances = null;

		RemoveRange instFilter = new RemoveRange();
		instFilter.setInstancesIndices(instanceIndices);
		instFilter.setInvertSelection(invertSelection);

		try {
			instFilter.setInputFormat(instances);
			newInstances = Filter.useFilter(instances, instFilter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newInstances;
	}

	/**
	 * Get instances with specific attributes
	 * @param instances
	 * @param attributeIndices attribute indices (e.g., 1,3,4) first index is 1
	 * @param invertSelection for invert selection
	 * @return new instances with specific attributes
	 */
	static public Instances getInstancesByRemovingSpecificAttributes(Instances instances,String attributeIndices,boolean invertSelection){
		Instances newInstances = new Instances(instances);

		Remove remove;

		remove = new Remove();
		remove.setAttributeIndices(attributeIndices);
		remove.setInvertSelection(invertSelection);
		try {
			remove.setInputFormat(newInstances);
			newInstances = Filter.useFilter(newInstances, remove);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return newInstances;
	}

	/**
	 * Get instances with specific attributes
	 * @param instances
	 * @param attribute indices in int[]. first index is 0.
	 * @param option for invert selection
	 * @return new instances with specific attributes
	 */
	static public Instances getInstancesByRemovingSpecificAttributes(Instances instances,int[] intAttributeIndices,boolean invertSelection){

		String attributeIndices = "";

		for(int index:intAttributeIndices){
			attributeIndices += (index+1) +",";
		}

		return getInstancesByRemovingSpecificAttributes(instances,attributeIndices,invertSelection);
	}

	/**
	 * Get similar attribute indices within a project
	 * @param instances
	 * @return int[] contains similar attribute indices
	 */
	static public int[] selectSimilarFeatures(Instances instances) {

		//KolmogorovSmirnovTest KStest = new KolmogorovSmirnovTest();
		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		ArrayList<Integer> selectedAttrIndice = new ArrayList<Integer>();
		ArrayList<String> processedMatching = new ArrayList<String>();

		// find common features where the KS-test p-value is greater than 0.5
		for(int attrIndex=0;attrIndex<instances.numAttributes();attrIndex++){
			if(instances.attribute(attrIndex)==instances.classAttribute())
				continue;

			double[] sample1 = instances.attributeToDoubleArray(attrIndex);

			for(int attrIndex2=0;attrIndex2<instances.numAttributes();attrIndex2++){
				if(instances.attribute(attrIndex2)==instances.classAttribute() || attrIndex==attrIndex2)
					continue;

				double[] sample2 = instances.attributeToDoubleArray(attrIndex2);

				if(!(processedMatching.contains(attrIndex+"_" + attrIndex2) || processedMatching.contains(attrIndex2+"_" + attrIndex))){
					double pvalue =  statTest.wilcoxonSignedRankTest(sample1, sample2, false);//KStest.kolmogorovSmirnovTest(sample1, sample2);				
					if(pvalue>=0.05){
						processedMatching.add(attrIndex + "_" + attrIndex2);

						if(!selectedAttrIndice.contains(attrIndex))
							selectedAttrIndice.add(attrIndex);

						if(!selectedAttrIndice.contains(attrIndex2))
							selectedAttrIndice.add(attrIndex2);
					}
				}
			}
		}

		selectedAttrIndice.add(instances.classIndex());

		return ArrayListUtil.getIntPromitive(selectedAttrIndice);
	}

	/**
	 * To get instances from a list of instance indice
	 * @param instances
	 * @param indice
	 * @return
	 */
	static public Instances getInstancesFromIndice(Instances instances,ArrayList<Integer> indice){

		Instances selectedInstances = new Instances(instances,0);

		for(Integer index:indice){
			selectedInstances.add(instances.get(index));
		}

		return selectedInstances;
	}

	/**
	 * To get instances with selected attributes and new pos and neg labels
	 * @param instances
	 * @param selectedAttributes
	 * @param posLabel
	 * @return
	 */
	static public Instances getInstancesWithSelectedAttributes(Instances instances,ArrayList<Integer> selectedAttributes,String posLabel){

		// create attribute information
		ArrayList<Attribute> attributes = WekaUtils.createAttributeInfoForClassfication(selectedAttributes.size()+1); // +1 for label
		Instances newInstances =new Instances("newInstancesWithSelectedAttributes", attributes, 0);

		for(Instance instance:instances){
			double[] vals = new double[attributes.size()];

			// process attribute values except for label
			for(int i=0; i<attributes.size()-1;i++){
				vals[i] = instance.value(selectedAttributes.get(i));
			}

			// assign label value
			String currentInstaceLabel = instance.stringValue(instances.attribute(instance.classIndex()));
			if(currentInstaceLabel.equals(posLabel))
				vals[attributes.size()-1] = WekaUtils.dblPosValue;
			else
				vals[attributes.size()-1] = WekaUtils.dblNegValue;

			newInstances.add(new DenseInstance(1.0, vals));
		}

		newInstances.setClassIndex(attributes.size()-1);

		return newInstances;
	}

	static public String getSelectedAttributesByCorrelationAnalysis(Instances instances){
		String selectedAttributes = "";
		double corCutoff = 0.9;
		HashMap<Integer,ArrayList<Integer>> attributeClusters = new HashMap<Integer,ArrayList<Integer>>();

		for(int attrIdx = 0; attrIdx < instances.numAttributes(); attrIdx++){
			if(attrIdx==instances.classIndex())
				continue;

			double[] attributeValues = instances.attributeToDoubleArray(attrIdx);

			ArrayList<Integer> attributes = new ArrayList<Integer>();
			attributes.add(attrIdx);
			attributeClusters.put(attrIdx, attributes);

			for(int attrIdx2 = 0; attrIdx2 < instances.numAttributes(); attrIdx2++){
				if(attrIdx2==instances.classIndex() || attrIdx==attrIdx2)
					continue;

				double[] attributeValues2 = instances.attributeToDoubleArray(attrIdx2);
				attributeValues = replaceNaNInZero(attributeValues);
				attributeValues2 = replaceNaNInZero(attributeValues2);

				// compute Spearman correlation
				SpearmansCorrelation sc = new SpearmansCorrelation();
				double correlation = sc.correlation(attributeValues, attributeValues2);

				if(correlation>corCutoff)
					attributeClusters.get(attrIdx).add(attrIdx2);
			}
		}

		//compute statistic of atributeClusters
		ArrayList<Integer> sizeOfAttrClusters = new ArrayList<Integer>();
		for(Integer key:attributeClusters.keySet()){
			sizeOfAttrClusters.add(attributeClusters.get(key).size());
		}

		//double median = ArrayListUtil.getMedianFromIntegerArrayList(sizeOfAttrClusters);
		double mean = ArrayListUtil.getMeanFromIntegerArrayList(sizeOfAttrClusters);

		for(Integer key:attributeClusters.keySet()){
			//System.out.println("Cluster " + key + ": " + attributeClusters.get(key).size());
			if(attributeClusters.get(key).size()>=mean)//(instances.numAttributes()*0.15))
				selectedAttributes += (key+1) +","; // add 1 since the first attribute index is 1 for Filter.
		}

		selectedAttributes += (instances.classIndex() + 1); //add class index
		return selectedAttributes;
	}

	private static double[] replaceNaNInZero(double[] attributeValues) {
		for(int i=0;i<attributeValues.length;i++){
			if(Double.isNaN(attributeValues[i]))
				attributeValues[i]=0;

		}
		return attributeValues;
	}

	/**
	 * Get a list of the average value of each attribute
	 * @param instnaces
	 * @return a list of average values
	 */
	static public ArrayList<Double> getAverageFromAllAttributes(Instances data){
		ArrayList<Double> averages = new ArrayList<Double>();

		for(int i=0; i<data.numAttributes();i++){
			if(data.classIndex()==i)
				continue;
			averages.add(data.attributeStats(i).numericStats.mean);
		}

		return averages;
	}

	/**
	 * Get a list of the standard deviation value of each attribute
	 * @param instnaces
	 * @return a list of standard deviation values
	 */
	static public ArrayList<Double> getSTDFromAllAttributes(Instances data){
		ArrayList<Double> std = new ArrayList<Double>();

		for(int i=0; i<data.numAttributes();i++){
			if(data.classIndex()==i)
				continue;
			std.add(data.attributeStats(i).numericStats.stdDev);
		}

		return std;
	}

	/**
	 * Get a balance value from pf and pd
	 * @param pd
	 * @param pf
	 * @return
	 */
	static public double getBalance(double pd, double pf){
		return 1 - ( Math.sqrt(Math.pow(1-pd,2)+Math.pow(pf,2)) / Math.sqrt(2) );
	}

	/**
	 * a method for n-fold cross validation
	 * @param strClassifier
	 * @param folds
	 * @param instances
	 * @param sourceLabelName
	 * @param randomSeed
	 * @return
	 * @throws Exception
	 */
	static public Evaluation nfoldCrossValidation(String strClassifier,
			int folds,
			Instances instances, String labelName, int randomSeed) throws Exception{

		Classifier classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
		instances.setClass(instances.attribute(labelName));

		// Perform n-fold cross-validation on source..
		Evaluation eval = new Evaluation(instances);

		eval.crossValidateModel(classifier, instances, folds, new Random(randomSeed));

		return eval;
	}

	/**
	 * To select and sort popular instances based on popular index (Kocaguneli et al., Active Learning and Effort Estimation: Finding the Essential Content of Software Effort Estimation Data@TSE2013)
	 * @param instances 
	 * @param size selection size
	 * @return
	 */
	static public Instances selectAndSortInsancesUsingPopIndex(Instances instances,int size){
		Instances selectedInstances =new Instances(instances,0);

		// compute distance matrix
		int numInstances = instances.numInstances();
		double[][] distanceMatrix = new double[numInstances][numInstances];
		EuclideanDistance distance = new EuclideanDistance(instances);

		for(int row=0;row<numInstances;row++){
			for(int col=0;col<numInstances;col++){
				distanceMatrix[row][col] = distance.distance(instances.get(row), instances.get(col));
			}
		}

		// compute Everyone's nearest neighbor, Enn matrix. Mark 1 for the NN otherwise 0
		double[][] ENNMatrix = new double[numInstances][numInstances];

		for(int row=1;row<numInstances ; row++){
			int minColIndex = 0;
			for(int col=0;col<numInstances-1;col++){
				if(row!=col && row!=col+1 && distanceMatrix[row][minColIndex]>distanceMatrix[row][col+1])
					minColIndex = col+1;
			}
			ENNMatrix[row][minColIndex]=1;
		}

		// compute popularity index
		HashMap<Integer,Integer> popIndex = new HashMap<Integer,Integer>();
		for(int col=0;col<numInstances;col++){
			int sumOfACol = 0;
			for(int row=0;row<numInstances;row++)
				sumOfACol+=ENNMatrix[row][col];
			popIndex.put(col, sumOfACol);
		}

		// sort instances by using popindex. The first one is the most popular one.
		Map<Integer, Integer> map = sortByComparator(popIndex);
		for(Map.Entry<Integer, Integer> entry: map.entrySet()){
			if(selectedInstances.numInstances()>=size)
				break;
			selectedInstances.add(instances.get(entry.getKey()));
		}

		return selectedInstances;
	}

	/**
	 * Generate new instances by shuffling each feature vector values
	 * @param instances
	 * @return new instances shuffled on each feature vector
	 */
	public static Instances randomizeByShufflingFeatureVector(Instances instances){

		Instances randomizedData = new Instances(instances);

		double[] attrValues = new double[instances.numInstances()];

		for(int i=0;i<instances.numInstances();i++){

			// get attribute values
			for(int a=0;a<instances.numAttributes()-1;a++){// ignore label attribute
				attrValues[a] = instances.instance(i).value(a);
			}

			shuffleArray(attrValues);

			for(int a=0;a<instances.numAttributes()-1;a++){// ignore label attribute
				randomizedData.instance(i).setValue(a, attrValues[a]);
			}

		}

		return randomizedData;
	}

	public static double harmonicMean(double[] data)
	{
		double sum = 0.0;

		for (int i = 0; i < data.length; i++)
		{
			sum += 1.0 / data[i];
		}

		return data.length / sum;
	}

	/**
	 * Implementing Fisher-Yates shuffle
	 * @param ar double array to be shuffled
	 */
	static void shuffleArray(double[] ar)
	{
		Random rnd = new Random();
		for (int i = ar.length - 1; i >= 0; i--)
		{
			int index = rnd.nextInt(i + 1);
			// Simple swap
			double a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	/**
	 * Load Instances from arff file. Last attribute will be set as class attribute
	 * @param path arff file path
	 * @return Instances
	 */
	public static Instances loadArff(String path){
		Instances instances=null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			instances = new Instances(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		instances.setClassIndex(instances.numAttributes()-1);

		return instances;
	}
	
	/**
	 * Load Instances from arff files in a certain directory. Last attribute will be set as class attribute
	 * @param path to the directory having arff files
	 * @param the arff path to be excluded
	 * @return HashMap of Instances
	 */
	static HashMap<String,Instances> listOfinstances=new HashMap<String,Instances>();
	public static HashMap<String,Instances> loadArffs(String dirPath,String exclude){
		
		
		File root = new File(dirPath);
        File[] list = root.listFiles();
		
		BufferedReader reader;
		
		for(File file:list){
		
			// only consider arff files
			if(file.isDirectory()){
				loadArffs(file.getPath(),exclude);
				continue;
			}
			
			if(!file.getName().toLowerCase().endsWith(".arff"))
				continue;
			
			if(file.getPath().toLowerCase().endsWith(exclude.toLowerCase()))
				continue;
						
			Instances instances=null;
			
			try {
				reader = new BufferedReader(new FileReader(file));
				instances = new Instances(reader);
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			instances.setClassIndex(instances.numAttributes()-1);
			
			listOfinstances.put(file.getName(),instances);
		}

		return listOfinstances;
	}

	/**
	 * Load Instances from arff file. Last attribute will be set as class attribute
	 * @param path arff file path
	 * @return Instances
	 */
	public static Instances loadArff(String path,String classAttributeName){
		Instances instances=null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			instances = new Instances(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		instances.setClassIndex(instances.attribute(classAttributeName).index());

		return instances;
	}

	/**
	 * normalize a target value by using min-max normalization (values are scaled in 0 to 1)
	 * @param min
	 * @param max
	 * @param value
	 * @return
	 */
	static public double minMaxNormalize(double min,double max,double value){
		return (value-min)/(max-min); 
	}

	/**
	 * sort Map by value (DESC)
	 * @param unsortMap
	 * @return
	 */
	private static Map<Integer,Integer> sortByComparator(Map<Integer,Integer> unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return -((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		// put sorted list into map again
		//LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	/**
	 * get values for a positive class
	 * @param instances
	 * @param attrIdx
	 * @param sourceLabelPos
	 * @param isForPosClass
	 * @return
	 */
	public static double[] getValuesByClassValue(Instances instances,
			int attrIdx, String sourceLabelPos, boolean isForPosClass) {

		ArrayList<Double> values = new ArrayList<Double>();

		int buggyClassIndex = getClassValueIndex(instances,sourceLabelPos);

		for(int instIdx=0;instIdx<instances.numInstances();instIdx++){
			double value = instances.instance(instIdx).value(attrIdx);
			double classValueIdx = instances.instance(instIdx).classValue();
			if(classValueIdx==buggyClassIndex && isForPosClass)
				values.add(value);

			if(classValueIdx!=buggyClassIndex && !isForPosClass)
				values.add(value);
		}

		return Doubles.toArray(values);
	}

	/**
	 * conduct simple cross prediction on the same split of n-fold cross validation of target within-prediction
	 * @param predictionInfo
	 * @param sourceInstances
	 * @param targetInstances
	 * @param posLabel
	 * @param repeat
	 * @param folds
	 * @param verbose
	 */
	public static void crossPredictionOnTheSameSplit(String predictionInfo,Instances sourceInstances,
			Instances targetInstances,String posLabel,int repeat,int folds, boolean verbose) {

		String mlAlgorithm = "weka.classifiers.functions.Logistic";

		try {
			Classifier classifierForCross = (Classifier) Utils.forName(Classifier.class, mlAlgorithm, null);
			Classifier classifierForTargetWithin = (Classifier) Utils.forName(Classifier.class, mlAlgorithm, null);
			classifierForCross.buildClassifier(sourceInstances);
			//Evaluation eval = new Evaluation(targetInstances);

			//eval.evaluateModel(classifier, targetInstances);

			int posClassIndex = WekaUtils.getClassValueIndex(sourceInstances, posLabel);

			Instances tarData = new Instances(targetInstances);

			Measures wMeasures = new Measures();
			Measures cMeasures = new Measures();

			DecimalFormat df = new DecimalFormat("0.0");
			String pcntNewInstances = df.format((double)(sourceInstances.numInstances()*100)/targetInstances.numInstances());

			for(int i=0; i<repeat; i++){
				// randomize with different seed for each iteration
				tarData.randomize(new Random(i)); 

				if (folds>1){
					tarData.stratify(folds);
				}

				Evaluation tarEval = new Evaluation(tarData);
				Evaluation crossEval = new Evaluation(sourceInstances);
				for(int n=0;n<folds;n++){
					Instances tarTrain = folds==1?tarData:tarData.trainCV(folds, n);
					Instances tarTest = folds==1?tarData:tarData.testCV(folds, n);

					Classifier clsCopy = AbstractClassifier.makeCopy(classifierForTargetWithin);
					clsCopy.buildClassifier(tarTrain);
					tarEval.evaluateModel(clsCopy, tarTest);

					Classifier clsCopyForCross = AbstractClassifier.makeCopy(classifierForCross);
					clsCopyForCross.buildClassifier(sourceInstances);
					crossEval.evaluateModel(classifierForCross, tarTest);

					wMeasures.getFmeasures().add(tarEval.fMeasure(posClassIndex));
					wMeasures.getAUCs().add(tarEval.areaUnderROC(posClassIndex));
					wMeasures.getMCCs().add(tarEval.matthewsCorrelationCoefficient(posClassIndex));
					wMeasures.getAUPRCs().add(tarEval.areaUnderPRC(posClassIndex));

					cMeasures.getFmeasures().add(crossEval.fMeasure(posClassIndex));
					cMeasures.getAUCs().add(crossEval.areaUnderROC(posClassIndex));
					cMeasures.getMCCs().add(crossEval.matthewsCorrelationCoefficient(posClassIndex));
					cMeasures.getAUPRCs().add(crossEval.areaUnderPRC(posClassIndex));

					if(verbose)
						System.out.println("D," + predictionInfo + "," +	 
								sourceInstances.numInstances() + "," + pcntNewInstances + "%," + 
								targetInstances.numInstances() + "," + 
								tarEval.fMeasure(posClassIndex) + "," + crossEval.fMeasure(posClassIndex) + ",-," +
								tarEval.areaUnderROC(posClassIndex) + "," + crossEval.areaUnderROC(posClassIndex) + ",-," +
								tarEval.matthewsCorrelationCoefficient(posClassIndex) + "," + crossEval.matthewsCorrelationCoefficient(posClassIndex) + ",-," +
								tarEval.areaUnderPRC(posClassIndex) + "," + crossEval.areaUnderPRC(posClassIndex) + ",-");
				}
			}

			System.out.println("A," + predictionInfo + "," +

											sourceInstances.numInstances() + "," + pcntNewInstances + "%," + 
											targetInstances.numInstances() + "," + 
											getWinTieLoss(wMeasures.getFmeasures(),cMeasures.getFmeasures()) + "," +
											getWinTieLoss(wMeasures.getAUCs(),cMeasures.getAUCs()) + "," +
											getWinTieLoss(wMeasures.getMCCs(),cMeasures.getMCCs()) + "," +
											getWinTieLoss(wMeasures.getAUPRCs(),cMeasures.getAUPRCs()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * conduct simple cross prediction on the same split of n-fold cross validation of target within-prediction
	 * @param predictionInfo
	 * @param sourceInstances
	 * @param targetInstances
	 * @param posLabel
	 * @param repeat
	 * @param folds
	 */
	public static void crossPredictionOnTheSameSplit(String predictionInfo,Instances sourceInstances,
			Instances targetInstances,String posLabel,int repeat,int folds) {

		crossPredictionOnTheSameSplit(predictionInfo,sourceInstances,
				targetInstances,posLabel,repeat,folds,true);

	}

	/**
	 * conduct Win/Tie/Loss by Wilcoxon Signed-rank test
	 * @param sample1
	 * @param sample2
	 * @return
	 */
	public static String getWinTieLoss(ArrayList<Double> sample1, ArrayList<Double> sample2){

		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();

		double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(sample1), Doubles.toArray(sample2),false);

		DescriptiveStatistics stat1 = new DescriptiveStatistics(Doubles.toArray(sample1));
		DescriptiveStatistics stat2 = new DescriptiveStatistics(Doubles.toArray(sample2));

		double sample1Median = stat1.getPercentile(50);
		double sample2Median = stat2.getPercentile(50);
		double sample1Mean = stat1.getMean();
		double sample2Mean = stat2.getMean();

		String wtlResult = "tie";

		if(sample1Median<sample2Median && p < 0.05)
			wtlResult = "win";
		else if(sample1Median>sample2Median && p < 0.05)
			wtlResult = "loss";

		return sample1Median + "," + sample2Median + "," + sample1Mean + "," + sample2Mean + "," + wtlResult;
	}

	/**
	 * Get average Fmeasure by various thresholds using ThresholdCurve
	 * @param predictions
	 * @param classValueIndex
	 * @return
	 */
	public static double getAvgFmeasureByVarThresholds(FastVector<NominalPrediction> predictions,
			int classValueIndex) {
		ThresholdCurve tc = new ThresholdCurve();
		Instances results = tc.getCurve(predictions,classValueIndex);

		return results.attributeStats(results.attribute("FMeasure").index()).numericStats.mean;
		//return results.attributeStats(results.attribute("FMeasure").index()).numericStats.max;
	}

	/**
	 * Calculates the performance stats for the desired class and return 
	 * results as a set of Instances.
	 *
	 * @param predictions the predictions to base the curve on
	 * @param classIndex index of the class of interest.
	 * @return datapoints as a set of instances.
	 */
	public static Instances getCurve(FastVector predictions, int classValueIndex) {

		ThresholdCurve tc = new ThresholdCurve();
		Instances results = tc.getCurve(predictions,classValueIndex);

		return results;
	}
	
	public static ArrayList<String> getPrecisionRecallFmeasureFromCurve(Instances curve){
		ArrayList<String> PRF = new ArrayList<String>();
		
		for(Instance instance:curve){
			String line = "";
			
			line =  instance.value(curve.attribute("True Positives")) + "";
			line =  line + "," + instance.value(curve.attribute("False Positives"));
			line =  line + "," + instance.value(curve.attribute("True Negatives"));
			line =  line + "," + instance.value(curve.attribute("False Negatives"));
			line =  line + "," + instance.value(curve.attribute("Precision"));
			line =  line + "," + instance.value(curve.attribute("Recall"));
			line =  line + "," + instance.value(curve.attribute("FMeasure"));
			line =  line + "," + instance.value(curve.attribute("Threshold"));
			
			
			PRF.add(line);
		}
		
		return PRF;
	}
	
	public static double getBestThresholdForFMeasure(Instances curve) {
		double bestThreshold = 0.0;
		double bestFMeasure = 0.0;
		
		for(Instance instance:curve){
			double curFMeasure = instance.value(curve.attribute("FMeasure"));
			
			if(curFMeasure>=bestFMeasure){
				bestFMeasure = curFMeasure;
				bestThreshold = instance.value(curve.attribute("Threshold"));
			}
			
		}
		return bestThreshold;
	}
	
	public static double getBestThresholdPositionForFMeasure(
			Instances curve) {
		
		double bestThresholdPosition = 0.0;
		double bestFMeasure = 0.0;
		
		for(int i=0;i<curve.numInstances()-1;i++){
			Instance instance = curve.get(i);
			double curFMeasure = instance.value(curve.attribute("FMeasure"));
			
			if(curFMeasure>=bestFMeasure){
				bestFMeasure = curFMeasure;
				bestThresholdPosition = (double)(i+1)/curve.numInstances();
			}
			
		}
		return bestThresholdPosition;
	}
	
	public static String getPredictionResultsByThreshold(
			ArrayList<String> precisionRecallFmeasureFromCurve,
			double bestThresholdForFMeasure) {
		
		for(String prf:precisionRecallFmeasureFromCurve){
			
			String[] values = prf.split(","); // TP, FN, FP, TN, P, R, F, Threshold
			Double threshold = Double.parseDouble(values[7]);
			
			if(threshold>=bestThresholdForFMeasure)
				return prf;
		}
		
		return "0,0,0,0,0,0,0,0";
	}
	
	public static String getPredictionResultsByThresholdPosition(
			ArrayList<String> precisionRecallFmeasureFromCurve,
			double bestThresholdPositionForFmeasure) {
		
		double position = precisionRecallFmeasureFromCurve.size() * bestThresholdPositionForFmeasure;
		
		int intPosition = (int)Math.round(position)-1;
		
		return precisionRecallFmeasureFromCurve.get(intPosition==-1?0:intPosition);
	}

	/**
	 * Compute precision using TP, FP, TN, and fN.
	 *
	 * @param True Positive
	 * @param False Positive
	 * @param True Negative
	 * @param False NEgative
	 * @return precision
	 */
	public static double getPrecision(int TP, int FP, int TN, int FN){
		return (double)TP/(TP+FP);

	}

	/**
	 * Compute recall using TP, FP, TN, and fN.
	 *
	 * @param True Positive
	 * @param False Positive
	 * @param True Negative
	 * @param False NEgative
	 * @return recall
	 */
	public static double getRecall(int TP, int FP, int TN, int FN){
		return (double)TP/(TP+FN);

	}

	/**
	 * Compute fmeasure using TP, FP, TN, and fN.
	 *
	 * @param True Positive
	 * @param False Positive
	 * @param True Negative
	 * @param False NEgative
	 * @return f-measure
	 */
	public static double getFmeasure(int TP, int FP, int TN, int FN){
		double precision = getPrecision(TP,FP,TN,FN);
		double recall = getRecall(TP,FP,TN,FN);
		return (double)2*(precision*recall)/(double)(precision+recall);
	}

	/**
	 * Compute recall using TP, FP, TN, and fN.
	 *
	 * @param True Positive
	 * @param False Positive
	 * @param True Negative
	 * @param False NEgative
	 * @return recall
	 */
	public static double getFalsePositiveRate(int TP, int FP, int TN, int FN){
		return (double)FP/(FP+TN);

	}

	/**
	 * Compute recall using TP, FP, TN, and fN.
	 *
	 * @param True Positive
	 * @param False Positive
	 * @param True Negative
	 * @param False NEgative
	 * @return recall
	 */
	public static double getFalseNegativeRate(int TP, int FP, int TN, int FN){
		return (double)FN/(FN+TP);

	}


	/**
	 * 
	 * @param predictions the predictions to use
	 * @param classIndex the class index
	 * @return the probabilities
	 */
	static private double [] getProbabilities(FastVector predictions, int classIndex) {

		// sort by predicted probability of the desired class.
		double [] probs = new double [predictions.size()];
		for (int i = 0; i < probs.length; i++) {
			NominalPrediction pred = (NominalPrediction)predictions.elementAt(i);
			probs[i] = pred.distribution()[classIndex];
		}
		return probs;
	}

	/**
	 * generates the header
	 * 
	 * @return the header
	 */
	static private Instances makeHeader() {

		FastVector fv = new FastVector();
		fv.addElement(new Attribute(TRUE_POS_NAME));
		fv.addElement(new Attribute(FALSE_NEG_NAME));
		fv.addElement(new Attribute(FALSE_POS_NAME));
		fv.addElement(new Attribute(TRUE_NEG_NAME));
		fv.addElement(new Attribute(FP_RATE_NAME));
		fv.addElement(new Attribute(TP_RATE_NAME));
		fv.addElement(new Attribute(PRECISION_NAME));
		fv.addElement(new Attribute(RECALL_NAME));
		fv.addElement(new Attribute(FALLOUT_NAME));
		fv.addElement(new Attribute(FMEASURE_NAME));
		fv.addElement(new Attribute(SAMPLE_SIZE_NAME));
		fv.addElement(new Attribute(LIFT_NAME));
		fv.addElement(new Attribute(THRESHOLD_NAME));      
		return new Instances(RELATION_NAME, fv, 100);
	}

	/**
	 * generates an instance out of the given data
	 * 
	 * @param tc the statistics
	 * @param prob the probability
	 * @return the generated instance
	 */
	static private Instance makeInstance(TwoClassStats tc, double prob) {

		int count = 0;
		double [] vals = new double[13];
		vals[count++] = tc.getTruePositive();
		vals[count++] = tc.getFalseNegative();
		vals[count++] = tc.getFalsePositive();
		vals[count++] = tc.getTrueNegative();
		vals[count++] = tc.getFalsePositiveRate();
		vals[count++] = tc.getTruePositiveRate();
		vals[count++] = tc.getPrecision();
		vals[count++] = tc.getRecall();
		vals[count++] = tc.getFallout();
		vals[count++] = tc.getFMeasure();
		double ss = (tc.getTruePositive() + tc.getFalsePositive()) / 
				(tc.getTruePositive() + tc.getFalsePositive() + tc.getTrueNegative() + tc.getFalseNegative());
		vals[count++] = ss;
		double expectedByChance = (ss * (tc.getTruePositive() + tc.getFalseNegative()));
		if (expectedByChance < 1) {
			vals[count++] = Utils.missingValue();
		} else {
			vals[count++] = tc.getTruePositive() / expectedByChance; 

		}
		vals[count++] = prob;
		return new DenseInstance(1.0, vals);
	}

	/** The name of the relation used in threshold curve datasets */
	public static final String RELATION_NAME = "ThresholdCurve";

	/** attribute name: True Positives */
	public static final String TRUE_POS_NAME  = "True Positives";
	/** attribute name: False Negatives */
	public static final String FALSE_NEG_NAME = "False Negatives";
	/** attribute name: False Positives */
	public static final String FALSE_POS_NAME = "False Positives";
	/** attribute name: True Negatives */
	public static final String TRUE_NEG_NAME  = "True Negatives";
	/** attribute name: False Positive Rate" */
	public static final String FP_RATE_NAME   = "False Positive Rate";
	/** attribute name: True Positive Rate */
	public static final String TP_RATE_NAME   = "True Positive Rate";
	/** attribute name: Precision */
	public static final String PRECISION_NAME = "Precision";
	/** attribute name: Recall */
	public static final String RECALL_NAME    = "Recall";
	/** attribute name: Fallout */
	public static final String FALLOUT_NAME   = "Fallout";
	/** attribute name: FMeasure */
	public static final String FMEASURE_NAME  = "FMeasure";
	/** attribute name: Sample Size */
	public static final String SAMPLE_SIZE_NAME = "Sample Size";
	/** attribute name: Lift */
	public static final String LIFT_NAME = "Lift";
	/** attribute name: Threshold */
	public static final String THRESHOLD_NAME = "Threshold";


}

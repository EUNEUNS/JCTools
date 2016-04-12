package net.lifove.research.utils;

import java.io.File;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class SingleFeatureTransformer{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SingleFeatureTransformer().run(args);
	}

	private void run(String[] args) {
		String dataPath = args[0];
		String dirPath = dataPath.substring(0,dataPath.lastIndexOf(File.separator));
		String fileName =  args[1];
		String posLabel = args[2];
		
		Instances instances = WekaUtils.loadArff(dataPath);
		
		int numOfFeatures = instances.numAttributes()-1; //excludig label
		int numOfNewFeatures = numOfFeatures*9; // (1 Original + 8 arithmetic expressions)*numOfFeatures
		
		ArrayList<Attribute> newFeatures = WekaUtils.createAttributeInfoForClassfication(numOfNewFeatures + 1); // 1: including label
		int labelIndexForNewFeatures = numOfNewFeatures;
		
		Instances transformedData = new Instances(fileName + "_associated",newFeatures,0);
		
		double[][] values = new double[instances.numInstances()][newFeatures.size()];
		
		// add original data to values array
		for(int i=0;i<instances.numAttributes()-1;i++){
			for(int idxInstance=0;idxInstance<instances.numInstances();idxInstance++)
				values[idxInstance][i] = instances.instance(idxInstance).value(i);
		}
		
		// compute new values
		String[] expressions = {"square", "root", "log", "cos", "tan", "sin", "ceil", "floor"};
		for(int idxInstance=0;idxInstance<instances.numInstances();idxInstance++){
			for(int i=numOfFeatures;i<newFeatures.size()-1;i++){
				for(int attrIdx = 0; attrIdx < instances.numAttributes()-1;attrIdx++){
					for(String expression:expressions){
						values[idxInstance][i] = computeNewValue(instances.instance(idxInstance).value(attrIdx),expression);
						i++;
					}
				}
			}
		}
		
		// assign label values
		for(int idxInstance=0;idxInstance<instances.numInstances();idxInstance++){
			if(instances.get(idxInstance).classValue()==WekaUtils.getClassValueIndex(instances, posLabel))
				values[idxInstance][labelIndexForNewFeatures] = WekaUtils.dblPosValue;
			else
				values[idxInstance][labelIndexForNewFeatures] = WekaUtils.dblNegValue;
		}
		
		// add instances
		for(int i=0;i<values.length;i++){
			DenseInstance newInstance = new DenseInstance(1,values[i]);
			transformedData.add(newInstance);
		}
		
		WekaUtils.writeADataFile(transformedData, dirPath + File.separator + fileName + "_SFT.arff");
	}

	private double computeNewValue(double value, String expression) {
		
		//square root log exp cos tan sin ceil floor
		if(expression.equals("square")){
			return value*value;
		}
		if(expression.equals("root")){
			return Math.sqrt(value);
		}
		if(expression.equals("log")){
			double newValue = value;
			if(value==0)
				newValue = 0.000001; // use samall number when value is 0.
			return Math.log(newValue);
		}
		if(expression.equals("exp")){
			double exp = Math.exp(value);
			return Double.isInfinite(exp)?Double.MAX_VALUE:exp;
		}
		if(expression.equals("cos")){
			return Math.cos(value);
		}
		if(expression.equals("tan")){
			return Math.tan(value);
		}
		if(expression.equals("sin")){
			return Math.sin(value);
		}
		if(expression.equals("ceil")){
			return Math.ceil(value);
		}
		if(expression.equals("floor")){
			return Math.floor(value);
		}

		return Double.NaN;
	}
}

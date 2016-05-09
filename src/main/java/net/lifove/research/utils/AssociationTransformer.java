package net.lifove.research.utils;

import java.io.File;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class AssociationTransformer {
	final int numExpressions = 8;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AssociationTransformer().run(args);
	}

	private void run(String[] args) {
		String dataPath = args[0];
		String dirPath = dataPath.substring(0,dataPath.lastIndexOf(File.separator));
		String fileName =  args[1];
		String posLabel = args[2];
		
		//String operator = args[3];
		
		Instances instances = WekaUtils.loadArff(dataPath);
		int numOfInstances = instances.numInstances();
		
		System.out.println("Arff loaded");
		
		int numOfFeatures = instances.numAttributes()-1; // excluding label
		// # original = #AllTransformedFeatures/9
		int numOriginal = numOfFeatures/(numExpressions+1);
		// num orginal +num orginal *(num orginal-1)
		long numOfAssociatedFeatures = 6*(numOfFeatures)*(numOfFeatures-1-8)/2; // six expressions
		
		System.out.println("numOfAssociatedFeatures=" + numOfAssociatedFeatures);
		
		ArrayList<Attribute> features = WekaUtils.createAttributeInfoForClassfication(numOfAssociatedFeatures + 1); // 1: including label
		int numAssociatedFeaturesInclLabel = features.size();
		System.out.println("attributes created!");
		
		Instances transformedData = new Instances(fileName + "_associated",features,0);
		
		
		double[][] values = new double[numOfInstances][features.size()];
		
		System.out.println("value array initiated!");
		
		
		
		// assign new feature values to array
		int srcCurSection = 0;
		int tarCurSection = 0;
		int newFeatureIndex = 0;
		for(int idxInstance=0;idxInstance<numOfInstances;idxInstance++){
			newFeatureIndex=0;
			for(int srcIndex=0;srcIndex<numOfFeatures-1;srcIndex++){
				if (srcIndex<numOriginal)
					srcCurSection = 0;
				else
					srcCurSection = (int)Math.ceil(((srcIndex+1)-numOriginal)/(double)numExpressions);
				
				for(int tarIndex=srcIndex+1;tarIndex<numOfFeatures;tarIndex++){
					
					if (tarIndex<numOriginal)
						tarCurSection = 0;
					else
					tarCurSection = (int)Math.ceil(((tarIndex+1)-numOriginal)/(double)numExpressions);
					
					if(srcIndex==tarIndex)
						continue;
					
					if(tarIndex >= numOriginal && (srcIndex == (tarCurSection-1) ||  (srcCurSection==tarCurSection)))
						continue;
								
					// new values using six expressions A+B,A-B,B-A,A*B,A/B,B/A
					double srcValue = instances.get(idxInstance).value(srcIndex);
					double tarValue = instances.get(idxInstance).value(tarIndex);
					values[idxInstance][newFeatureIndex] = srcValue + tarValue;
					newFeatureIndex++;
					values[idxInstance][newFeatureIndex] = srcValue - tarValue;
					newFeatureIndex++;
					values[idxInstance][newFeatureIndex] = tarValue - srcValue;
					newFeatureIndex++;
					values[idxInstance][newFeatureIndex] = srcValue * tarValue;
					newFeatureIndex++;
					values[idxInstance][newFeatureIndex] = tarValue==0?srcValue/0.000001:srcValue / tarValue;
					newFeatureIndex++;
					values[idxInstance][newFeatureIndex] = srcValue==0?tarValue/0.000001:tarValue / srcValue;
					newFeatureIndex++;		
				}
			}
		}
		//System.out.println(newFeatureIndex);
		//System.exit(0);
		
		// assign label values
		for(int idxInstance=0;idxInstance<numOfInstances;idxInstance++){
			if(instances.get(idxInstance).classValue()==WekaUtils.getClassValueIndex(instances, posLabel))
				values[idxInstance][newFeatureIndex] = WekaUtils.dblPosValue;
			else
				values[idxInstance][newFeatureIndex] = WekaUtils.dblNegValue;
		}
		
		String strArffStructure = transformedData.toString();
		
		// add instances
		for(int i=0;i<numOfInstances;i++){
			DenseInstance newInstance = new DenseInstance(1,values[i]);
			transformedData.add(newInstance);
		}
	
		values = null;	
			
		ArrayList<String> lines = new ArrayList<String>();
		
		System.out.println(transformedData.relationName());
		lines.add(strArffStructure.trim());
		
		//generate arrayList
		for(int i=0;i<transformedData.numInstances();i++){
			lines.add(transformedData.get(i).toString());
		}
		
		FileUtil.writeAFile(lines, dirPath + File.separator + fileName + "_associated.arff");
		
		//WekaUtils.writeADataFile(transformedData, dirPath + File.separator + fileName + "_associated.arff");
	}
}

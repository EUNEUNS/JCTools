package net.lifove.research.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class AssociationSubsetTransformer {
	final int numExpressions = 8;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AssociationSubsetTransformer().run(args);
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
		//long numOfAssociatedFeaturesInASubset = (numOfAssociatedFeatures-numOriginal)/numExpressions + numOriginal;
		long numOfAssociatedFeaturesInASubset = 6*((numOfFeatures-numOriginal)/numExpressions+numOriginal)
													*((numOfFeatures-numOriginal)/numExpressions+numOriginal-2)/2;
		
		for(int idxNewData=0;idxNewData<numExpressions;idxNewData++){
		
			System.out.println("numOfAssociatedFeaturesInASubset=" + numOfAssociatedFeaturesInASubset);
			ArrayList<Attribute> features = WekaUtils.createAttributeInfoForClassfication(numOfAssociatedFeaturesInASubset + 1); // 1: including label
			System.out.println("attributes created!");
			
			Instances transformedData = new Instances(fileName + "_associated_subset_" + idxNewData,features,0);
			
			
			// file create!
			try {
				File file= new File(dirPath + File.separator + fileName + "_associated_subset_" + idxNewData + ".arff");
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos=new DataOutputStream(fos);
				
				// write dataset structure
				dos.write((transformedData.toString()+"\n").getBytes());
				
				//double[][] values = new double[numOfInstances][features.size()];
				
				System.out.println("value array initiated!");
				
				// assign new feature values to array
				int srcCurSection = 0;
				int tarCurSection = 0;
				//int newFeatureIndex = 0;
				for(int idxInstance=0;idxInstance<numOfInstances;idxInstance++){
					//newFeatureIndex=0;
					//String newInstance = "";
					for(int srcIndex=0;srcIndex<numOfFeatures-1;srcIndex++){
						if (srcIndex<numOriginal)
							srcCurSection = 0;
						else
							srcCurSection = (int)Math.ceil(((srcIndex+1)-numOriginal)/(double)numExpressions);
						
						if (srcCurSection>0 && (srcIndex-numOriginal)%numExpressions!=idxNewData)
							continue;
						
						for(int tarIndex=srcIndex+1;tarIndex<numOfFeatures;tarIndex++){
							
							if (tarIndex<numOriginal)
								tarCurSection = 0;
							else
								tarCurSection = (int)Math.ceil(((tarIndex+1)-numOriginal)/(double)numExpressions);
							
							if(srcIndex==tarIndex)
								continue;
							
							if(tarIndex >= numOriginal && (srcIndex == (tarCurSection-1) ||  (srcCurSection==tarCurSection)))
								continue;
							
							// check the target index is related to the expression index.
							// if not, skip
							if(tarIndex>=numOriginal && (tarIndex-numOriginal)%numExpressions!=idxNewData)
								continue;
								
							// new values using six expressions A+B,A-B,B-A,A*B,A/B,B/A
							double srcValue = instances.get(idxInstance).value(srcIndex);
							double tarValue = instances.get(idxInstance).value(tarIndex);
							dos.write(((srcValue + tarValue) + ",").getBytes());
							dos.write(((srcValue - tarValue) + ",").getBytes());
							dos.write(((tarValue - srcValue) + ",").getBytes());
							dos.write((( srcValue * tarValue) + ",").getBytes());
							dos.write(((tarValue==0?srcValue/0.000001:srcValue / tarValue) + ",").getBytes());
							dos.write(((srcValue==0?tarValue/0.000001:tarValue / srcValue) + ",").getBytes());
						}
					}
					
					if(instances.get(idxInstance).classValue()==WekaUtils.getClassValueIndex(instances, posLabel))
						dos.write((WekaUtils.strPos).getBytes());
					else
						dos.write((WekaUtils.strNeg).getBytes());
					
					dos.write(("\n").getBytes());
				}
				dos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}

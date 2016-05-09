package net.lifove.research.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveRange;

public class InstanceFilteringBasedPrediction {

	/*public enum FilterType {
		TOP_N, MUST5P,MUST10P,MUST15P
	}*/
	
	int repeat = 500;
	int folds = 2;
	
	public static void main(String[] args) {
		new InstanceFilteringBasedPrediction().run(args);

	}

	private void run(String[] args) {
		// load parameters
		String dataPath = args[0];
		String groupName = args[1];
		String projectName = args[2];
		String classAttributeName = args[3];
		String strPosLabel = args[4];
		
		
		// load instances
		Instances instances = WekaUtils.loadArff(dataPath, classAttributeName);
		
		// apply feature selection first
		/*String[] cblArgs = {dataPath,groupName,projectName,classAttributeName,strPosLabel,"4","4","-1","false",
				"false","MEDIAN","true","true"};
		ClusteringBasedLearning CBL = new ClusteringBasedLearning();
		CBL.run(cblArgs);
		String selectedAttributeIndices = CBL.getSelectedAttributeIndices();
		*/
		String selectedAttributeIndices = WekaUtils.getSelectedAttributesByCorrelationAnalysis(instances);
		
		Instances originalInstances = new Instances(instances);
		instances = WekaUtils.getInstancesByRemovingSpecificAttributes(instances,
				selectedAttributeIndices, true);
		
		// compute n if the filter type is top n%
		//if(FilterType.valueOf(filterType) != FilterType.TOP_N){
		int	n = (int)(instances.numInstances()*0.5);
		//}
		
		// generate HashMap with a key (instance index) and an attribute value for each attribute
		ArrayList<HashMap<Integer,Double>> alstAttributeValues = new ArrayList<HashMap<Integer,Double>>();
		for(int attrIdx=0; attrIdx < instances.numAttributes(); attrIdx++){
			
			HashMap<Integer,Double> attributeValues = new HashMap<Integer,Double>();
			alstAttributeValues.add(attributeValues);
			
			if(attrIdx==instances.classIndex())
				continue;
			
			for(int instIdx=0;instIdx<instances.numInstances();instIdx++){
				attributeValues.put(instIdx, instances.get(instIdx).value(attrIdx));
			}
		}
		
		// sort attribute values in HashMap and find top n-th value and bottom n-th value
		ArrayList<Double> topNthValues = new ArrayList<Double>();
		ArrayList<Double> bottomNthValues = new ArrayList<Double>();
		for(int attrIdx=0;attrIdx<alstAttributeValues.size();attrIdx++){
			
			// skip class attribute
			if(alstAttributeValues.get(attrIdx).size()==0)
				continue;
			
			List<Integer> sortedIndice = ArrayListUtil.sortByValue(alstAttributeValues.get(attrIdx));
			
			Double topCutValue = alstAttributeValues.get(attrIdx).get(sortedIndice.get(n-1));
			Double bottomCutValue = alstAttributeValues.get(attrIdx).get(sortedIndice.get(instances.numInstances()-n));

			topNthValues.add(topCutValue);
			bottomNthValues.add(bottomCutValue);	
		}
		
		// generate indices for top n and bottom n instances
		ArrayList<ArrayList<Integer>> alstTopInstanceIndices = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> alstBottomInstanceIndices = new ArrayList<ArrayList<Integer>>();
		for(int attrIdx=0;attrIdx<alstAttributeValues.size();attrIdx++){
			
			ArrayList<Integer> topInstanceIndices = new ArrayList<Integer>();
			ArrayList<Integer> bottomInstanceIndices = new ArrayList<Integer>();
			
			alstTopInstanceIndices.add(topInstanceIndices);
			alstBottomInstanceIndices.add(bottomInstanceIndices);
			
			// skip class attribute
			if(alstAttributeValues.get(attrIdx).size()==0)
				continue;
			
			// the number of instance may not be same when there are many instances that have the same feature values
			for(int instIdx=0;instIdx<instances.numInstances();instIdx++){
				Double value = instances.get(instIdx).value(attrIdx);
				if(value >= topNthValues.get(attrIdx))
					topInstanceIndices.add(instIdx);
				
				if(value <= bottomNthValues.get(attrIdx))
					bottomInstanceIndices.add(instIdx);
			}
		}
		
		// find common instances among top or bottom instances
		ArrayList<Integer> topCommonIndices = getCommonIndices(alstTopInstanceIndices);
		ArrayList<Integer> bottomCommonIndices = getCommonIndices(alstBottomInstanceIndices);
		
		// remove common instance indices in both top and bottom indices.
		removeCommonIndices(topCommonIndices,bottomCommonIndices);
		
		// generate training set
		Instances trainingInstances = null;
		
		RemoveRange instFilter = new RemoveRange();
		RemoveRange instFilter2 = new RemoveRange();
		instFilter.setInstancesIndices(ArrayListUtil.getCommaSeperatedString(topCommonIndices,1));
		instFilter.setInvertSelection(true);
		instFilter2.setInstancesIndices(ArrayListUtil.getCommaSeperatedString(bottomCommonIndices,1));
		instFilter2.setInvertSelection(true);
		try {
			instFilter.setInputFormat(instances);
			Instances newPosInstances = Filter.useFilter(instances, instFilter);

			for(int instIdx=0;instIdx<newPosInstances.numInstances();instIdx++){
				newPosInstances.get(instIdx).setClassValue(strPosLabel);
			}
			
			instFilter2.setInputFormat(instances);
			Instances newNegInstances = Filter.useFilter(instances, instFilter2);
			for(int instIdx=0;instIdx<newNegInstances.numInstances();instIdx++){
				newNegInstances.get(instIdx).setClassValue(WekaUtils.getNegClassStringValue(newNegInstances, instances.classAttribute().name(), strPosLabel));
			}
		
			trainingInstances = new Instances(newPosInstances);
			
			for(int instIdx=0;instIdx<newNegInstances.numInstances();instIdx++){
				trainingInstances.add(newNegInstances.get(instIdx));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(groupName + "," + projectName + "," + SimpleCrossPredictor.crossPredictionOnTheSameSplit(trainingInstances, instances, originalInstances, strPosLabel, repeat, folds));
	}

	private void removeCommonIndices(ArrayList<Integer> topCommonIndices,
			ArrayList<Integer> bottomCommonIndices) {
		
		ArrayList<Integer> commonIndices = new ArrayList<Integer>();
		
		for(int topIdx=0;topIdx<topCommonIndices.size();topIdx++){
			if(bottomCommonIndices.contains(topCommonIndices.get(topIdx)))
				commonIndices.add(topCommonIndices.get(topIdx));
		}

		for(Integer value:commonIndices){
			topCommonIndices.remove(value);
			bottomCommonIndices.remove(value);
		}
		
	}

	private ArrayList<Integer> getCommonIndices(
			ArrayList<ArrayList<Integer>> alstTopInstanceIndices) {
		/*for(int i=0;i<alstTopInstanceIndices.size();i++){
			System.out.println("Attribute " + i);
			for(Integer value:alstTopInstanceIndices.get(i)){
				System.out.print(value + "-");
			}
			System.out.println();
		}*/
		
		// frequency of each instance in alstTopInstanceIndices
		HashMap<Integer,Integer> instanceFrequency = new HashMap<Integer,Integer>(); // key = instance index, value = frequency
		for(int i=0;i<alstTopInstanceIndices.size();i++){ // i is an attribute index
			for(Integer instanceIndex:alstTopInstanceIndices.get(i)){
				if(!instanceFrequency.containsKey(instanceIndex))
					instanceFrequency.put(instanceIndex, 1);
				else
					instanceFrequency.put(instanceIndex, instanceFrequency.get(instanceIndex)+1);
			}
		}
		
		//for(Integer key:instanceFrequency.keySet())
		//	System.out.println(key + ": " + instanceFrequency.get(key));
		
		
		ArrayList<Integer> firstIndices = null;
		for(int attrIdx=0;attrIdx<alstTopInstanceIndices.size();attrIdx++){
			if(alstTopInstanceIndices.get(attrIdx).size()==0)
				continue;
			
			firstIndices = alstTopInstanceIndices.get(attrIdx);
			break;
		}
		
		for(int attrIdx=0;attrIdx<alstTopInstanceIndices.size();attrIdx++){
			if(alstTopInstanceIndices.get(attrIdx).size()==0)
				continue;
			
			for(int i=0;i<firstIndices.size();i++)
				if(!alstTopInstanceIndices.get(attrIdx).contains(firstIndices.get(i)))
					firstIndices.remove(firstIndices.get(i));
		}
		return firstIndices;
	}
}

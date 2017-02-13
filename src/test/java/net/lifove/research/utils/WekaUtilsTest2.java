package net.lifove.research.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import net.lifove.research.utils.WekaUtils;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.Instances;

public class WekaUtilsTest2 {

	@Test
	public void testMergeArffWithNewFeatures(){
		
		for(int i=3; i<8;i++){
			AddManualFeatures("" + i, "train");
			AddManualFeatures("" + i, "test");
		}
		
	}

	private void AddManualFeatures(String fold, String trainOrTest) {
		String path = System.getProperty("user.home")
				+ "/Documents/ODP/projects/lucene/Song_token/arffsManuallyFilteredWOTestCases/" + fold + "/token_input/" + trainOrTest + "-fold-" + fold + "-.arff";
		Instances instances = WekaUtils.loadArff(path, "buggy");
		ArrayList<String> strNewFeatures = FileUtil.getLines(System.getProperty("user.home") + "/Documents/ODP/projects/lucene/semanticFeatures.txt", true);
		
		HashMap<String,ArrayList<Double>> featureValues = getFeatureValues(strNewFeatures);
		
		Instances newData = null;
		
		newData = new Instances(instances);
        // add new attributes
        // 1. nominal
        //FastVector values = new FastVector(); /* FastVector is now deprecated. Users can use any java.util.List */
        //values.addElement("A");               /* implementation now */
        //values.addElement("B");
        //values.addElement("C");
        //values.addElement("D");*/
        //newData.insertAttributeAt(new Attribute("NewNominal", values), newData.numAttributes());
        // 2. numeric
        newData.insertAttributeAt(new Attribute("useMathFloorAndLog"), newData.numAttributes()-1);
        newData.insertAttributeAt(new Attribute("initObjectBeforeTryBlock"), newData.numAttributes()-1);
        newData.insertAttributeAt(new Attribute("notUsingEqualsForObjectComparison"), newData.numAttributes()-1);
        
        // set values
        for (int i = 0; i < newData.numInstances(); i++) {
            // 1. nominal
            // index of labels A:0,B:1,C:2,D:3
            //newData.instance(i).setValue(newData.numAttributes() - 2, rand.nextInt(4));
        	
        	String change_id = "" + (int)newData.instance(i).value(newData.attribute("changeid"));
        	ArrayList<Double> values = featureValues.get(change_id);
        	
        	if(values!=null){
	            newData.instance(i).setValue(newData.attribute("useMathFloorAndLog"), values.get(0));
	            newData.instance(i).setValue(newData.attribute("initObjectBeforeTryBlock"), values.get(1));
	            newData.instance(i).setValue(newData.attribute("notUsingEqualsForObjectComparison"), values.get(2));
        	}else{
        		newData.instance(i).setValue(newData.attribute("useMathFloorAndLog"), Double.NaN);
	            newData.instance(i).setValue(newData.attribute("initObjectBeforeTryBlock"), Double.NaN);
	            newData.instance(i).setValue(newData.attribute("notUsingEqualsForObjectComparison"),Double.NaN);
        	}
            
          }
        
       FileUtil.writeAFile(newData.toString(), System.getProperty("user.home") + "/Documents/ODP/projects/lucene/Song_token/" + trainOrTest + "_" + fold + ".arff");
	}

	private HashMap<String, ArrayList<Double>> getFeatureValues(ArrayList<String> lines) {
		HashMap<String,ArrayList<Double>> featureValues = new HashMap<String,ArrayList<Double>>();
		for(String line:lines){
			String[] split = line.split(",");
			ArrayList<Double> values = new ArrayList<Double>();
			Double value1 = Double.parseDouble(split[1]);
			Double value2 = Double.parseDouble(split[2]);
			Double value3 = Double.parseDouble(split[3]);
			
			values.add(value1);
			values.add(value2);
			values.add(value3);
			
			featureValues.put(split[0], values);
			
		}
		return featureValues;
	}

}

package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.WekaUtils;

import org.junit.Test;

import weka.core.Instances;

public class CrossPredictionTest {
	
	@Test
	public void testCrossPrediciton(){
		//Instances srcInstances = WekaUtils.loadArff("../../Documents/temp/ODP/arffsManuallyCleaned/7/train.arff");
		//Instances tarInstances = WekaUtils.loadArff("../../Documents/temp/ODP/arffsManuallyCleaned/7/test.arff");
		
		Instances srcInstances = WekaUtils.loadArff("/Users/JC/Documents/HKUST/Research/CDDP/workspace/CrossPredictionSimulator/data/AEEEM/EQ.arff");
		Instances tarInstances = WekaUtils.loadArff("/Users/JC/Documents/HKUST/Research/CDDP/workspace/CrossPredictionSimulator/data/AEEEM/JDT.arff");
		
		Measure m = SimpleCrossPredictor.crossPrediction(srcInstances,tarInstances,"weka.classifiers.functions.Logistic","buggy");
		System.out.println(m.AUC);
		m = SimpleCrossPredictor.crossPrediction(srcInstances,tarInstances,"weka.classifiers.functions.Logistic","clean");
		System.out.println(m.AUC);
		m = SimpleCrossPredictor.crossPrediction(srcInstances,tarInstances,"weka.classifiers.trees.J48","clean");
		System.out.println(m.AUC);
		m = SimpleCrossPredictor.crossPrediction(srcInstances,tarInstances,"weka.classifiers.trees.J48","buggy");
		System.out.println(m.AUC);
	}
}

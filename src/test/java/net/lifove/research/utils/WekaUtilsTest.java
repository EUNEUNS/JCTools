package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.WekaUtils;

import org.junit.Test;

import weka.core.Instances;

public class WekaUtilsTest {

	//@Test
	public void testFeatrueSelectionByCfsSubsetEval() {
		Instances instances = WekaUtils.loadArff("data/AEEEM/EQ.arff");
		instances = WekaUtils.featrueSelectionByCfsSubsetEval(instances);
		System.out.println(instances);
		assertEquals("# selected features are not correct!",instances.numAttributes(),13,0);
	}
	
	//@Test
	public void testPredictionResultsFromEnsemble(){
		WekaUtils.predictionResultsFromEnsemble("promise,reskator","data/ensemble/detailed_mw1_redaktor.txt","data/ensemble/promise_redaktor_detailed.txt",WekaUtils.strPos);
	}
	
	@Test
	public void testGenerateRDataForBeanPlotsFromArff(){
		//WekaUtils.generateRSciprtsForBeanPlotsFromArff("data/gene/lucene_norm.arff","data/gene/lucene2.csv","class","buggy");
		/*WekaUtils.generateRSciprtsForBeanPlotsFromArff("data/gene/jackrabbit_norm.arff","data/gene/jackrabbit2.csv","class","buggy");
		WekaUtils.generateRSciprtsForBeanPlotsFromArff("data/gene/httpclient_norm.arff","data/gene/httpclient2.csv","class","buggy");
		WekaUtils.generateRSciprtsForBeanPlotsFromArff("data/gene/rhino_norm.arff","data/gene/rhino2.csv","class","buggy");*/
		//WekaUtils.generateRDataForBeanPlotsFromArff("data/Relink/Safe_norm.arff","data/Relink/Safe2.csv","isDefective","TRUE");
		WekaUtils.generateRDataForBeanPlotsFromArff("data/Relink/Zxing_norm.arff","data/Relink/Zxing2.csv","isDefective","TRUE");
		//WekaUtils.generateRDataForBeanPlotsFromArff("data/Relink/Apache_norm.arff","data/Relink/Apache2.csv","isDefective","TRUE");
	}

}

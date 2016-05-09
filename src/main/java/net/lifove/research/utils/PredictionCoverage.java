package net.lifove.research.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.csvreader.CsvReader;

public class PredictionCoverage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PredictionCoverage().run();
	}

	public void run(){
		
		ArrayList<PredictionCombination> preComb = new ArrayList<PredictionCombination>();
		TreeMap<String,ArrayList<Double>> lstTargetCrossF = new TreeMap<String,ArrayList<Double>>();
		TreeMap<String,ArrayList<Double>> lstTargetWithinF = new TreeMap<String,ArrayList<Double>>();
		
		ArrayList<Double> lstCrossF = new ArrayList<Double>();
		ArrayList<Double> lstWithinF = new ArrayList<Double>();
		
		CsvReader prediction;
		try {
			prediction = new CsvReader("data/20131128_analysis.csv");
		
			prediction.readHeaders();
			
			while (prediction.readRecord()){
				
				String predCombi = prediction.get(0);

				String source = predCombi.substring(0, predCombi.indexOf(">>"));
				String target = predCombi.substring(predCombi.indexOf(">>")+2, predCombi.length());
				String numTarget = prediction.get(2);
				String buggyRate = prediction.get(7);
				String srcTarRate = prediction.get(5);
				String algorithm = prediction.get(12);
				String withinF = prediction.get(10);
				String crossF = prediction.get(11);
				
				preComb.add(new PredictionCombination(source,target,numTarget,buggyRate,srcTarRate,algorithm,withinF,crossF));
				
				lstTargetCrossF.put(algorithm + "," + target, new ArrayList<Double>());
				lstTargetWithinF.put(algorithm + "," + target, new ArrayList<Double>());
			}
			
			for(PredictionCombination pc:preComb){
				//if(pc.buggyRate > 0.3 && pc.buggyRate < 0.5 && pc.srcTarRate >= 4){
				//if(pc.buggyRate > 0.2 && pc.srcTarRate >= 2){
				//if(pc.buggyRate > 0.3 && pc.srcTarRate >= 2){
				//if(pc.srcTarRate >= 1.5){
				if(pc.buggyRate > 0.3 &&  pc.buggyRate < 0.5 && pc.srcTarRate >= 4){
				//if(pc.srcTarRate >= 1.5 && pc.buggyRate <= 0.5){
					String key = pc.algorithm + "," + pc.target;
					lstTargetCrossF.get(key).add(pc.crossF);
					lstTargetCrossF.put(key,lstTargetCrossF.get(key));
					
					lstCrossF.add(pc.crossF);
					
					lstTargetWithinF.get(key).add(pc.withinF);
					lstTargetWithinF.put(key,lstTargetWithinF.get(key) );
					
					lstWithinF.add(pc.withinF);
				}
			}
			
			String[] mlAlgorithms = {"weka.classifiers.trees.J48",
					"weka.classifiers.trees.RandomForest",
					//"weka.classifiers.bayes.BayesNet",
					"weka.classifiers.bayes.NaiveBayes",
					"weka.classifiers.functions.Logistic",};
					//"weka.classifiers.functions.SMO"};
					//"weka.classifiers.functions.MultilayerPerceptron"};
			
			for(String algorithm:mlAlgorithms){
				lstCrossF.clear();
				lstWithinF.clear();
				int coverage = 0;
				for(String key:lstTargetCrossF.keySet()){
					if(key.startsWith(algorithm)){
						String checked = lstTargetCrossF.get(key).size()==0?"0":"1";
						System.out.println(key + "," + checked + "," + ArrayListUtil.getAverage(lstTargetCrossF.get(key)) + "," + ArrayListUtil.getAverage(lstTargetWithinF.get(key)));
						
						for(Double value:lstTargetCrossF.get(key))
							lstCrossF.add(value);
						
						for(Double value:lstTargetWithinF.get(key))
							lstWithinF.add(value);
						
						if(lstTargetCrossF.get(key).size()>0)
							coverage++;
					}
				}
				
				System.out.println("Total coverage,"+coverage);
				
				System.out.println("AvgCrossF," + ArrayListUtil.getAverage(lstCrossF));
				System.out.println("AvgWithinF," + ArrayListUtil.getAverage(lstWithinF));
				
				System.out.print("crossF: ");
				for(double f:lstCrossF){
					System.out.print(f + ",");
				}
				System.out.println();
				
				System.out.print("withinF: ");
				for(double f:lstWithinF){
					System.out.print(f + ",");
				}
				
				System.out.println("\n\n");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class PredictionCombination{
	String source;
	String target;
	double numTarget;
	double buggyRate;
	double srcTarRate;
	String algorithm;
	double withinF;
	double crossF;
	
	PredictionCombination(String source,String target,String numTarget,String buggyRate,String srcTarRate,String algorithm,String withinF, String crossF){
		this.source = source;
		this.target = target;
		this.numTarget = Double.parseDouble(numTarget);
		this.buggyRate = Double.parseDouble(buggyRate);
		this.srcTarRate = Double.parseDouble(srcTarRate);
		this.algorithm = algorithm;
		this.crossF = Double.parseDouble(crossF);
		this.withinF = Double.parseDouble(withinF);
	}
}

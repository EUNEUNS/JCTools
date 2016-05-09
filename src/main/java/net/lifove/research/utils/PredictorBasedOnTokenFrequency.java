package net.lifove.research.utils;

import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Instances;

public class PredictorBasedOnTokenFrequency {
	
	ArrayList<String> featureNames;
	HashMap<Integer,Double> tokenProbability = new HashMap<Integer,Double>(); // <Integer,Double> = <token index,token probability>
	HashMap<Integer,Double> buggyTokenCounter = new HashMap<Integer,Double>(); // <Integer,Integer> = <token index,token counter>
	HashMap<Integer,Double> cleanTokenCounter = new HashMap<Integer,Double>(); // <Integer,Integer> = <token index,token counter>
	
	HashMap<Integer,Double> tfidfBuggy = new HashMap<Integer,Double>();
	HashMap<Integer,Double> tfidfClean = new HashMap<Integer,Double>();
	
	
	double probabilityCutoff = 0.5; // inclusive

	
	public static void main(String[] args) {
		PredictorBasedOnTokenFrequency runner = new PredictorBasedOnTokenFrequency();
		runner.run(args);
	}

	void run(String[] args) {
		
		// load source file
		Instances srcInstances = WekaUtils.loadArff(args[0]);
		
		// load target file
		Instances tarInstances = WekaUtils.loadArff(args[1]);
		
		// load a list of features
		//featureNames = FileUtil.getLines(args[2], false);
		
		probabilityCutoff = Double.parseDouble(args[2]);
		
		//buildTokenProbabilityBasedOnTFRanking(srcInstances);
		buildTokenProbabilityBasedOnTFIDF(srcInstances);
		
		predict(tarInstances);
	}

	private void buildTokenProbabilityBasedOnTFIDF(Instances instances) {
		countBuggyNCleanTokens(instances);
		int numBuggyInstances = instances.attributeStats(instances.classIndex()).nominalCounts[1];
		int numCleanInstances = instances.attributeStats(instances.classIndex()).nominalCounts[0];
		
		
		for(int attrIdx=0;attrIdx<instances.numAttributes();attrIdx++){ // ignore class (last) attribute
			double tfB = buggyTokenCounter.containsKey(attrIdx)?buggyTokenCounter.get(attrIdx):0; // boolean frequency since our data is boolean values.
			//double idfB = Math.log(numBuggyInstances/tfB); // since tf is boolean frequency, the number of documents having the token is same as tf.
			//double tfidfB = tfB*idfB;
			//tfidfB = tfidfB.equals(Double.NaN)?0.0:tfidfB;
			//tfidfBuggy.put(attrIdx,tfidfB);
			
			
			double tfC = cleanTokenCounter.containsKey(attrIdx)?cleanTokenCounter.get(attrIdx):0;
			//double idfC = Math.log(numCleanInstances/tfC); // since tf is boolean frequency, the number of documents having the token is same as tf.
			//double tfidfC = tfC*idfC;
			//tfidfClean.put(attrIdx,tfidfC);
			
			/*/double tokenProb = 0;
			if(tfB==0)
				tokenProb = 0;
			else if(tfC==0)
				tokenProb = 1;
			else
				tokenProb = tfidfB>tfidfC?(tfidfC/tfidfB)*0.5:1-(tfidfB/tfidfC)*0.5;*/
			
			double tfBR = tfB/numBuggyInstances;
			double tfCR = tfC/numCleanInstances;
			double tokenProb =(tfBR/(tfBR+tfCR)); //(tfB/(tfB+tfC));//(tfBR/(tfBR+tfCR));//*(tfB/numBuggyInstances);
			tokenProbability.put(attrIdx, tokenProb);
		}
	}

	private void predict(Instances instances) {
		// Predict (compute prediction probability using token probability)
		int TP=0,FP=0,TN=0,FN=0;
		
		int numBuggyWithNewTokens=0;
		int numCleanWithNewTokens=0;
		
		// iteration for each target instance
		for(int instIdx=0;instIdx < instances.numInstances();instIdx++){
		
			// token (attribute) values
			double[] values = instances.get(instIdx).toDoubleArray();
			
			// original label
			boolean isBuggy = values[instances.classIndex()]==1.0?true:false;
			
			double sumTokenProbability = 0;
			int numValidTokens = 0;
			double maxTokenProbability = 0;
			double minTokenProbability = 1;
			double bestBuggyFrequencyTokenProbability=0;
			double maxNumBuggyInstancesForToken = 0;
			boolean hasNewTokens =  false;
			ArrayList<Double> probOfValidTokens = new ArrayList<Double>();
			//System.out.println("***Instance=" + instIdx + "," + isBuggy);
			
			// get each token (attribute) buggy probability
			for(int attrIdx=0; attrIdx<instances.numAttributes()-1;attrIdx++){ // ignore class
				double attrValue = values[attrIdx];
				double numBuggyInstancesForToken=buggyTokenCounter.containsKey(attrIdx)?buggyTokenCounter.get(attrIdx):0;
				
				// only consider tokens whose attribute values are > 0
				// based on corresponding token probability, the token affects whether the instance is buggy or clean.
				if(attrValue>0){
					
					double tokenProb = tokenProbability.get(attrIdx);
					//System.out.println("token=" + attrIdx + ",tokenProb=" + tokenProb + ",numBuggyInstancesForToken=" + numBuggyInstancesForToken + ",numCleanInstancesForToken=" + (cleanTokenCounter.containsKey(attrIdx)?cleanTokenCounter.get(attrIdx):0));
					
					// the token does not have any values in a training set, consider as new token.
					if(tokenProb==-1){
						hasNewTokens=true;
						System.out.println("f" + (attrIdx+1));
					}
					
					// non-existing probability then make it as 0.5. but there are no these cases.
					tokenProb = tokenProb>=0?tokenProb:0.5;
					
					probOfValidTokens.add(tokenProb);
					
					numValidTokens+=attrValue;
					sumTokenProbability+=tokenProb*attrValue;
					
					if(maxTokenProbability < tokenProb)
						maxTokenProbability = tokenProb;
					
					if(minTokenProbability > tokenProb)
						minTokenProbability = tokenProb;

					if(maxNumBuggyInstancesForToken<numBuggyInstancesForToken){
						maxNumBuggyInstancesForToken=numBuggyInstancesForToken;
						bestBuggyFrequencyTokenProbability=tokenProb;
					}
				}
			}
			
			if(isBuggy && hasNewTokens)
				numBuggyWithNewTokens++;
			
			if(!isBuggy && hasNewTokens)
				numCleanWithNewTokens++;
				
			// TODO predictionProbability
			double predictionProbability = maxTokenProbability; //sumTokenProbability/numValidTokens;
			//double predictionProbability = bestBuggyFrequencyTokenProbability;
			//double predictionProbability = sumTokenProbability/numValidTokens;
			//double predictionProbability = ArrayListUtil.getMedian(probOfValidTokens);
			//double predictionProbability = minTokenProbability; //sumTokenProbability/numValidTokens;
			
			//System.out.println(predictionProbability);
			
			if(predictionProbability>=probabilityCutoff){
				if(isBuggy)
					TP++;
				else
					FP++;
			}
			else{
				if(isBuggy)
					FN++;
				else
					TN++;
			}
		}
		
		//System.out.println("numBuggyWithNewTokens=" + numBuggyWithNewTokens);
		//System.out.println("numCleanWithNewTokens=" + numCleanWithNewTokens);
		
		double precision = WekaUtils.getPrecision(TP, FP, TN, FN);
		double recall = WekaUtils.getRecall(TP, FP, TN, FN);
		double fmeasure = WekaUtils.getFmeasure(TP, FP, TN, FN);
		System.out.print(DecimalUtil.threeDecimal(precision) + "," + DecimalUtil.threeDecimal(recall) +"," + DecimalUtil.threeDecimal(fmeasure));
		System.out.println(", TP=" + TP + ", FP=" + FP + ", TN=" + TN + ", FN=" + FN);
	}

	public HashMap<Integer,Double> buildTokenProbabilityBasedOnTFRanking(Instances instances) {
		
		// count tokens by class
		countBuggyNCleanTokens(instances);
		
		// compute token probability
		// rank tokens by count
		buggyTokenCounter = (HashMap<Integer, Double>) HashMapUtil.sortByValue(buggyTokenCounter);
		cleanTokenCounter = (HashMap<Integer, Double>) HashMapUtil.sortByValue(cleanTokenCounter);
		
		ArrayList<Integer> buggyTokenRanking = new ArrayList<Integer>();
		ArrayList<Integer> cleanTokenRanking = new ArrayList<Integer>();
		
		
		for(Integer key:buggyTokenCounter.keySet()){
			buggyTokenRanking.add(key);
			//System.out.println("buggy\t" + key + "\t" + buggyTokenCounter.get(key));
		}
		
		for(Integer key:cleanTokenCounter.keySet()){
			cleanTokenRanking.add(key);
			//System.out.println("clean\t" + key + "\t" + cleanTokenCounter.get(key));
		}
		
		// compute token probability
		for(int attrIdx = 0; attrIdx < instances.numAttributes()-1;attrIdx++){
			int buggyRank = buggyTokenRanking.contains(attrIdx)?buggyTokenRanking.indexOf(attrIdx):-1; // -1 means not-exist
			int cleanRank = cleanTokenRanking.contains(attrIdx)?cleanTokenRanking.indexOf(attrIdx):-1;
			
			if(buggyRank==-1 && cleanRank==-1)
				tokenProbability.put(attrIdx, -1.0);
			else if(buggyRank==-1){
				tokenProbability.put(attrIdx, 0.0);
			}
			else if(cleanRank==-1){
				tokenProbability.put(attrIdx, 1.0);
			}	
			else{
				// TODO based on ranking
				tokenProbability.put(attrIdx, (double) (cleanRank+1)/(buggyRank+1+cleanRank+1));
				
				// TODO based on frequency
				//tokenProbability.put(attrIdx, (double) buggyTokenCounter.get(attrIdx)/(buggyTokenCounter.get(attrIdx)+cleanTokenCounter.get(attrIdx)));
			}
		}
		
		//for(Integer key:tokenProbability.keySet())
		//	System.out.println("prob\t" + key + "\t" + tokenProbability.get(key));
		
		return tokenProbability;
	}

	private void countBuggyNCleanTokens(Instances instances) {
		for(int instIdx = 0; instIdx<instances.size();instIdx++){
			double[] values = instances.get(instIdx).toDoubleArray();
			
			boolean isBuggy = values[instances.classIndex()]==1.0?true:false;
			
			for(int attrIdx = 0; attrIdx < instances.numAttributes()-1;attrIdx++){ // ignore class attribute
				
				double attrValue =  values[attrIdx];
				
				if(attrValue>0){
					if(isBuggy){
						countToken(attrIdx,attrValue,buggyTokenCounter);
					}
					else{
						countToken(attrIdx,attrValue,cleanTokenCounter);
					}
				}
			}
		}
	}

	private void countToken(int instIdx,double attrValue,HashMap<Integer,Double> tokenCounter) {
		if(!tokenCounter.containsKey(instIdx))
			tokenCounter.put(instIdx,attrValue);
		else{
			tokenCounter.put(instIdx,tokenCounter.get(instIdx)+attrValue);
		}
	}

}

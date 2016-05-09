package net.lifove.research.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;

public class PredictorBasedOnInstanceSimilarity {
	
	ArrayList<String> patchNumForTrainingData;
	ArrayList<String> patchNumForTestData;
	String pathForPatches = "";
	//double SIMILARITY_THD = 0.3; // inclusive >=
	double THRESHOLD=0.000000000; // inclusive > 0 means any of similar instance to buggy src instaces is predicted to be buggy.
	int sameOneValueTockenAtLeast = 1;
	boolean ADAPTIVE = false;
	boolean HYBRID = false;
	boolean TrainingSetGuided = false;
	boolean PrintPatch = false;
	double BuggyValueIdx = 1.0; // clean label = 0.0, buggy label = 1.0 (in weka file class {0,1})
	
	HashMap<String,Integer> buggyTokenCounts = new HashMap<String,Integer>();
	HashMap<String,Integer> cleanTokenCounts = new HashMap<String,Integer>();
	
	HashMap<Integer,Double> tokenProbability = new HashMap<Integer,Double>(); // <Integer,Double> = <token index,token probability>
	
	
	ArrayList<String> featureNames;
	
	public static void main(String[] args) {
		PredictorBasedOnInstanceSimilarity runner = new PredictorBasedOnInstanceSimilarity();
		runner.run(args);
	}

	private void run(String[] args) {
		
		// load source file
		Instances srcInstances = WekaUtils.loadArff(args[0]);
		
		// load target file
		Instances tarInstances = WekaUtils.loadArff(args[1]);
		
		// load a list of features
		featureNames = FileUtil.getLines(args[2], false);
		
		patchNumForTrainingData = FileUtil.getStringInASpecificColumn(args[3], false,0);
		patchNumForTestData = FileUtil.getStringInASpecificColumn(args[4], false,0);
		pathForPatches = args[5];
		
		double simThreshold = Double.parseDouble(args[6]);
		sameOneValueTockenAtLeast = Integer.parseInt(args[7]);
		
		THRESHOLD = Double.parseDouble(args[8]);
		ADAPTIVE = Boolean.parseBoolean(args[9]);
		HYBRID  = Boolean.parseBoolean(args[10]);
		TrainingSetGuided = Boolean.parseBoolean(args[11]);
		PrintPatch = Boolean.parseBoolean(args[11]);
		
		// conduct prediction on a training set using n-fold cross validation
		double bestTHD=THRESHOLD;
		double bestSimilarityTHD=simThreshold;
					
		if(TrainingSetGuided){
			// get optimum THD and similarity_thd
			int numRepeat = 5;
			int folds = 10;
			
			HashMap<String,Double> resultsFromTrainingSet = new HashMap<String,Double>();
			for(int repeat=0; repeat<numRepeat;repeat++){
				
				srcInstances.randomize(new Random(repeat)); 
				srcInstances.stratify(folds);
				
				for(int n=0;n<folds;n++){
					Instances trainingSet = srcInstances.trainCV(folds, n);
					Instances testSet = srcInstances.testCV(folds, n);
					for(int i=1; i<=20;i++){
						double similarity_thd = 0.05*i;
						String key = DecimalUtil.twoDecimal(similarity_thd);
						
						Measure measure = patternBasedPrediction(trainingSet,testSet,0.0,similarity_thd,true);
						
						//System.out.println(repeat + "," + n + "," + key + "," + measure.precision + "," + measure.recall + "," +measure.fmeasure);
						
						if(!resultsFromTrainingSet.containsKey(key))
							resultsFromTrainingSet.put(key,measure.fmeasure);
						else
							resultsFromTrainingSet.put(key, resultsFromTrainingSet.get(key) + measure.fmeasure);	
					}
				}
			}
			
			resultsFromTrainingSet=(HashMap<String, Double>) HashMapUtil.sortByValue(resultsFromTrainingSet);
			
			int i=0;
			for(String key:resultsFromTrainingSet.keySet()){
				//System.out.println(key + " " + resultsFromTrainingSet.get(key));
				if(i==0){
					bestSimilarityTHD = Double.parseDouble(key);
				}
				//break;
				i++;
			}
			
			//System.out.println("\nbestSimTHD=" + bestSimilarityTHD);
		}
		
		tokenProbability = new PredictorBasedOnTokenFrequency().buildTokenProbabilityBasedOnTFRanking(srcInstances);
		
		patternBasedPrediction(srcInstances,tarInstances,bestTHD,bestSimilarityTHD,false);
		
		/*for(String key:buggyTokenCounts.keySet()){
			System.out.println( "buggy\t" + key + "\t" + buggyTokenCounts.get(key));
		}
		
		for(String key:cleanTokenCounts.keySet()){
			System.out.println( "clean\t" + key + "\t" + cleanTokenCounts.get(key));
		}*/
	}
	
	ArrayList<Integer> truePositives = new ArrayList<Integer>();// list of true positives to see what makes them be buggy.
	
	private Measure patternBasedPrediction(Instances srcInstances,Instances tarInstances,double THD,double similarity_thd,boolean isForTrainingSet){
		
		HashMap<Integer,PredictionInfo> predInfo = new HashMap<Integer,PredictionInfo>();
		
		THRESHOLD=THD;
		//SIMILARITY_THD = similarity_thd;
		
		// compare src inst and tar inst
		for(int srcIdx=0; srcIdx < srcInstances.size(); srcIdx++){
			
			// get metric values of an src instance
			double[] values = srcInstances.get(srcIdx).toDoubleArray();
			//System.out.println("Instance " + srcIdx + ": "  + (values[values.length-1]==0?"clean":"buggy"));
			
				//System.out.println("Buggy " + (srcIdx+1) );
			// compare with each tar inst to the current src inst
			for(int tarIdx=0; tarIdx < tarInstances.size(); tarIdx++){
				
				// get metric values of a current tar inst
				double[] tarValues = tarInstances.get(tarIdx).toDoubleArray();
				// get labels of src and tar instances on comparing
				boolean isBuggySrc = (values[values.length-1]==BuggyValueIdx); // index of class attribute
				boolean isBuggyTar = (tarValues[tarValues.length-1]==BuggyValueIdx); // index of class attribute
				
				// create prediction info for the current star instance. Initiate with its original lable
				PredictionInfo curPredInfo = new PredictionInfo(isBuggyTar);
				
				// check and add hashmap
				if(!predInfo.containsKey(tarIdx+1)){
					predInfo.put(tarIdx+1,curPredInfo);
				}
				else{
					curPredInfo = predInfo.get(tarIdx+1);
				}
				
				// compare similarity between tar and src instances in the current iteration
				double similarity = getSimilarity(values,tarValues,srcIdx,tarIdx,similarity_thd);
				
				// decide similarity based on a threshold value
				if(similarity>=similarity_thd){
					
					/*System.out.println("===" + (srcIdx+1) + "-" + (tarIdx+1) + " " + similarity + ": ");
					System.out.println(Arrays.toString(values));
					System.out.println(Arrays.toString(tarValues));*/
					
					// if the label of the current src inst is buggy
					if(isBuggySrc){
						// predicted as buggy
						curPredInfo.putInfo("P", (srcIdx+1), similarity);
						//System.out.println("P " + verboseStringForBuggyCases);
					}
					else{
						// predicted as clean
						curPredInfo.putInfo("N", (srcIdx+1), similarity);
					}
				}
				else{ // src and target is not similar
						// unclassified at this moment
						curPredInfo.putInfo("U", (srcIdx+1), similarity);
				}
			}
		}
		
		int TP=0,FP=0,TN=0,FN=0,UP=0,UN=0;
		
		// since prediction finished, compute buggy probability and compute TP, FP, TN, FN, and unclassified ones
		// and make a list of  classified instances and unclassified instances to classify unclassified ones using classified ones LATER.
		ArrayList<Integer> classifiedTarInstancesAsP = new ArrayList<Integer>(); // value is the tar index starting from 1
		ArrayList<Integer> classifiedTarInstancesAsN = new ArrayList<Integer>(); // value is the tar index starting from 1
		ArrayList<Integer> unclassifiedTarInstances = new ArrayList<Integer>();
		for(Integer key:predInfo.keySet()){ // key is tar instance index starting from 1
			PredictionInfo info = predInfo.get(key);
			
			info.buggyProbability();
			
			if(info.buggyProbability.equals(Double.NaN)){
				if(info.isBuggy){
					UP++;
					unclassifiedTarInstances.add(key);
				}else{
					UN++;
					unclassifiedTarInstances.add(key);
				}
			}
			else{
				// a tar instance is buggy
				if(info.isBuggy){
					// predicted as buggy
					if(info.buggyProbability>THRESHOLD){
						TP++;
						classifiedTarInstancesAsP.add(key);
						truePositives.add(key);
					//predicted as clean (negative) >> false negative
					}else{
						FN++;
						classifiedTarInstancesAsN.add(key);
					}
				// a tar instance is clean
				}else{
					// predicted as buggy >> false positive
					if(info.buggyProbability>THRESHOLD){
						FP++;
						classifiedTarInstancesAsP.add(key);
					// predicted as clean (negative)
					}else{
						TN++;
						classifiedTarInstancesAsN.add(key);
					}
				}
			}
			//System.out.println(key + "," + (info.isBuggy?"buggy":"clean") + "," +  info.buggyProbability);
			
		}
		
		DecimalFormat df = new DecimalFormat("00.0");
		
		if(!isForTrainingSet){
			System.out.print("," + "TP: " + TP + " " +
								"FP: " + FP + " " +
								 "TN: " + TN + " " +
								  "FN: " + FN + " " +
								  	"UP: " + UP + " " +
								  	 "UN: " + UN);
	
			// percentage of unclassified ones
			System.out.print( " (" + df.format((double)(UP+UN)*100/tarInstances.numInstances()) + "%)");
			
			// compute performance by excluding all unclassified ones.
			System.out.print("," + df.format(WekaUtils.getPrecision(TP, FP, TN, FN)*100) + " " + 
					df.format(WekaUtils.getRecall(TP, FP, TN, FN)*100) + " " +
					df.format(WekaUtils.getFmeasure(TP, FP, TN, FN)*100));
			
			// compute performance by including all unclassified ones.
			System.out.print("," + df.format(WekaUtils.getPrecision(TP, FP, TN+UN, FN+UP)*100) + " " + 
					df.format(WekaUtils.getRecall(TP, FP, TN+UN, FN+UP)*100) + " " +
					df.format(WekaUtils.getFmeasure(TP, FP, TN+UN, FN+UP)*100));
		}
		
		int[] confusionMatrixForUnclassfied = null;
		
		
		// conduct HYBRID or ADPATIVE
		if (HYBRID || ADAPTIVE){
			if(HYBRID)
				confusionMatrixForUnclassfied = hybridApproach(srcInstances,tarInstances,classifiedTarInstancesAsP,classifiedTarInstancesAsN,unclassifiedTarInstances);
			else if(ADAPTIVE)  // adaptive
				confusionMatrixForUnclassfied = selfPatternBasedApproach(srcInstances,tarInstances,classifiedTarInstancesAsP,classifiedTarInstancesAsN,unclassifiedTarInstances,similarity_thd);
			
			TP = TP+confusionMatrixForUnclassfied[0];
			FP = FP+confusionMatrixForUnclassfied[1];
			TN = TN+confusionMatrixForUnclassfied[2];
			FN = FN+confusionMatrixForUnclassfied[3];
			UP = confusionMatrixForUnclassfied[4];
			UN = confusionMatrixForUnclassfied[5];
			
			if(!isForTrainingSet){
				System.out.print(", Final " + "TP: " + TP + " " +
						"FP: " + FP + " " +
						 "TN: " + TN + " " +
						  "FN: " + FN + " " +
						  	"UP: " + UP + " " +
						  	 "UN: " + UN);
			
				//System.out.print(SIMILARITY_THD + ": " + df.format(WekaUtils.getPrecision(TP, FP, TN, FN)*100) + " " +
				//System.out.print("," + "TP=" +TP + " FP=" +FP + " TN=" +TN + " FN=" +FN + " UP=" +UP+ " UN=" +UN + "," + df.format(WekaUtils.getPrecision(TP, FP, TN, FN)*100) + " " + 
				//											df.format(WekaUtils.getRecall(TP, FP, TN, FN)*100) + " " +
				//											df.format(WekaUtils.getFmeasure(TP, FP, TN, FN)*100));
				
				// percentage of unclassified ones
				System.out.print(" (" + df.format((double)(UP+UN)*100/tarInstances.numInstances()) + "%)");
				
				// compute performance of recursive approach by excluding all unclassified ones.
				System.out.print("," + df.format(WekaUtils.getPrecision(TP, FP, TN, FN)*100) + " " + 
								df.format(WekaUtils.getRecall(TP, FP, TN, FN)*100) + " " +
								df.format(WekaUtils.getFmeasure(TP, FP, TN, FN)*100));
				
				// compute performance of recursive approach by including all unclassified ones.		
				System.out.print("," + df.format(WekaUtils.getPrecision(TP, FP, TN+UN, FN+UP)*100) + " " + 
																	df.format(WekaUtils.getRecall(TP, FP, TN+UN, FN+UP)*100) + " " +
																	df.format(WekaUtils.getFmeasure(TP, FP, TN+UN, FN+UP)*100));
			}
		}
		
		// display true positives and their tokens related to buggy changes.
		System.out.println();
		
		for(Integer key:truePositives){
			//System.out.println("===== Target instance " + key + " (starting from 1) =====");
			
			int maxTokenNum = 0;
			int trainingInstKeyWhoHasMostSameTokens = -1;
			boolean IsTrainingInstanceFromTarget = false;
			for(String tokens: tokensThatMakeToBeBuggy.get(key)){
				boolean isCurrentTrainingInstanceFromTarget = tokens.indexOf("-")==0?true:false;
				// if training instance from target, then instance idx is minus.
				int trainingInstKey=isCurrentTrainingInstanceFromTarget?Integer.parseInt(tokens.split("-")[1]):Integer.parseInt(tokens.split("-")[0]);
				int tokenNum = tokens.split(",").length;
				if(tokenNum>maxTokenNum){
					maxTokenNum = tokenNum;
					trainingInstKeyWhoHasMostSameTokens = trainingInstKey;
					IsTrainingInstanceFromTarget = isCurrentTrainingInstanceFromTarget;
				}
				//System.out.println(tokens);
			}
			
			if(PrintPatch){
				// display buggy tokens in a representative training instance
				String srcPatchName = IsTrainingInstanceFromTarget?patchNumForTestData.get(trainingInstKeyWhoHasMostSameTokens-1):patchNumForTrainingData.get(trainingInstKeyWhoHasMostSameTokens-1); // key starts from 1 so need to -1 for real intance index
				String tarPatchName = patchNumForTestData.get(key-1);
				
				
				// show src patch
				System.out.println("############### source intance " + trainingInstKeyWhoHasMostSameTokens + " " + srcPatchName + " ###############");
				FileUtil.print(FileUtil.getLines(pathForPatches + File.separator + (IsTrainingInstanceFromTarget?tarPatchName:srcPatchName), true), -1);
				
				// show tar patch
				System.out.println("############### target instance " + key + " " + tarPatchName + "###############");
				FileUtil.print(FileUtil.getLines(pathForPatches + File.separator + tarPatchName, true), -1);
			}
		}
		
		double precision = WekaUtils.getPrecision(TP, FP, TN+UN, FN+UP);
		double recall = WekaUtils.getRecall(TP, FP, TN+UN, FN+UP);
		double fmeasure = WekaUtils.getFmeasure(TP, FP, TN+UN, FN+UP);
		Measure result = new Measure(precision,recall,fmeasure,TP,FP,TN+UN,FN+UP);
		
		return result;
	}

	private int[] hybridApproach(Instances srcInstances, Instances tarInstances,
			ArrayList<Integer> classifiedTarInstancesAsP,	// value is the tar index starting from 1
			ArrayList<Integer> classifiedTarInstancesAsN,	// value is the tar index starting from 1
			ArrayList<Integer> unclassifiedTarInstances) {
		
		// create new training set
		Instances newTrainingData = new Instances(srcInstances);
		
		// add target instances (with predicted labels) predicted in the previous step to this new training set
		for(Integer tarInstIdx:classifiedTarInstancesAsP){
			Instance newInst = tarInstances.get(tarInstIdx-1);
			newInst.setClassValue(1.0); // assign the buggy label as predicted
			newTrainingData.add(newInst);
		}
		
		for(Integer tarInstIdx:classifiedTarInstancesAsN){
			Instance newInst = tarInstances.get(tarInstIdx-1);
			newInst.setClassValue(0.0); // assign the clean label as predicted
			newTrainingData.add(newInst);
		}
		
		// create new test set with unclassified instances;
		Instances newTestData = new Instances(tarInstances,0);
		
		for(Integer unclassifiedInstIdx:unclassifiedTarInstances){
			Instance newInst = tarInstances.get(unclassifiedInstIdx-1);
			newTestData.add(newInst);
		}
		
		// resampling for training set
		/*SpreadSubsample resampling = new SpreadSubsample();
		resampling.setMaxCount(0.0);
		resampling.setDistributionSpread(1.0);
		resampling.setRandomSeed(1);
		resampling.setAdjustWeights(false);
		
		
		try {
			resampling.setInputFormat(newTrainingData);
			newTrainingData = Filter.useFilter(newTrainingData, resampling);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		// build a model and predict the instances in the new test set.
		// weka.classifiers.bayes.NaiveBayes weka.classifiers.trees.RandomForest
		Measure measure = SimpleCrossPredictor.crossPrediction(newTrainingData, newTestData, "weka.classifiers.bayes.NaiveBayes", "1");
		
		// compute TP, FP, TN, FN
		int[] confusionMatrixForUnclassified = {measure.TP,measure.FP,measure.TN,measure.FN,0,0};
		
		return confusionMatrixForUnclassified;
	}

	private int[] selfPatternBasedApproach(Instances srcInstances, Instances tarInstances,
			ArrayList<Integer> classifiedTarInstancesAsP,
			ArrayList<Integer> classifiedTarInstancesAsN,
			ArrayList<Integer> unclassifiedTarInstances, double simThreshold) {
		
		int numUn = unclassifiedTarInstances.size();
		
		HashMap<Integer,PredictionInfo> predInfo = new HashMap<Integer,PredictionInfo>();
		
		for(Integer key:unclassifiedTarInstances){
			int tarIdx = key-1;
			double[] tarValues = tarInstances.get(tarIdx).toDoubleArray();
			
			boolean isBuggyTar = (tarValues[tarValues.length-1]==BuggyValueIdx); // index of class attribute
			
			// create prediction info for the current star instance. Initiate with its original lable
			PredictionInfo curPredInfo = new PredictionInfo(isBuggyTar);
			
			// check and add hashmap
			if(!predInfo.containsKey(tarIdx+1)){
				predInfo.put(tarIdx+1,curPredInfo);
			}
			else{
				curPredInfo = predInfo.get(tarIdx+1);
			}
			
			// compare similarity between src instances and unclassified target instances
			/*for(int srcIdx=0; srcIdx<srcInstances.size();srcIdx++){
				double[] srcInstValues = srcInstances.get(srcIdx).toDoubleArray();
				boolean isSrcInstBuggy = srcInstances.get(srcIdx).classValue()==BuggyValueIdx?true:false;
				
				// compare similarity between tar and src instances in the current iteration
				double similarity = getSimilarity(srcInstValues,tarValues,srcIdx,tarIdx,simThreshold); // when use target P instances is training set, display tarIdx with minus sign. -2 is to adjust the final key value with minus sign in getSimilaity
				
				if(isSrcInstBuggy){
					if(similarity>=simThreshold){
						//System.out.println(key + "-P:" + similarity);
						// predicted as buggy
						curPredInfo.putInfo("P", (srcIdx+1), similarity);
					}
					else{ // src and target is not similar
						// unclassified at this moment
						curPredInfo.putInfo("U", (srcIdx+1), similarity);
					}
				}else{
					// decide similarity based on a threshold value
					if(similarity>=simThreshold){
						//System.out.println(key + "-N:" + similarity);
						// predicted as buggy
						curPredInfo.putInfo("N", (srcIdx+1), similarity);
					}
					else{ // src and target is not similar
						// unclassified at this moment
						curPredInfo.putInfo("U", (srcIdx+1), similarity);
					}	
				}
				
			}*/
			
			// compare with classifiedTarInstancesAsP
			for(Integer keyOfP:classifiedTarInstancesAsP){
				int tarIdxP = keyOfP-1;
				double[] valuesOfP = tarInstances.get(tarIdxP).toDoubleArray();
				// change class label as P
				valuesOfP[valuesOfP.length-1]=1.0;
				
				// compare similarity between tar and src instances in the current iteration
				double similarity = getSimilarity(valuesOfP,tarValues,-tarIdxP-2,tarIdx,simThreshold); // when use target P instances is training set, display tarIdx with minus sign. -2 is to adjust the final key value with minus sign in getSimilaity
				
				//System.out.println(similarity);
				
				// decide similarity based on a threshold value
				if(similarity>=simThreshold){
					//System.out.println(key + "-P:" + similarity);
					// predicted as buggy
					curPredInfo.putInfo("P", ((-tarIdxP-2)+1), similarity);
				}
				else{ // src and target is not similar
					// unclassified at this moment
					curPredInfo.putInfo("U", ((-tarIdxP-2)+1), similarity);
				}
			}
			
			// compare with classifiedTarInstancesAsN
			for(Integer keyOfN:classifiedTarInstancesAsN){
				int tarIdxN = keyOfN-1;
				double[] valuesOfN = tarInstances.get(tarIdxN).toDoubleArray();
				// change class label as N
				valuesOfN[valuesOfN.length-1]=0.0;
				
				// compare similarity between tar and src instances in the current iteration
				double similarity = getSimilarity(valuesOfN,tarValues,tarIdxN,tarIdx,simThreshold);
				
				// decide similarity based on a threshold value
				if(similarity>=simThreshold){
					//System.out.println(key + "-N:" + similarity);
					// predicted as buggy
					curPredInfo.putInfo("N", ((-tarIdxN-2)+1), similarity);
				}
				else{ // src and target is not similar
					// unclassified at this moment
					curPredInfo.putInfo("U", ((-tarIdxN-2)+1), similarity);
				}	
			}
		}
		
		int TP=0,FP=0,TN=0,FN=0,UP=0,UN=0;
		
		// since prediction finished, compute buggy probability and compute TP, FP, TN, FN, and unclassified ones
		// and make a list of  classified instances and unclassified instances to classify unclassified ones using classified ones LATER.
		ArrayList<Integer> classifiedTarInstAsP = new ArrayList<Integer>(); // value is the tar index starting from 1
		ArrayList<Integer> classifiedTarInstAsN = new ArrayList<Integer>(); // value is the tar index starting from 1
		ArrayList<Integer> unclassifiedTarInst = new ArrayList<Integer>();

		for(Integer key:predInfo.keySet()){ // key is unclassified tar instance index starting from 1
			PredictionInfo info = predInfo.get(key);
			
			info.buggyProbability();
			
			if(info.buggyProbability.equals(Double.NaN)){
				if(info.isBuggy){
					UP++;
					unclassifiedTarInst.add(key);
				}else{
					UN++;
					unclassifiedTarInst.add(key);
				}
			}
			else{
				// a tar instance is buggy
				if(info.isBuggy){
					// predicted as buggy
					if(info.buggyProbability>THRESHOLD){
						TP++;
						classifiedTarInstAsP.add(key);
						truePositives.add(key);
					//predicted as clean (negative) >> false negative
					}else{
						FN++;
						classifiedTarInstAsN.add(key);
					}
				// a tar instance is clean
				}else{
					// predicted as buggy >> false positive
					if(info.buggyProbability>THRESHOLD){
						FP++;
						classifiedTarInstAsP.add(key);
					// predicted as clean (negative)
					}else{
						TN++;
						classifiedTarInstAsN.add(key);
					}
				}
			}
		}
		
		int[] confusionMatrixForUnclassified = {-1,-1,-1,-1,-1,-1};
		
		// stop there are no additional classifications
		if(numUn == UP+UN){
			if(ADAPTIVE && simThreshold>0.0){ // when ADAPTIVE, decrease SIMILARITY_THD until it goes to 0.0 so that we can predict as many as possible
				//System.out.println("sim_thd=" + (simThreshold-0.01) + " is processing!");
				confusionMatrixForUnclassified = selfPatternBasedApproach(srcInstances,tarInstances,classifiedTarInstAsP,classifiedTarInstAsN,unclassifiedTarInst,simThreshold-0.01);
			}else{
				confusionMatrixForUnclassified[0]=TP;
				confusionMatrixForUnclassified[1]=FP;
				confusionMatrixForUnclassified[2]=TN;
				confusionMatrixForUnclassified[3]=FN;
				confusionMatrixForUnclassified[4]=UP;
				confusionMatrixForUnclassified[5]=UN;
			}
			return confusionMatrixForUnclassified;
		}
		
		confusionMatrixForUnclassified = selfPatternBasedApproach(srcInstances,tarInstances,classifiedTarInstAsP,classifiedTarInstAsN,unclassifiedTarInst,simThreshold);
		
		int newTP=confusionMatrixForUnclassified[0], newFP=confusionMatrixForUnclassified[1],
				newTN=confusionMatrixForUnclassified[2], newFN=confusionMatrixForUnclassified[3],
				newUP=confusionMatrixForUnclassified[4], newUN=confusionMatrixForUnclassified[5];;
		
		
		
		confusionMatrixForUnclassified[0] = TP+newTP;
		confusionMatrixForUnclassified[1] = FP+newFP;
		confusionMatrixForUnclassified[2] = TN+newTN;
		confusionMatrixForUnclassified[3] = FN+newFN;
		confusionMatrixForUnclassified[4] = newUP;
		confusionMatrixForUnclassified[5] = newUN;
		
		return confusionMatrixForUnclassified;
	}

	HashMap<Integer,ArrayList<String>> tokensThatMakeToBeBuggy = new HashMap<Integer,ArrayList<String>>(); // key: tarIdx+1, tokens realted to buggy changes
	String verboseStringForBuggyCases = "";
	
	private double getSimilarity(double[] values, double[] tarValues, int srcIdx, int tarIdx,double simThreshold) {
		
		int numAllValuesGreaterThanZero = 0;
		int numSimilarValueInTarget = 0;
		
		verboseStringForBuggyCases = "";
		
		ArrayList<Integer> sameTokens = new ArrayList<Integer>();
		
		// compare all values except for the last (label)
		for(int i=0;i<values.length-1;i++){
			//if(values[i]>=1 || tarValues[i]>=1){ // exact similarity
			if(values[i]>=1){	// source-fit
			//if(tarValues[i]>=1){ // target-fit
				numAllValuesGreaterThanZero++;
				// divide a small one by a big one
				if(values[i]<=tarValues[i]){
					if((double)values[i]/tarValues[i]>=simThreshold){
						numSimilarValueInTarget++;
						sameTokens.add(i);
					}
				}else{
					if((double)tarValues[i]/values[i]>=simThreshold){
						numSimilarValueInTarget++;
						sameTokens.add(i);
					}
				}	
			}
		}
		
		// at least sameOneValueTockenAtLeast tokens should be same
		if(numSimilarValueInTarget<sameOneValueTockenAtLeast)
			return 0.0;
		
		double similarity = (double)numSimilarValueInTarget/numAllValuesGreaterThanZero;
		
		// adjust similarity by using token probability
		double sumSameTokenProbability = 0.0;
		double maxSameTokenPriobability = 0.0;
		for(int tokenIdx=0;tokenIdx<sameTokens.size();tokenIdx++){
			double tokenProb = tokenProbability.get(tokenIdx);
			sumSameTokenProbability+=tokenProb;
			if(maxSameTokenPriobability<tokenProb)
				maxSameTokenPriobability=tokenProb;
		}
		similarity = similarity * (sumSameTokenProbability/sameTokens.size());
		//similarity = similarity * maxSameTokenPriobabilityl
		
		boolean isBuggySrc = (values[values.length-1]==BuggyValueIdx);
		//System.out.println(tarIdx);
		// print buggy tokens that make a prediction to be buggy
		if(isBuggySrc && similarity>=simThreshold){
			verboseStringForBuggyCases = ((srcIdx+1) + "-" + (tarIdx+1) + ": ");
			for(int tokenIdx:sameTokens){
				verboseStringForBuggyCases += featureNames.get(tokenIdx) + ",";
				
				String token = featureNames.get(tokenIdx);
				
				if(!buggyTokenCounts.containsKey(token)){
					buggyTokenCounts.put(token, 1);
				}
				else{
					buggyTokenCounts.put(token, buggyTokenCounts.get(token)+1);
				}
			}
			
			int key = tarIdx+1;
			if(!tokensThatMakeToBeBuggy.containsKey(key)){
				ArrayList<String> tokens = new ArrayList<String>();
				tokens.add(verboseStringForBuggyCases);
				tokensThatMakeToBeBuggy.put(key, tokens);
			}else{
				tokensThatMakeToBeBuggy.get(key).add(verboseStringForBuggyCases);
			}
		}
		else{
			for(int tokenIdx:sameTokens){
				String token = featureNames.get(tokenIdx);
				
				if(!cleanTokenCounts.containsKey(token)){
					cleanTokenCounts.put(token, 1);
				}
				else{
					cleanTokenCounts.put(token, cleanTokenCounts.get(token)+1);
				}
			}
		}
		return similarity; // if allOnesInSource is zero, it returns NaN.
	}
}

class PredictionInfo{
	boolean isBuggy;
	ArrayList<String> types;
	ArrayList<Integer> srcInsts; // start from 1
	ArrayList<Double> similarityScores;
	Double buggyProbability = 0.0;
	
	PredictionInfo(boolean isBuggy){
		types = new ArrayList<String>();
		srcInsts = new ArrayList<Integer>();
		similarityScores = new ArrayList<Double>();
		this.isBuggy = isBuggy; 
	}
	
	void putInfo(String type,int srcInst,double similarity){
		types.add(type);
		srcInsts.add(srcInst);
		similarityScores.add(similarity);
	}
	
	void buggyProbability(){
		
		int P=0,N=0,U=0;
		
		// NOSIMILAR src instances? then clean
		if(types.size()==0){
			buggyProbability = 0.0; // no similar patterns? let them clean
			return;
		}
		
		for(int i=0;i<types.size();i++){
			
			if(isBuggy){
				if(types.get(i).equals("P")){
					P++;
				}else if(types.get(i).equals("N")){
					N++;
				}else
					U++;
			}else{
				if(types.get(i).equals("N")){
					N++;
				}else if(types.get(i).equals("P")){
					P++;
				}else
					U++;
			}
		}
		
		buggyProbability = (double)P/(P+N);
		//System.out.println(isBuggy + " P=" + P + " N=" + N + " U=" + U);
		//buggyProbability = (double)sumPSimScore/(sumPSimScore+sumNSimScore);
	}
}

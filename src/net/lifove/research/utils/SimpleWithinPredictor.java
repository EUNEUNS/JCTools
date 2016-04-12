package net.lifove.research.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.Utils;

public class SimpleWithinPredictor {
	/**
	 * @param args 0: data path, 1: label name, 2: positive label value, 3: negative label value
	 */
	public static void main(String[] args){
		new SimpleWithinPredictor().run(args);
	}
	
	public void run(String[] args){
		
		String dataPath = args[0];
		String labelName = args[1];
		String posLabel = args[2];
		String comment = args[3];
		String mlAlgorithm = args[4];
		String preprocessing = args[5];
		int numRepeat = Integer.parseInt(args[6]);
		int folds = Integer.parseInt(args[7]);
		
		// load sourceData
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(dataPath));
			Instances instances = new Instances(reader);
			reader.close();
			
			instances.setClass(instances.attribute(labelName));
		
			//Instances targetInstances =  WekaUtils.applyNormalize(instances);
			
			if(preprocessing.equals("FS")){
				File fileWithFS = new File(dataPath + "FS.arff");
				if(!fileWithFS.exists()){
					instances = WekaUtils.featrueSelectionByGainRatioAttributeEval(instances);
					WekaUtils.writeADataFile(instances, dataPath + "FS.arff");
				}
				else{
					reader = new BufferedReader(new FileReader(dataPath + "FS.arff"));
					instances = new Instances(reader);
					reader.close();
					instances.setClass(instances.attribute(labelName));
				}
			}else if(preprocessing.equals("PCA")){
				File fileWithFS = new File(dataPath + "PCA.arff");
				if(!fileWithFS.exists()){
					instances = WekaUtils.ApplyPCA(instances);
					WekaUtils.writeADataFile(instances, dataPath + "PCA.arff");
				}
				else{
					reader = new BufferedReader(new FileReader(dataPath + "PCA.arff"));
					instances = new Instances(reader);
					reader.close();
					instances.setClass(instances.attribute(labelName));
				}
			}
			
			int posClassValueIndex = WekaUtils.getClassValueIndex(instances, posLabel);
			//int negClassValueIndex = WekaUtils.getClassValueIndex(instances, negLabel);
			
			/*String[] mlAlgorithms = {"weka.classifiers.trees.J48",
					"weka.classifiers.trees.RandomForest",
					"weka.classifiers.bayes.BayesNet",
					"weka.classifiers.bayes.NaiveBayes",
					"weka.classifiers.functions.Logistic"};
			*/
			
			//for(int i=0;i<mlAlgorithms.length;i++){
			System.out.println("comment,ML,TP,FP,TN,FN,Precision,Recall,Fmeasure,FPR,AUC,MCC,numInstances");
			
			String strClassifier = mlAlgorithm;
			Classifier classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
				
			for(int repeat=0; repeat<numRepeat;repeat++){
				
				instances.randomize(new Random(repeat)); 
				instances.stratify(folds);
				Evaluation eval;
				
				for(int n=0;n<folds;n++){
					Instances trainingSet = instances.trainCV(folds, n);
					Instances testSet = instances.testCV(folds, n);

					Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
					clsCopy.buildClassifier(trainingSet);
					eval = new Evaluation(instances);
					eval.evaluateModel(clsCopy, testSet);
					
					//double accuracy = eval.pctCorrect()/100;
					double TP = eval.numTruePositives(posClassValueIndex);
					double FP = eval.numFalsePositives(posClassValueIndex);
					double TN = eval.numTrueNegatives(posClassValueIndex);
					double FN = eval.numFalseNegatives(posClassValueIndex);
					
					double precision = eval.precision(posClassValueIndex);
					double recall = eval.recall(posClassValueIndex);
					double fmeasure = eval.fMeasure(posClassValueIndex);
					//double precision = eval.precision(posClassValueIndex);
					//double recall = eval.recall(posClassValueIndex);
					double fpr = eval.falsePositiveRate(posClassValueIndex);
					double auc = eval.areaUnderROC(posClassValueIndex);
					double mcc = eval.matthewsCorrelationCoefficient(posClassValueIndex);
					
					Instances curve = WekaUtils.getCurve(eval.predictions(), posClassValueIndex);
					
					
					System.out.println(curve);
					
					FileUtil.print(WekaUtils.getPrecisionRecallFmeasureFromCurve(curve), 0);
					
					
					
					
					System.out.println(comment + "," + strClassifier + "," 
										+ TP + ","
										+ FP + ","
										+ TN + ","
										+ FN + ","
										+ precision + ","
										+ recall + ","
										+ fmeasure + ","
										+ fpr + ","
										+ auc + "," + mcc + "," + instances.numInstances());
				}
			}
			//}
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

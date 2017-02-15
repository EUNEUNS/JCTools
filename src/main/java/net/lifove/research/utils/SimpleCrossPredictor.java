package net.lifove.research.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import com.google.common.primitives.Doubles;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.Utils;

public class SimpleCrossPredictor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SimpleCrossPredictor().run(args);
	}

	private void run(String[] args) {
		String predictionInfo = args[0];
		String sourcePath = args[1];
		String targetPath = args[2];
		String classAttributeName = args[3];
		String posLabel = args[4];
		int repeat = Integer.parseInt(args[5]);
		int folds = Integer.parseInt(args[6]);
		Instances sourceInstances = WekaUtils.loadArff(sourcePath, classAttributeName);
		Instances targetInstances = WekaUtils.loadArff(targetPath, classAttributeName);
		boolean verbose=false;
		if(args.length==8)
			verbose = Boolean.parseBoolean(args[7]);
		
		WekaUtils.crossPredictionOnTheSameSplit(predictionInfo,
						sourceInstances, targetInstances, posLabel, repeat, folds, verbose,"");
	}
	
	public static Measure crossPrediction(Instances srcInstances, Instances tgtInstances, String mlAlgorithm, String posStrLabel){
		return crossPrediction(srcInstances,tgtInstances,mlAlgorithm,posStrLabel,false);
	}
	
	public static Measure crossPrediction(Instances srcInstances, Instances tgtInstances, String mlAlgorithm, String posStrLabel,boolean saveDetailedFoldResult){
		
		String result = "";
		int posClassValueIndex = WekaUtils.getClassValueIndex(tgtInstances, posStrLabel);
		Measure measure = new Measure();
		
		try {
			Classifier classifierForCross = (Classifier) Utils.forName(Classifier.class, mlAlgorithm, null);
			classifierForCross.buildClassifier(srcInstances);
			Evaluation eval = new Evaluation(srcInstances);
			eval.evaluateModel(classifierForCross, tgtInstances);
			
			if(saveDetailedFoldResult){
				System.out.println("=====");
				
				for(int predIdx=0; predIdx<eval.predictions().size();predIdx++){
					Prediction pred = (Prediction) eval.predictions().get(predIdx);
					System.out.println(predIdx + "," + (pred.actual()==posClassValueIndex?WekaUtils.strPos:WekaUtils.strNeg) + "," + (pred.predicted()==posClassValueIndex?WekaUtils.strPos:WekaUtils.strNeg));
				}
			}
			
			measure.addPrecision(eval.precision(posClassValueIndex));
			measure.addRecall(eval.recall(posClassValueIndex));
			measure.addFmeasure(eval.fMeasure(posClassValueIndex));
			measure.addAUC(eval.areaUnderROC(posClassValueIndex));
			measure.addFNR(eval.falseNegativeRate(posClassValueIndex));
			measure.addFPR(eval.falsePositiveRate(posClassValueIndex));
			measure.addTP((int)eval.numTruePositives(posClassValueIndex));
			measure.addFP((int)eval.numFalsePositives(posClassValueIndex));
			measure.addTN((int)eval.numTrueNegatives(posClassValueIndex));
			measure.addFN((int)eval.numFalseNegatives(posClassValueIndex));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return measure;
	}
	
	public static String crossPredictionOnTheSameSplit(Instances newSourceInstances,
			Instances targetInstances,Instances originalInstances,String posLabel,int repeat,int folds) {
		
		String mlAlgorithm = "weka.classifiers.functions.Logistic";
		String result ="";
		
		try {
			Classifier classifierForCross = (Classifier) Utils.forName(Classifier.class, mlAlgorithm, null);
			Classifier classifierForTargetWithin = (Classifier) Utils.forName(Classifier.class, mlAlgorithm, null);
			classifierForCross.buildClassifier(newSourceInstances);
			//Evaluation eval = new Evaluation(targetInstances);
			
			//eval.evaluateModel(classifier, targetInstances);
			
			int posClassIndex = WekaUtils.getClassValueIndex(newSourceInstances, posLabel);
			
			Instances tarData = new Instances(targetInstances);
			Instances originalTarData = new Instances(originalInstances);
			
			Measures wMeasures = new Measures();
			Measures cMeasures = new Measures();
			
			for(int i=0; i<repeat; i++){
				// randomize with different seed for each iteration
				originalTarData.randomize(new Random(i)); 
				originalTarData.stratify(folds);
				
				tarData.randomize(new Random(i)); 
				tarData.stratify(folds);
				Evaluation tarEval = new Evaluation(tarData);
				Evaluation crossEval = new Evaluation(newSourceInstances);
				for(int n=0;n<folds;n++){
					// for Within
					Instances orginalTarTrain = originalTarData.trainCV(folds, n);
					Instances orginalTarTest = originalTarData.testCV(folds, n);
					
					// for cross, we use tarTest from preprocessed datasets
					Instances tarTest = tarData.testCV(folds, n);
					
					Classifier clsCopy = AbstractClassifier.makeCopy(classifierForTargetWithin);
					clsCopy.buildClassifier(orginalTarTrain);
					tarEval.evaluateModel(clsCopy, orginalTarTest);
					
					Classifier clsCopyForCross = AbstractClassifier.makeCopy(classifierForCross);
					clsCopyForCross.buildClassifier(newSourceInstances);
					crossEval.evaluateModel(classifierForCross, tarTest);
					
					wMeasures.getPrecisions().add(tarEval.precision(posClassIndex));
					wMeasures.getRecalls().add(tarEval.recall(posClassIndex));
					wMeasures.getFmeasures().add(tarEval.fMeasure(posClassIndex));
					wMeasures.getAUCs().add(tarEval.areaUnderROC(posClassIndex));
					wMeasures.getMCCs().add(tarEval.matthewsCorrelationCoefficient(posClassIndex));
					wMeasures.getAUPRCs().add(tarEval.areaUnderPRC(posClassIndex));
					
					cMeasures.getPrecisions().add(crossEval.precision(posClassIndex));
					cMeasures.getRecalls().add(crossEval.recall(posClassIndex));
					cMeasures.getFmeasures().add(crossEval.fMeasure(posClassIndex));
					cMeasures.getAUCs().add(crossEval.areaUnderROC(posClassIndex));
					cMeasures.getMCCs().add(crossEval.matthewsCorrelationCoefficient(posClassIndex));
					cMeasures.getAUPRCs().add(crossEval.areaUnderPRC(posClassIndex));
				}
			}
			
			DecimalFormat df = new DecimalFormat("0.0");
			
			String pcntNewInstances = df.format((double)(newSourceInstances.numInstances()*100)/targetInstances.numInstances());
			
			result =  WekaUtils.getNumInstancesByClass(newSourceInstances,posLabel) + "," +
											(newSourceInstances.numInstances()-WekaUtils.getNumInstancesByClass(newSourceInstances,posLabel)) + "," + 
											newSourceInstances.numInstances() + "," + 
											pcntNewInstances + "%," + 
											WekaUtils.getNumInstancesByClass(targetInstances,posLabel) + "," +
											(targetInstances.numInstances()-WekaUtils.getNumInstancesByClass(targetInstances,posLabel)) + "," + 
											targetInstances.numInstances() + "," + 
											newSourceInstances.numAttributes() + "," + 
											originalInstances.numAttributes() + "," + 
											getWinTieLoss(wMeasures.getPrecisions(),cMeasures.getPrecisions()) + "," +
											getWinTieLoss(wMeasures.getRecalls(),cMeasures.getRecalls()) + "," +
											getWinTieLoss(wMeasures.getFmeasures(),cMeasures.getFmeasures()) + "," +
											getWinTieLoss(wMeasures.getAUCs(),cMeasures.getAUCs()) + "," +
											getWinTieLoss(wMeasures.getMCCs(),cMeasures.getMCCs()) + "," +
											getWinTieLoss(wMeasures.getAUPRCs(),cMeasures.getAUPRCs());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String getWinTieLoss(ArrayList<Double> sample1, ArrayList<Double> sample2){		
		return WekaUtils.getWinTieLoss(sample1, sample2);
	}
}

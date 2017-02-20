package net.lifove.research.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.core.Instances;

public class CrossPredictionWithIFSDatasets {
	
	String mlAlg = "";
	int threadPoolSize=4;
	HashMap<String,ArrayList<String>> existingIFSPreidctions = new HashMap<String,ArrayList<String>>(); // key: source>>target, value: line

	public static void main(String[] args) {
		new CrossPredictionWithIFSDatasets().run(args);
	}

	private void run(String[] args) {
		
		String pathRoot = args[0];
		mlAlg = args[1];
		threadPoolSize = args[2]!=null?Integer.parseInt(args[2]):threadPoolSize;
		String pathForExistingIFSResults = args[3];
		existingIFSPreidctions = pathForExistingIFSResults != null? getExistingIFS(pathForExistingIFSResults):existingIFSPreidctions;
		
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo(pathRoot + "data/AEEEM/", "class", "buggy", AEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo(pathRoot + "data/ReLink/", "isDefective", "TRUE", ReLink);

		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		ProjectGroupInfo projectGroupPROMISE = new ProjectGroupInfo(pathRoot + "data/CK/", "label", "buggy", PROMISE);
		
		String[] NASA = {"CM1","MW1","PC1","PC3","PC4"};
		ProjectGroupInfo projectGroupNASA = new ProjectGroupInfo(pathRoot + "data/NASA/", "Defective", "Y", NASA);
		
		String[] NASA2 = {"JM1"};
		ProjectGroupInfo projectGroupNASA2 = new ProjectGroupInfo(pathRoot + "data/NASA/", "Defective", "Y", NASA2);
		
		String[] NASA3 = {"PC2"};
		ProjectGroupInfo projectGroupNASA3 = new ProjectGroupInfo(pathRoot + "data/NASA/", "Defective", "Y", NASA3);
		
		String[] NASA4 = {"PC5","MC1"};
		ProjectGroupInfo projectGroupNASA4 = new ProjectGroupInfo(pathRoot + "data/NASA/", "Defective", "Y", NASA4);
		
		String[] NASA5 = {"MC2","KC3"};
		ProjectGroupInfo projectGroupNASA5 = new ProjectGroupInfo(pathRoot + "data/NASA/", "Defective", "Y", NASA5);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo(pathRoot + "data/SOFTLAB/", "defects", "true", SOFTLAB);
		
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupSOFTLAB,mlAlg,existingIFSPreidctions));
		
		
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupAEEEM,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupRelink,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupPROMISE,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA5,mlAlg,existingIFSPreidctions));
		
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA2,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA3,mlAlg,existingIFSPreidctions));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA4,mlAlg,existingIFSPreidctions));
		
		executor.shutdown();
		
		while (!executor.isTerminated()) {
			// waiting
        }
	}

	private HashMap<String, ArrayList<String>> getExistingIFS(String pathForExistingIFSResults) {
		
		HashMap<String,ArrayList<String>> mapIFSPreidictions = new HashMap<String,ArrayList<String>>(); // key: source>>target value: line
		ArrayList<String> lines = FileUtil.getLines(pathForExistingIFSResults,false);
		
		for(String line:lines){
			// exmple line: D,EQ>>ar1,324,267.8%,121,0.0,0.2857142857142857,-,0.5,0.6839285714285714,-,0.0,0.22939923264149759,-,0.08196721311475409,0.14528688524590164,-
			String[] splitLine = line.split(",");
			String key = splitLine[1];
			
			if(mapIFSPreidictions.containsKey(key)){
				mapIFSPreidictions.get(key).add(line);		
			}else{
				ArrayList<String> lstLines = new ArrayList<String>();
				mapIFSPreidictions.put(key,lstLines);
				lstLines.add(line);
			}
		}
		
		return mapIFSPreidictions;
	}
}

class BatchRunner implements Runnable{
	
	ProjectGroupInfo sourceGroup;
	ProjectGroupInfo targetGroup;
	String mlAlg;
	final HashMap<String,ArrayList<String>> existingIFSPreidctions;
	
	public BatchRunner(ProjectGroupInfo srcGroup,ProjectGroupInfo tarGroup,String mlAlg,HashMap<String,ArrayList<String>> existingIFS){
		sourceGroup = srcGroup;
		targetGroup = tarGroup;
		this.mlAlg = mlAlg;
		existingIFSPreidctions = existingIFS;
	}

	@Override
	public void run() {
		batchRunner(sourceGroup,targetGroup,existingIFSPreidctions);
	}
	
	private void batchRunner(ProjectGroupInfo sourceGroup,ProjectGroupInfo targetGroup,HashMap<String,ArrayList<String>> existingIFSPreidctions) {
		
		for(String source:sourceGroup.projects){
			for(String target:targetGroup.projects){
				
				String predictionInfo = source + ">>" + target;
				String sourcePath = sourceGroup.dirPath + "ifs_" + source +".arff";
				String targetPath = targetGroup.dirPath + "ifs_" + target +".arff";
				String classAttributeName = WekaUtils.labelName;
				String posLabel = WekaUtils.strPos;
				int repeat = 500;
				int folds = 2;
				
				if((!existingIFSPreidctions.containsKey(predictionInfo) && existingIFSPreidctions.get(predictionInfo).size()!=((repeat*folds)+1))){
				
					Instances sourceInstances = WekaUtils.loadArff(sourcePath, classAttributeName);
					Instances targetInstances = WekaUtils.loadArff(targetPath, classAttributeName);
					
					WekaUtils.crossPredictionOnTheSameSplit(predictionInfo,
							sourceInstances, targetInstances, posLabel, repeat, folds,mlAlg);
				}else{
					for(String line:existingIFSPreidctions.get(predictionInfo)){
						System.out.println(line);
					}
				}

			}
		}
	}
}

class ProjectGroupInfo{
	String dirPath;
	String labelName;
	String posLabel;
	String[] projects;
	
	ProjectGroupInfo(String dirPath,String labelName,String posLabel,String[] projects){
		this.dirPath = dirPath;
		this.labelName = labelName;
		this.posLabel = posLabel;
		this.projects = projects;
	}
}

package net.lifove.research.utils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.core.Instances;

public class CrossPredictionWithIFSDatasets {
	
	String mlAlg = "";
	int threadPoolSize=4;

	public static void main(String[] args) {
		new CrossPredictionWithIFSDatasets().run(args);
	}

	private void run(String[] args) {
		
		String pathRoot = args[0];
		mlAlg = args[1];
		threadPoolSize = args[2]!=null?Integer.parseInt(args[2]):threadPoolSize;
		
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
		
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupNASA5,mlAlg));
		executor.execute(new BatchRunner(projectGroupAEEEM,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupNASA5,mlAlg));
		executor.execute(new BatchRunner(projectGroupRelink,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupNASA5,mlAlg));
		executor.execute(new BatchRunner(projectGroupPROMISE,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupSOFTLAB,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupSOFTLAB,mlAlg));
		
		
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupAEEEM,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupRelink,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupPROMISE,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupSOFTLAB,projectGroupNASA5,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA,projectGroupNASA5,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA2,projectGroupNASA5,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA4,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA3,projectGroupNASA5,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA4,projectGroupNASA5,mlAlg));
		
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA2,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA3,mlAlg));
		executor.execute(new BatchRunner(projectGroupNASA5,projectGroupNASA4,mlAlg));
		
		executor.shutdown();
		
		while (!executor.isTerminated()) {
			// waiting
        }
	}
}

class BatchRunner implements Runnable{
	
	ProjectGroupInfo sourceGroup;
	ProjectGroupInfo targetGroup;
	String mlAlg;
	
	public BatchRunner(ProjectGroupInfo srcGroup,ProjectGroupInfo tarGroup,String mlAlg){
		sourceGroup = srcGroup;
		targetGroup = tarGroup;
		this.mlAlg = mlAlg;
	}

	@Override
	public void run() {
		batchRunner(sourceGroup,targetGroup);
	}
	
	private void batchRunner(ProjectGroupInfo sourceGroup,ProjectGroupInfo targetGroup) {
		
		for(String source:sourceGroup.projects){
			for(String target:targetGroup.projects){
				String predictionInfo = source + ">>" + target;
				String sourcePath = sourceGroup.dirPath + "ifs_" + source +".arff";
				String targetPath = targetGroup.dirPath + "ifs_" + target +".arff";
				String classAttributeName = WekaUtils.labelName;
				String posLabel = WekaUtils.strPos;
				int repeat = 500;
				int folds = 2;
				Instances sourceInstances = WekaUtils.loadArff(sourcePath, classAttributeName);
				Instances targetInstances = WekaUtils.loadArff(targetPath, classAttributeName);
				
				WekaUtils.crossPredictionOnTheSameSplit(predictionInfo,
						sourceInstances, targetInstances, posLabel, repeat, folds,mlAlg);

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

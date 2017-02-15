package net.lifove.research.utils;

import weka.core.Instances;

public class CrossPredictionWithIFSDatasets {
	
	String mlAlg = "";

	public static void main(String[] args) {
		new CrossPredictionWithIFSDatasets().run(args);
	}

	private void run(String[] args) {
		
		String pathRoot = args[0];
		mlAlg = args[1];
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo(pathRoot + "data/AEEEM/", "class", "buggy", AEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo(pathRoot + "data/Relink/", "isDefective", "TRUE", ReLink);

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
		
		
		batchRunner(projectGroupAEEEM,projectGroupRelink);
		batchRunner(projectGroupAEEEM,projectGroupPROMISE);
		batchRunner(projectGroupAEEEM,projectGroupNASA);
		batchRunner(projectGroupAEEEM,projectGroupNASA2);
		batchRunner(projectGroupAEEEM,projectGroupNASA3);
		batchRunner(projectGroupAEEEM,projectGroupNASA4);
		batchRunner(projectGroupAEEEM,projectGroupNASA5);
		batchRunner(projectGroupAEEEM,projectGroupSOFTLAB);
		
		batchRunner(projectGroupRelink,projectGroupAEEEM);
		batchRunner(projectGroupRelink,projectGroupPROMISE);
		batchRunner(projectGroupRelink,projectGroupNASA);
		batchRunner(projectGroupRelink,projectGroupNASA2);
		batchRunner(projectGroupRelink,projectGroupNASA3);
		batchRunner(projectGroupRelink,projectGroupNASA4);
		batchRunner(projectGroupRelink,projectGroupNASA5);
		batchRunner(projectGroupRelink,projectGroupSOFTLAB);
		
		batchRunner(projectGroupPROMISE,projectGroupAEEEM);
		batchRunner(projectGroupPROMISE,projectGroupRelink);
		batchRunner(projectGroupPROMISE,projectGroupNASA);
		batchRunner(projectGroupPROMISE,projectGroupNASA2);
		batchRunner(projectGroupPROMISE,projectGroupNASA3);
		batchRunner(projectGroupPROMISE,projectGroupNASA4);
		batchRunner(projectGroupPROMISE,projectGroupNASA5);
		batchRunner(projectGroupPROMISE,projectGroupSOFTLAB);
		
		batchRunner(projectGroupNASA,projectGroupAEEEM);
		batchRunner(projectGroupNASA,projectGroupRelink);
		batchRunner(projectGroupNASA,projectGroupPROMISE);
		batchRunner(projectGroupNASA,projectGroupSOFTLAB);
		
		batchRunner(projectGroupNASA2,projectGroupAEEEM);
		batchRunner(projectGroupNASA2,projectGroupRelink);
		batchRunner(projectGroupNASA2,projectGroupPROMISE);
		batchRunner(projectGroupNASA2,projectGroupSOFTLAB);
		
		batchRunner(projectGroupNASA3,projectGroupAEEEM);
		batchRunner(projectGroupNASA3,projectGroupRelink);
		batchRunner(projectGroupNASA3,projectGroupPROMISE);
		batchRunner(projectGroupNASA3,projectGroupSOFTLAB);
		
		batchRunner(projectGroupNASA4,projectGroupAEEEM);
		batchRunner(projectGroupNASA4,projectGroupRelink);
		batchRunner(projectGroupNASA4,projectGroupPROMISE);
		batchRunner(projectGroupNASA4,projectGroupSOFTLAB);
		
		batchRunner(projectGroupNASA5,projectGroupAEEEM);
		batchRunner(projectGroupNASA5,projectGroupRelink);
		batchRunner(projectGroupNASA5,projectGroupPROMISE);
		batchRunner(projectGroupNASA5,projectGroupSOFTLAB);
		
		
		batchRunner(projectGroupSOFTLAB,projectGroupAEEEM);
		batchRunner(projectGroupSOFTLAB,projectGroupRelink);
		batchRunner(projectGroupSOFTLAB,projectGroupPROMISE);
		batchRunner(projectGroupSOFTLAB,projectGroupNASA);
		batchRunner(projectGroupSOFTLAB,projectGroupNASA2);
		batchRunner(projectGroupSOFTLAB,projectGroupNASA3);
		batchRunner(projectGroupSOFTLAB,projectGroupNASA4);
		batchRunner(projectGroupSOFTLAB,projectGroupNASA5);
		
		batchRunner(projectGroupNASA,projectGroupNASA2);
		batchRunner(projectGroupNASA,projectGroupNASA3);
		batchRunner(projectGroupNASA,projectGroupNASA4);
		batchRunner(projectGroupNASA,projectGroupNASA5);
		
		batchRunner(projectGroupNASA2,projectGroupNASA);
		batchRunner(projectGroupNASA2,projectGroupNASA3);
		batchRunner(projectGroupNASA2,projectGroupNASA4);
		batchRunner(projectGroupNASA2,projectGroupNASA5);
		
		batchRunner(projectGroupNASA3,projectGroupNASA);
		batchRunner(projectGroupNASA3,projectGroupNASA2);
		batchRunner(projectGroupNASA3,projectGroupNASA4);
		batchRunner(projectGroupNASA3,projectGroupNASA5);
		
		batchRunner(projectGroupNASA4,projectGroupNASA);
		batchRunner(projectGroupNASA4,projectGroupNASA2);
		batchRunner(projectGroupNASA4,projectGroupNASA3);
		batchRunner(projectGroupNASA4,projectGroupNASA5);
		
		batchRunner(projectGroupNASA5,projectGroupNASA);
		batchRunner(projectGroupNASA5,projectGroupNASA2);
		batchRunner(projectGroupNASA5,projectGroupNASA3);
		batchRunner(projectGroupNASA5,projectGroupNASA4);
		
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

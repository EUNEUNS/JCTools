package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.MatrixUtil;

import org.junit.Test;

public class IFSTransformerTest {

	@Test
	public void testIFSTransformer() {
		
		String [] projects = {
				"ReLink/Safe.arff",
				"ReLink/Apache.arff",
				"ReLink/Zxing.arff",
				"NASA/MC2.arff", // new
				"NASA/PC5.arff", // new
				"NASA/PC1.arff",
				"NASA/PC2.arff", // new
				"NASA/JM1.arff", // new
				"NASA/PC4.arff",
				"NASA/KC3.arff", // new
				"NASA/PC3.arff",
				"NASA/MW1.arff",
				"NASA/CM1.arff",
				"NASA/MC1.arff", // new
				"SOFTLAB/ar5.arff",
				"SOFTLAB/ar3.arff",
				"SOFTLAB/ar4.arff",
				"SOFTLAB/ar1.arff",
				"SOFTLAB/ar6.arff",
				"CK/ant-1.3.arff",
				"CK/arc.arff",
				"CK/camel-1.0.arff",
				"CK/poi-1.5.arff",
				"CK/redaktor.arff",
				"CK/skarbonka.arff",
				"CK/tomcat.arff",
				"CK/velocity-1.4.arff",
				"CK/xalan-2.4.arff",
				"CK/xerces-1.2.arff",
				"AEEEM/PDE.arff",
				"AEEEM/EQ.arff",
				"AEEEM/LC.arff",
				"AEEEM/JDT.arff",
				"AEEEM/ML.arff"
		};
		
		String pathRoot = System.getProperty("user.home") + "/Documents/UW/HDP+/data/"; 
		
		for(String project:projects){
			String[] args = new String[4];
			args[0] = pathRoot + project.split("/")[0]; // path
			args[1] = project.split("/")[1]; // arffName
			
			String[] labelInfo = getLabelInfo(project);
			args[2] = labelInfo[0]; // className
			args[3] = labelInfo[1]; // strBuggyLabel
			
			IFSTransformer.main(args);
		
		}
		
	}
	
	private String[] getLabelInfo(String path) {
		
		String[] labelInfo = new String[2];
		
		String group = path.substring(0, path.indexOf("/"));
		
		if(group.equals("ReLink")){
			labelInfo[0] = "isDefective";
			labelInfo[1] = "TRUE";
		}
		
		if(group.equals("NASA")){
			labelInfo[0] = "Defective";
			labelInfo[1] = "Y";
		}
		
		if(group.equals("AEEEM")){
			labelInfo[0] = "class";
			labelInfo[1] = "buggy";
		}
		
		if(group.equals("SOFTLAB")){
			labelInfo[0] = "defects";
			labelInfo[1] = "true";
		}
		
		if(group.equals("CK")){
			labelInfo[0] = "bug"; // bug or class
			labelInfo[1] = "buggy";
		}
		
		return labelInfo;
	}
}

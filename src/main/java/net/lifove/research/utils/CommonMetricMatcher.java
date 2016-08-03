package net.lifove.research.utils;

import java.util.ArrayList;

import weka.core.Instances;

public class CommonMetricMatcher {

	public static void main(String[] args) {
		CommonMetricMatcher.match(args);
	}

	public static void match(String[] args) {
		String groupName1 = args[0];
		String pathForArff1 = args[1];
		String groupName2 = args[2];
		String pathForArff2 = args[3];
		
		Instances instances1 = WekaUtils.loadArff(pathForArff1);
		Instances instances2 = WekaUtils.loadArff(pathForArff2);
		
		ArrayList<String> commonAttrNames = new ArrayList<String>();
		
		for(int attrIdxArff1=0;attrIdxArff1<instances1.numAttributes()-1;attrIdxArff1++){
			for(int attrIdxArff2=0;attrIdxArff2<instances2.numAttributes()-1;attrIdxArff2++){
				String attrName1 = instances1.attribute(attrIdxArff1).name();
				String attrName2 = instances2.attribute(attrIdxArff2).name();
				if(attrName1.equals(attrName2))
					commonAttrNames.add(attrName1);
			}
		}
		
		String strCommonAttrs = "";
		for(String attrName:commonAttrNames)
			strCommonAttrs += attrName + ">>" + attrName +"|";
		
		System.out.println(groupName1 + ">>" + groupName2 + ":" + strCommonAttrs);
		
	}

}

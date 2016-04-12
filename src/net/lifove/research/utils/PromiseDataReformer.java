package net.lifove.research.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class PromiseDataReformer {

	String path = "Datasets/Promise";
	
	String[] targetStrings ={"@attribute defects {false,true}","@attribute problems {no,yes}","@attribute Faulty6_1 {0, 1}","@attribute Defective {Y,N}","@attribute class{buggy,clean}"};
	String replaceTo = "@attribute class {buggy,clean}";
	
	/**
	 * @param args 0: list of target directories. a name of a csv file is same as the directory name
	 *             1: relative path Datasets/Promise
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new PromiseDataReformer().run(args);
	}
	
	void run(String[] args){
		
		if(args.length >=2)
			path = args[1];
		
		// load a list of target files
		ArrayList<String> names = FileUtil.getLines(args[0], false);
		
		for(String name:names){
			String filePath = name +"/" + name+".arff";
			ArrayList<String> arffHeaderLines = FileUtil.getArffHeaderLines(filePath);
			ArrayList<String> arffBodyLines = FileUtil.getArffBodyLines(filePath);

			for(int i=0; i<arffHeaderLines.size();i++){
				if (targetStringExist(arffHeaderLines.get(i)))
					arffHeaderLines.set(i, replaceTo);
			}
			
			for(int i=0; i<arffBodyLines.size();i++){
				arffBodyLines.set(i, labelChanged(arffBodyLines.get(i)));
			}
			
			arffHeaderLines.addAll(arffBodyLines);
			FileUtil.writeAFile(arffHeaderLines, filePath);
		}
	}
	
	String labelChanged(String line){
		
		String values[] = line.split(",");
		int lastIndex = values.length-1;
		String label = values[lastIndex];
		if(label.equals("true") ||  label.equals("yes") || label.equals("1") || label.equals("Y"))
			values[lastIndex] = "buggy";
		else
			values[lastIndex] = "clean";
		
		return ArrayListUtil.strArrayListToString(new ArrayList<String>(Arrays.asList(values)));
	}
	
	boolean targetStringExist(String line){
		for(String targetString:targetStrings)
			if (line.equals(targetString))
				return true;
		
		return false;
	}
	
}

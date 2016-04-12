package net.lifove.research.utils;

import java.util.ArrayList;

import net.lifove.research.utils.FileUtil;

public class ArffReformer {

	String arffFile = "";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ArffReformer().run(args);
	}
	
	void run(String[] args){
		arffFile = args[0];
		ArrayList<String> arffHeaderLines;
		ArrayList<String> arffBodyLines;
		
		arffHeaderLines = FileUtil.getArffHeaderLines(arffFile);
		arffBodyLines = FileUtil.getArffBodyLines(arffFile);
		
		arffHeaderLines.remove(getTargetIndexToRemove(arffHeaderLines,"@attribute project_name {"));
		arffHeaderLines.remove(getTargetIndexToRemove(arffHeaderLines,"@attribute version"));
		arffHeaderLines.remove(getTargetIndexToRemove(arffHeaderLines,"@attribute name {"));
		
		ArrayListUtil.removeColumn(arffBodyLines,2);
		ArrayListUtil.removeColumn(arffBodyLines,1);
		ArrayListUtil.removeColumn(arffBodyLines,0);
		// change label values
		arffBodyLines = changeLabelForClassfication(arffBodyLines,20);
		
		// change label attribute
		arffHeaderLines.remove("@attribute bug numeric");
		arffHeaderLines.remove("@data");
		arffHeaderLines.add("@ATTRIBUTE class {buggy,clean}");
		arffHeaderLines.add("@data");
		
		// merge all lines together
		arffHeaderLines.addAll(arffBodyLines);
		
		FileUtil.writeAFile(arffHeaderLines, arffFile + "_reformed.arff");
	}
	
	ArrayList<String> changeLabelForClassfication(ArrayList<String> lines,int targetIndex){		
		ArrayList<String> newClasses = new ArrayList<String>();
		ArrayList<String> newLines;
		
		for(String line:lines){
			String[] values = line.split(",");
			int targetValue = Integer.parseInt(values[targetIndex].trim());
			
			if (targetValue > 0)
				values[targetIndex] = "buggy";
			else
				values[targetIndex] = "clean";
			
			newClasses.add(values[targetIndex]);
		}
		
		ArrayListUtil.removeColumn(lines, targetIndex);
		newLines = ArrayListUtil.mergeSameLengthArrayList(lines, newClasses);
		
		return newLines;
	}
	
	int getTargetIndexToRemove(ArrayList<String> lines,String startsWith){
		
		for(int i=0; i<lines.size();i++){
			if(lines.get(i).startsWith(startsWith))
				return i;
		}
		
		System.out.println(arffFile + " no such line : " + startsWith);
		return -1;
	}
}

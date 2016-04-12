package net.lifove.research.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.csvreader.CsvReader;

public class CSVMerger {

	public static void main(String[] args) {
		new CSVMerger().run(args);
	}

	enum MergeType{INNER,OUTERLEFT,OUTERRIGHT};
	public void run(String[] args) {
		String srouceCSVPath = args[0];
		String sourceKeyName = args[1];
		String targetCSVPath = args[2];
		String targetKeyName = args[3];
		String newCSVPath = args[4];
		MergeType mergeType = MergeType.valueOf(args[5]);
		
		CsvReader sourceData,targetData;
		try {
			sourceData = new CsvReader(srouceCSVPath);
			targetData = new CsvReader(targetCSVPath);
			
			sourceData.readHeaders();
			targetData.readHeaders();
			
			HashMap<String,String> sourceMap = new HashMap<String,String>();
			HashMap<String,String> targetMap = new HashMap<String,String>();
			
			int numValuesOfTarget = targetData.getHeaders().length;
			
			while (sourceData.readRecord()){
				String key = sourceData.get(sourceKeyName);
				String value = sourceData.getRawRecord().replace(",NA", ",?");

				sourceMap.put(key, value);
			}
			
			while (targetData.readRecord()){
				String key = targetData.get(targetKeyName);
				String value = targetData.getRawRecord().replace(",NA", ",?");

				targetMap.put(key, value);
			}
			
			HashMap<String,String> smallMap = null;
			HashMap<String,String> bigMap = null;
			if(sourceMap.size() < targetMap.size()){
				smallMap = sourceMap;
				bigMap = targetMap;
			}else{
				smallMap = targetMap;
				bigMap = sourceMap;
			}
			
			ArrayList<String> mergedLines = new ArrayList<String>();
			mergedLines.add(getCommnaSeperatedString(sourceData.getHeaders()) + "," +
					getCommnaSeperatedString(targetData.getHeaders()));
			System.out.println(mergedLines.get(0));
			
			if(mergeType==MergeType.INNER){
				for(String key:smallMap.keySet()){
					if(bigMap.containsKey(key)){
						mergedLines.add(sourceMap.get(key) + "," + targetMap.get(key));
					}
					
				}
			}else{
				
				for(String key:sourceMap.keySet()){
					if(targetMap.containsKey(key)){
						mergedLines.add(sourceMap.get(key) + "," + targetMap.get(key));
					}
					else{
						mergedLines.add(sourceMap.get(key) + "," + getCommas(numValuesOfTarget));
					}
				}
			}
			
			/*for(String line:mergedLines){
				System.out.println(line);
			}*/
			
			FileUtil.writeAFile(mergedLines, newCSVPath);
			
			System.out.println("source line #: " + sourceMap.size());
			System.out.println("target line #: " + targetMap.size());
			System.out.println("new line #: " + mergedLines.size());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private String getCommas(int numValuesOfTarget) {
		String commas = "";
		
		for(int i=0;i<numValuesOfTarget-1;i++)
			commas += ",";
		
		commas += "0";
		return commas;
	}

	private String getCommnaSeperatedString(String[] values){
		String line = "";
		
		int i=0;
		for(String value:values){
			line = line + value;
			i++;
			
			if(i!=values.length)
				line += ",";
			
		}
		
		return line;
	}

}

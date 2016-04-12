package net.lifove.research.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.csvreader.CsvReader;

public class MetricsTestResultsMatcher {
	
	HashMap<String,String[]> metricsValues = new HashMap<String,String[]>();
	ArrayList<String> headers = new ArrayList<String>();
	HashMap<String,String[]> testResultsValues = new HashMap<String,String[]>();

	/**
	 * @param args [0] metrics file path [1] test results file
	 */
	public static void main(String[] args) {
		new MetricsTestResultsMatcher().run(args);
	}
	
	private void run(String[] args) {
		String metricsDataPath = args[0];
		String testResultsDataPath = args[1];
		String souceCheckOutDate = args[2];
		String pathToSave = args[3];
		
		System.out.println("Source checkout date: " + souceCheckOutDate);
		
		String[] splitDate = souceCheckOutDate.split("-");
		String ddmmyyyyDate = splitDate[2] + "/" + splitDate[1] + "/" + splitDate[0];
		
		CsvReader metrics;
		String[] metricsHeaders=null;
		CsvReader testResults;
		try {
			testResults = new CsvReader(testResultsDataPath);
			testResults.readHeaders();		
			
			while (testResults.readRecord()){
				
				//only consider the target date
				if(!testResults.get("date").equals(DateUtil.getNextDate(ddmmyyyyDate,"d/M/yyyy","d/M/yy")))
					continue;
				String key = testResults.get(0);
				
				if(testResultsValues.containsKey(key)){
					if(testResultsValues.get(key)[1].indexOf("Fail")==-1 && testResults.get(4).indexOf("Fail")>=0){
						String[] values = {key,"Buggy"};
						testResultsValues.put(key, values);
					}
				}
				else{
					if(testResults.get(4).indexOf("Fail")>=0){
						String[] values = {key,"Buggy"};
						testResultsValues.put(key, values);
					}
					else{
						String[] values = {key,"Clean"};
						testResultsValues.put(key, values);
					}
				}
			}
			
			System.out.println("# of APIs from test results : " + testResultsValues.size());
			
			metrics = new CsvReader(metricsDataPath);
			metrics.readHeaders();
			metricsHeaders = metrics.getHeaders();
			String[] values;
			
			while (metrics.readRecord()){
				values = metrics.getValues();
				
				String key = metrics.get(1);
				key = key.startsWith("_")?key.substring(1):key;
				
				key = key.indexOf("(")==-1? key:key.substring(0, key.indexOf("("));	
				String kind = metrics.get(0);
				
				if(kind.equals("Struct"))
					continue;
				
				if(testResultsValues.containsKey(key))
					metricsValues.put(key, values);
			}
			
			System.out.println("# of APIs from source code: " + metricsValues.size());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String key:testResultsValues.keySet()){
			if(!metricsValues.containsKey(key))
				System.out.println("nonexist APIs in cm: " + key);
		}
		
		
		ArrayList<String> lines = new ArrayList<String>();
		
		String header="AltCountLineBlank,AltCountLineCode,AltCountLineComment," +
				"CountInput,CountLine,CountLineBlank,CountLineCode,CountLineCodeDecl," +
				"CountLineCodeExe,CountLineComment,CountLineInactive,CountLinePreprocessor," +
				"CountOutput,CountPath,CountSemicolon,CountStmt,CountStmtDecl,CountStmtEmpty," +
				"CountStmtExe,Cyclomatic,CyclomaticModified,CyclomaticStrict," +
				"Essential,Knots,MaxEssentialKnots,MaxNesting,MinEssentialKnots,RatioCommentToCode,Class";
		
		lines.add(header);
		String[] metricsName = header.split(",");
		
		for(String key:metricsValues.keySet()){
			String newLine = "";
			for(int i=0;i<metricsName.length-1;i++){
				if (metricsValues.get(key)[ArrayListUtil.getMetricIndex(metricsName[i], metricsHeaders)].equals("")){
					System.out.println("corrupted data in " + key);
					System.exit(0);
				}
				newLine += metricsValues.get(key)[ArrayListUtil.getMetricIndex(metricsName[i], metricsHeaders)] + ",";
			}
			
			newLine += testResultsValues.get(key)[1];
			
			lines.add(newLine);
		}
		
		System.out.println("# of matched APIs: " + metricsValues.size());
		
		FileUtil.writeAFile(lines, pathToSave + File.separator +  souceCheckOutDate + "_labeled.csv");
	}
}

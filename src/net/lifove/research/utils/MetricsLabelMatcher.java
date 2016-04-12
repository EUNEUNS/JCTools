package net.lifove.research.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import weka.core.Instances;

import com.csvreader.CsvReader;

public class MetricsLabelMatcher {
	
	HashMap<String,String[]> metricsValues = new HashMap<String,String[]>();
	ArrayList<String> headers = new ArrayList<String>();
	HashMap<String,String[]> testResultsValues = new HashMap<String,String[]>();

	/**
	 * @param args [0] metrics file path [1] test results file
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		new MetricsLabelMatcher().run(args);
	}
	
	private void run(String[] args) throws ParseException {
		String metricsDataPath = args[0];
		// TODO set the format of label data
		String labelDataPath = args[1]; // api_name,date,module_name,testcase_name,testcase_execution_status,TC
		String souceCheckOutDate = args[2];
		String predictionDate = args[3];
		String pathToSave = args[4];
		String apiListPath = args[5];
		boolean isTestSetGeneration = souceCheckOutDate.equals(predictionDate)?true:false;
		
		System.out.println("Source checkout date: " + souceCheckOutDate);
		
		String dateFormat = "d/M/yyyy";
		String[] splitModelDate = souceCheckOutDate.split("-");
		String ddmmyyyyModelDate = splitModelDate[2] + "/" + splitModelDate[1] + "/" + splitModelDate[0];
		Date sDate = new SimpleDateFormat(dateFormat).parse(ddmmyyyyModelDate); 
		String[] splitPredictionDate = predictionDate.split("-");
		String ddmmyyyyEndDate = splitPredictionDate[2] + "/" + splitPredictionDate[1] + "/" + splitPredictionDate[0];
		Date eDate = new SimpleDateFormat(dateFormat).parse(ddmmyyyyEndDate); 
		String ddmmyyyyNextDateOfEdate = DateUtil.getNextDate(ddmmyyyyEndDate, dateFormat, dateFormat);
		
		CsvReader metrics;
		String[] metricsHeaders=null;
		CsvReader testResults;
		try {
			testResults = new CsvReader(labelDataPath);
			testResults.readHeaders();		
			
			while (testResults.readRecord()){
				Date dateInLabelData = new SimpleDateFormat("d/M/yy").parse(testResults.get("date"));
				
				//to label, only consider between model date and prediction date (both days exclusive)
				// compareTo
				// the value 0 if the argument Date is equal to this Date;
				// a value less than 0 if this Date is before the Date argument;
				// and a value greater than 0 if this Date is after the Date argument.
				if(!isTestSetGeneration && (sDate.compareTo(dateInLabelData)>0 || eDate.compareTo(dateInLabelData)<=0))
					continue;
				
				// for test set, only consider the next date of the designated date for prediction
				if(isTestSetGeneration && eDate.compareTo(dateInLabelData)!=0)
					continue;
				
				String key = testResults.get(0);
				
				if(testResultsValues.containsKey(key)){
					if(testResultsValues.get(key)[1].indexOf("Fail")==-1 && testResults.get(4).indexOf("Fail")>=0){
						String[] values = {key,WekaUtils.strPos};
						testResultsValues.put(key, values);
					}
				}
				else{
					if(testResults.get(4).indexOf("Fail")>=0){
						String[] values = {key,WekaUtils.strPos};
						testResultsValues.put(key, values);
					}
					else{
						String[] values = {key,WekaUtils.strNeg};
						testResultsValues.put(key, values);
					}
				}
			}
			
			System.out.println("# of APIs from test results : " + testResultsValues.size());
			
			// no label data for test set
			// then generate ? labeled instances based on api_list.txt
			if(isTestSetGeneration && testResultsValues.size()==0){
				ArrayList<String> apis = FileUtil.getLines(apiListPath, false);
				for(String apiName:apis){
					String key=apiName;
					String[] values = {key,"?"};
					testResultsValues.put(key, values);
				}
			}
			
			metrics = new CsvReader(metricsDataPath);
			metrics.readHeaders();
			metricsHeaders = metrics.getHeaders();
			String[] values;
			
			// find apis matched with apis in label data
			while (metrics.readRecord()){
				values = metrics.getValues();
				
				String key = metrics.get(1);
				key = key.startsWith("_")?key.substring(1):key;
				
				key = key.indexOf("(")==-1? key:key.substring(0, key.indexOf("("));	
				String kind = metrics.get(0);
				
				// ignore Struct type
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
				"Essential,Knots,MaxEssentialKnots,MaxNesting,MinEssentialKnots,RatioCommentToCode";
		
		// create arff file
		Instances instances = new Instances(souceCheckOutDate,WekaUtils.createAttributeInfoWithAttributeNamesForClassfication(header),0);
		
		// dataset structure
		lines.add(instances.toString());
		String[] metricsName = header.split(",");
		
		for(String key:metricsValues.keySet()){
			String newLine = "";
			for(int i=0;i<metricsName.length;i++){
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
		
		FileUtil.writeAFile(lines, pathToSave + File.separator +  souceCheckOutDate + "_" + predictionDate + ".arff");
		
		// generate apiname_list in case of test sets
		if(isTestSetGeneration){
			ArrayList<String> names = new ArrayList<String>();
			for(String key:metricsValues.keySet()){
				names.add(key);
			}
			FileUtil.writeAFile(names, pathToSave + File.separator +  souceCheckOutDate + "_" + predictionDate + "_api_name.txt");
		}
		
		
	}
}

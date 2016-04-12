package net.lifove.research.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.csvreader.CsvReader;

public class DummyClassifierForTestResults {
	
	HashMap<String,ArrayList<String>> passedAPIs = new HashMap<String,ArrayList<String>>();
	HashMap<String,ArrayList<String>> failedAPIs = new HashMap<String,ArrayList<String>>();
	
	ArrayList<String> lstDates = new ArrayList<String>();
	
	/**
	 * Dummy classifier to predict test failures using the previous test failures
	 * @param args
	 */
	public static void main(String[] args) {
		new DummyClassifierForTestResults().run(args);
	}

	private void run(String[] args) {
		String testResultsDataPath = args[0];
		
		CsvReader testResults;
		try {
			testResults = new CsvReader(testResultsDataPath);
			testResults.readHeaders();		
			
			while (testResults.readRecord()){
				
				//only consider the target date
				//if(!testResults.get("date").equals(DateUtil.getNextDate(ddmmyyyyDate,"d/M/yyyy","d/M/yy")))
				//	continue;
				String key = testResults.get("date");
				String apiName = testResults.get("api_name");
				String testResult = testResults.get("testcase_execution_status");
				
				if(!lstDates.contains(key))
					lstDates.add(key);
				
				// initiate
				if(!passedAPIs.containsKey(key))
					passedAPIs.put(key, new ArrayList<String>());
				if(!failedAPIs.containsKey(key))
					failedAPIs.put(key, new ArrayList<String>());
				
				// add api name in the list
				if(testResult.contains("Fail")){
					// Fail result is dominant when having pass result in the same api
					//while(passedAPIs.get(key).remove(apiName)){}
					passedAPIs.get(key).remove(apiName);
					if(!failedAPIs.get(key).contains(apiName))
						failedAPIs.get(key).add(apiName);
				}
				else{
					if(!failedAPIs.get(key).contains(apiName) && !passedAPIs.get(key).contains(apiName)){
						passedAPIs.get(key).add(apiName);
					}
				}
					
			}
			
			System.out.println(failedAPIs.get("27/5/14").size());
			System.out.println(passedAPIs.get("27/5/14").size());
			
			System.out.println("srcDate,tarDate,fmeasure,precision,recall," + 
							"tarInstances,predictedInstances," +
							"unknownPositives,unknownNegatives");
			
			// dummy predictions daily
			for(int i=0;i<lstDates.size()-1;i++){
				String srcDate = lstDates.get(i);
				String tarDate = lstDates.get(i+1);
				
				int numPassedTargetInstances = passedAPIs.get(tarDate).size();
				int numFailedTargetInstances = failedAPIs.get(tarDate).size();
				
				int unknownPositives = 0;
				
				// compute false positive and false negative
				int TP = 0;
				int FN = 0;
				for(String apiName:failedAPIs.get(tarDate)){
					if(failedAPIs.get(srcDate).contains(apiName))
						TP++;
					if(passedAPIs.get(srcDate).contains(apiName))
						FN++;
				}
				
				int unknownNegatives = 0;
				// compute false negative and true negative
				int TN = 0;
				int FP = 0;
				for(String apiName:passedAPIs.get(tarDate)){
					if(passedAPIs.get(srcDate).contains(apiName))
						TN++;
					if(failedAPIs.get(srcDate).contains(apiName))
						FN++;
				}
				
				unknownPositives = numFailedTargetInstances - (TP+FN);
				unknownNegatives = numPassedTargetInstances - (TN+FP);
				
				int tarInstances = numFailedTargetInstances + numPassedTargetInstances;
				int predictedInstances = TP+FN+TN+FP;
				
				double precision = (double)TP/(TP+FP);
				double recall = (double)TP/(TP+FN);
				double fmeasure = (double)2*(precision*recall)/(precision+recall);
				
				System.out.println(srcDate + "," + tarDate + "," + fmeasure +"," + precision + "," + recall + "," + 
								tarInstances + "," +predictedInstances + ","
								+ unknownPositives + "," + unknownNegatives);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

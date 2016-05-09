package net.lifove.research.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * @author JC
 * This class generates a table whose columns and rows list weeks and the number of owners with N (N=1,2,...n)
 *        1   2  3   4  5  6 ...
 * week1
 * week2
 * week3
 * ...
 */
public class OwnerCounter {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		OwnerCounter runner = new OwnerCounter();
		
		String[] newArgs = {"sopra01_2011","data/hm_sopra01_2011.csv","2011-05-01"};
		args = newArgs;
		
		int[] numMembers2011 = {5,5,6,5,5,5,5,5,5,5};
		int[] numMembers2012 = {5,4,5,4,5,2,5,5,5,4};
		
		for(int i=1;i<=10;i++){
			args[0] = "sopra" + String.format("%02d", i) + "_2011";
			args[1] = "data/svnmetrics/hm_sopra" + String.format("%02d", i) + "_2011.csv";
			runner.run(args,numMembers2011[i-1],new SimpleDateFormat("yyyy-MM-dd").parse(args[2]));
		}
		
		for(int i=1;i<=10;i++){
			args[0] = "sopra" + String.format("%02d", i) + "_2012";
			args[1] = "data/svnmetrics/hm_sopra" + String.format("%02d", i) + "_2012.csv";
			args[2] = "2012-04-15";
			runner.run(args,numMembers2012[i-1],new SimpleDateFormat("yyyy-MM-dd").parse(args[2]));
		}
	}
	
	void run(String args[],int numMembers,Date startDate){
		
		String path = args[1];
	
		System.out.println(args[0]);
		
		ArrayList<String> lines = FileUtil.getLines(path, false);
		try {
			
			// display is for accumulated counting
			//display(countOwnership(getRawMetrics(lines),startDate));
			// display2 is independent weekly counting
			display2(countOwnership2(getRawMetrics(lines),startDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}
	
	/**
	 * @param records
	 * 
	 * This method displays the number of files changed by N persons in a week
	 */
	void display2(HashMap<String,OwnerCounterForAFileAndEachWeek> records){
		HashMap<Integer,Integer> counter = new HashMap<Integer,Integer>(); // # of owners is a key, count
		for(int week=1 ; week<17; week++){
			
			for(String path:records.keySet()){
				OwnerCounterForAFileAndEachWeek record = records.get(path);

				//if(!record.ext.equals("cs"))
				/*if(!(record.ext.toLowerCase().equals("jpg")  
						|| record.ext.toLowerCase().equals("png")
						|| record.ext.toLowerCase().equals("bmp")
						|| record.ext.toLowerCase().equals("wav")))*/
				//	continue;
				
				int numOwnersOfThisWeek = 0;
				int targetWeek = week;
				if (!record.ownerCounterWeekly.containsKey(week))
					continue;
	
				numOwnersOfThisWeek = record.ownerCounterWeekly.get(targetWeek);
				
				if(!counter.containsKey(numOwnersOfThisWeek))
					counter.put(numOwnersOfThisWeek,record.ownerCounterWeekly.get(targetWeek));
				else
					counter.put(numOwnersOfThisWeek, counter.get(numOwnersOfThisWeek) + record.ownerCounterWeekly.get(targetWeek));
			}
			
			String result = "week" + week +",";
			
			SortedSet<Integer> keys = new TreeSet<Integer>(counter.keySet());
			
			if(keys.size() > 0)
			for(int numOwners=1; numOwners<= keys.last();numOwners++){
				if(!counter.containsKey(numOwners))
					result += "0,";
				else
					result +=counter.get(numOwners) + ",";
				
			}
			System.out.println(result);
			counter.clear();
		}
		
	}
	
	void display(HashMap<String,OwnerCounterForAFile> records){
		HashMap<Integer,Integer> counter = new HashMap<Integer,Integer>(); // # of owners is a key, count
		for(int week=1 ; week<17; week++){
			
			for(String path:records.keySet()){
				OwnerCounterForAFile record = records.get(path);

				//if(!record.ext.equals("cs"))
				if(!(record.ext.toLowerCase().equals("jpg")  
						|| record.ext.toLowerCase().equals("png")
						|| record.ext.toLowerCase().equals("bmp")
						|| record.ext.toLowerCase().equals("wav")))
					continue;
				
				int numOwnersOfThisWeek = 0;
				int targetWeek = week;
				if (!record.ownerCounterWeekly.containsKey(week)){
					int existDataWeek = -1;
					for(int i = week; i >= 0 ; i--){
						if(record.ownerCounterWeekly.containsKey(i)){
							existDataWeek = i;
							targetWeek = existDataWeek;
							break;
						}
					}
					
					if(existDataWeek >= 1)
						numOwnersOfThisWeek = record.ownerCounterWeekly.get(targetWeek);
					else
						continue;
				}
				else
					numOwnersOfThisWeek = record.ownerCounterWeekly.get(targetWeek);
				
				if(!counter.containsKey(numOwnersOfThisWeek))
					counter.put(numOwnersOfThisWeek,record.ownerCounterWeekly.get(targetWeek));
				else
					counter.put(numOwnersOfThisWeek, counter.get(numOwnersOfThisWeek) + record.ownerCounterWeekly.get(targetWeek));
			}
			
			String result = "week" + week +",";
			
			SortedSet<Integer> keys = new TreeSet<Integer>(counter.keySet());
			
			if(keys.size() > 0)
			for(int numOwners=1; numOwners<= keys.last();numOwners++){
				if(!counter.containsKey(numOwners))
					result += "0,";
				else
					result +=counter.get(numOwners) + ",";
				
			}
			System.out.println(result);
			counter.clear();
		}
		
	}
	
	HashMap<String,OwnerCounterForAFile> countOwnership(ArrayList<RawMetrics> metrics,Date startDate) throws ParseException{
		HashMap<String,OwnerCounterForAFile> counter = new HashMap<String,OwnerCounterForAFile>();
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
		
		for(RawMetrics record:metrics){
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(record.date);
			long dayInterval = (date.getTime() - startDate.getTime())/86400000;
			int week = (int) dayInterval/7 + 1;
			
			if(!counter.containsKey(record.path)){
				counter.put(record.path, new OwnerCounterForAFile(record.ext,record.committer,week));
			}
			else{
				counter.get(record.path).count(record.committer, week);
			}
		}
		return counter;
	}
	
	HashMap<String,OwnerCounterForAFileAndEachWeek> countOwnership2(ArrayList<RawMetrics> metrics,Date startDate) throws ParseException{
		HashMap<String,OwnerCounterForAFileAndEachWeek> counter = new HashMap<String,OwnerCounterForAFileAndEachWeek>();
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
		
		for(RawMetrics record:metrics){
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(record.date);
			long dayInterval = (date.getTime() - startDate.getTime())/86400000;
			int week = (int) dayInterval/7 + 1;
			
			if(!counter.containsKey(record.path)){
				counter.put(record.path, new OwnerCounterForAFileAndEachWeek(record.ext,record.committer,week));
			}
			else{
				counter.get(record.path).count(record.committer, week);
			}
		}
		return counter;
	}
	
	ArrayList<RawMetrics> getRawMetrics(ArrayList<String> lines){
		
		ArrayList<RawMetrics> rawMetrics = new ArrayList<RawMetrics>();
		
		for(String line:lines)
			rawMetrics.add(new RawMetrics(line));
		
		return rawMetrics;
	}

}

class OwnerCounterForAFile{
	String ext="";
	ArrayList<String> owners = new ArrayList<String>();
	HashMap<Integer,Integer> ownerCounterWeekly = new HashMap<Integer,Integer>(); // week, counter
	
	OwnerCounterForAFile(String ext,String committer, int week){
		this.ext = ext;
		count(committer,week);
	}
	
	void count(String committer, int week){
		if(!owners.contains(committer))
			owners.add(committer);
		
		ownerCounterWeekly.put(week, owners.size());
	}
	
	int numOfOwners(){
		return owners.size();
	}
}

class OwnerCounterForAFileAndEachWeek{
	String ext="";
	HashMap<Integer,ArrayList<String>> ownersEachWeek = new HashMap<Integer,ArrayList<String>>();
	HashMap<Integer,Integer> ownerCounterWeekly = new HashMap<Integer,Integer>(); // week, counter
	
	OwnerCounterForAFileAndEachWeek(String ext,String committer, int week){
		this.ext = ext;
		count(committer,week);
	}
	
	void count(String committer, int week){
		if(!ownersEachWeek.containsKey(week)){
			ArrayList<String> owners = new ArrayList<String>();
			owners.add(committer);
			ownersEachWeek.put(week, owners);
		}
		else{
			if(!ownersEachWeek.get(week).contains(committer))
				ownersEachWeek.get(week).add(committer);
		}
			
		
		ownerCounterWeekly.put(week, ownersEachWeek.get(week).size());
	}
	
	int numOfOwners(int week){
		return ownersEachWeek.get(week).size();
	}
}

class RawMetrics{
	String path="";
	String ext;
	String fullDate;
	String date;
	int revision;
	String committer;
	int changedLOC;
	int isCreator;
	double ownership;
	double ownershipHighest;
	int isOwner;
	
	RawMetrics(String line){
		String[] record = line.split(",");
		
		// in case file path includes commas, need to compute the path and others correctly
		int interval = record.length -15;
		for(int i=0;i<interval+1;i++){
			path = path + record[i] +",";
		}
		ext = record[1+interval];
		fullDate = record[2+interval];
		date = record[3+interval];
		revision = Integer.parseInt(record[4+interval]);
		committer = record[5+interval];
		changedLOC = Integer.parseInt(record[7+interval]);
		isCreator = Integer.parseInt(record[11+interval]);
		ownership = Double.parseDouble(record[12+interval]);
		ownershipHighest = Double.parseDouble(record[13+interval]);
		isOwner = Integer.parseInt(record[14+interval]);
	}
}
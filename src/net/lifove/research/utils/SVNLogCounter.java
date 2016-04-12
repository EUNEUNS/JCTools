package net.lifove.research.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class SVNLogCounter {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		String[] myArgs = {"","data/svnmetrics/hm_sopra01_2011.csv"};
		args=myArgs;
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2011-05-01");
		for(int i=1;i<=10;i++){
			args[0] = "sopra" + String.format("%02d", i) + "_2011";
			args[1] = "data/svnmetrics/hm_sopra" + String.format("%02d", i) + "_2011.csv";
			new SVNLogCounter().run(args,startDate);
		}
		
		startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-04-22");
		for(int i=1;i<=10;i++){
			args[0] = "sopra" + String.format("%02d", i) + "_2012";
			args[1] = "data/svnmetrics/hm_sopra" + String.format("%02d", i) + "_2012.csv";
			new SVNLogCounter().run(args,startDate);
		}
	}

	void run(String[] args,Date startDate) throws ParseException{
		String group = args[0];
		String dataPath = args[1];
		ArrayList<String> lines = FileUtil.getLines(dataPath, false);
		HashMap<Integer,DateLog> dateInfo = getDateInfo(lines);
		System.out.println(group + "\n" +weeklyCounter(startDate, dateInfo) + "\n");
	}
	
	String weeklyCounter(Date startDate,HashMap<Integer,DateLog> dateInfo){
		
		HashMap<String,Integer> weeklyCounter = new HashMap<String,Integer>();
		
		//long a = (startDate.getTime() - endDate.getTime())/86400000;
		
		//System.out.println(a);
		
		ArrayList<String> committers = new ArrayList<String>();
		for(Integer revision:dateInfo.keySet()){
			long dayInterval = (dateInfo.get(revision).date.getTime() - startDate.getTime())/86400000;
			int week = (int) dayInterval/7 + 1;
			
			// key : week + committer
			String key= week + dateInfo.get(revision).committer;
			if(!committers.contains(dateInfo.get(revision).committer))
				committers.add(dateInfo.get(revision).committer);
			
			int count = 0;
			if(weeklyCounter.get(key)==null)
				count = 1;
			else
				count = weeklyCounter.get(key)+1;
			
			weeklyCounter.put(key, count);
		}
		
		String result = "";
		
		for(String committer:committers){		
			
			result += committer + ",";
			
			for(int i=1; i<=20;i++){
				String key = i + committer;
				
				if(weeklyCounter.containsKey(key))
					result = result + weeklyCounter.get(key) + ",";
				else
					result = result + 0 + ",";
			}
			result = result + "\n";
		}
		
		return result;
	}
	
	HashMap<Integer,DateLog> getDateInfo(ArrayList<String> lines) throws ParseException{
		HashMap<Integer,DateLog> dateInfo = new HashMap<Integer,DateLog>();
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
		
		int currentRevision = -1;
		
		for(String line:lines){
			String[] record = line.split(",");
			// in case file path includes commas, need to compute the path and others correctly
			int interval = record.length -15;
			String path="",ext="";
			for(int i=0;i<interval+1;i++){
				path = path + record[i] +",";
				ext = path.substring(path.lastIndexOf(".")+1).replace("," ,"");
			}
			
			Integer revision = Integer.parseInt(record[4+interval]);
			
			if(currentRevision!=revision){
				currentRevision = revision;
				
				if(ext.toLowerCase().equals("fbx")){
				/*if(ext.toLowerCase().equals("jpg")  
						|| ext.toLowerCase().equals("png")
						|| ext.toLowerCase().equals("bmp")
						|| ext.toLowerCase().equals("wav")
						){
						*/
				String committer = record[5+interval];
				String strDate = record[3+interval];
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
				
				dateInfo.put(revision,new DateLog(date,committer));
				}
			}
			
			
		}
		return dateInfo;
	}
}

class DateLog{
	Date date;
	String committer;
	
	DateLog(Date date,String committer){
		this.date = date;
		this.committer = committer;
	}
	
}

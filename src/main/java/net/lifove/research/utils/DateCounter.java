package net.lifove.research.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

public class DateCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DateCounter runner =  new DateCounter();
		
		String[] myArgs = {"path","year","startDate"};
		args = myArgs;
		
		for(int i=1; i<11;i++){
			args[0] = "data/dateInfo/sopra" + String.format("%02d", i) + "_email_date_sent.txt";
			args[1] = "2011";
			args[2] = "2011-05-01";
			System.out.println("\nsopra" + String.format("%02d", i) + "_" + args[1]);
			runner.run(args);
		}
		
		for(int i=1; i<11;i++){
			args[0] = "data/dateInfo/sopra" + String.format("%02d", i) + "_email_date_sent.txt";
			args[1] = "2012";
			args[2] = "2012-04-15";
			System.out.println("\nsopra" + String.format("%02d", i) + "_" + args[1]);
			runner.run(args);
		}
	}

	void run(String args[]){
		String path = args[0];
		String year = args[1];
		String startDate = args[2];

		ArrayList<String> lines = FileUtil.getLines(path, false);
		try {
			ArrayList<Date> dates2011 = getDates(lines,year);
			Collections.sort(dates2011);
			
			countMailsWeekly(dates2011, new SimpleDateFormat("yyyy-MM-dd").parse(startDate));
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	void countMailsWeekly(ArrayList<Date> dates,Date startDate){
		HashMap<Integer,Integer> counter = new HashMap<Integer,Integer>();
		
		for(Date date:dates){
			long dayInterval = (date.getTime() - startDate.getTime())/86400000;
			int week = (int) dayInterval/7 + 1;
			
			int currentCount = 0;
			if(counter.containsKey(week))
				currentCount = counter.get(week) + 1;
			else
				currentCount = 1;
			
			counter.put(week, currentCount);		
		}
		
		SortedSet<Integer> keys = new TreeSet<Integer>(counter.keySet());
		for(Integer week: keys){
			System.out.println("week" + week +"," + counter.get(week));
		}
	}
	
	ArrayList<Date> getDates(ArrayList<String> lines, String year) throws ParseException{
		ArrayList<Date> dates = new ArrayList<Date>();
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin")); 
		
		for(String line:lines){
			// Date: Sat, 23 Jun 2012 01:17:41 +0200
			if(line.contains(year)){
				Date date = new SimpleDateFormat("EEE, dd MMM yyyy H:m:s Z").parse(line.replace("Date: ", "").trim());
				dates.add(date);
			}
		}
		return dates;
	}
}

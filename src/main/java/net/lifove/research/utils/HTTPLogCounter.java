package net.lifove.research.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

public class HTTPLogCounter {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		String[] myArgs = {"data/httplogs/t0.2011.log"};
		args=myArgs;
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2011-05-01");
		new HTTPLogCounter().run(args,startDate);
		
		System.out.println();
		
		startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-04-15");
		args[0] = "data/httplogs/t0.2012.log";
		new HTTPLogCounter().run(args,startDate);
	}

	void run(String[] args,Date startDate) throws ParseException{
		String dataPath = args[0];
		ArrayList<String> lines = FileUtil.getLines(dataPath, false);
		HashMap<String,ArrayList<Log>> dateInfo = getDateInfo(lines);
		
		SortedSet<String> keys = new TreeSet<String>(dateInfo.keySet());
		
		
		weeklyCounter(startDate, dateInfo);
		
	}
	
	void weeklyCounter(Date startDate,HashMap<String,ArrayList<Log>> dateInfo){
	
		HashMap<String,Integer> weeklyCounter = new HashMap<String,Integer>();
		
		//long a = (startDate.getTime() - endDate.getTime())/86400000;
		
		//System.out.println(a);
		String result = "";
		SortedSet<String> keys = new TreeSet<String>(dateInfo.keySet());
		ArrayList<String> users = new ArrayList<String>();
		for(String group:keys){
			for(Log log:dateInfo.get(group)){
				long dayInterval = (log.accessDate.getTime() - startDate.getTime())/86400000;
				
				if(dayInterval <0)// || !log.type.equals("POST"))
					continue;
				
				int week = (int) dayInterval/7 + 1;
				
				// key : week + committer
				String key= week + log.user;
				if(!users.contains(log.user))
					users.add(log.user);
				
				int count = 0;
				if(weeklyCounter.get(key)==null)
					count = 1;
				else
					count = weeklyCounter.get(key)+1;
				
				weeklyCounter.put(key, count);
			}
			
			for(String user:users){		
				result += group + ",";
				result += user + ",";
				
				for(int i=1; i<=20;i++){
					String key = i + user;
					
					if(weeklyCounter.containsKey(key))
						result = result + weeklyCounter.get(key) + ",";
					else
						result = result + 0 + ",";
				}
				result = result + "\n";
			}
			
			System.out.println(result);
			users.clear();
			result="";
		}
	}
	
	HashMap<String,ArrayList<Log>> getDateInfo(ArrayList<String> lines) throws ParseException{
		HashMap<String,ArrayList<Log>> dateInfo = new HashMap<String,ArrayList<Log>>(); //group, Log
		
		//TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
		for(String line:lines){
			//remote190-063.home.uni-freiburg.de - lehmanna [01/Jun/2011:11:16:24 +0200] "GET /trac/sopra08/chrome/common/js/trac.js HTTP/1.1" 200 3836
			String[] record = line.split(" ");
			String user = record[2];
			String date = record[3].replace("[", "").replace("]", "");
			String type = record[5].replace("\"","");
			
			String group = "";
			if (record[6].length() >13 && record[6].startsWith("/trac/sopra") )
				group = record[6].substring(6, 13);
			
			// skip not in project url.
			if(group.equals(""))
				continue;
			
			Log newLog = new Log(user,type,date);
			
			if(!dateInfo.containsKey(group)){
				ArrayList<Log> logs = new ArrayList<Log>();
				logs.add(newLog);
				dateInfo.put(group, logs);
			}else{
				dateInfo.get(group).add(newLog);
			}
					
		}
		return dateInfo;
	}
}

class Log{
	String user="";
	String type = "";
	Date accessDate = null;
	
	Log(String user,String type,String date){
		this.user = user;
		this.type = type;
		// 01/Jun/2011:11:16:27 +0200
		try {
			this.accessDate = new SimpleDateFormat("dd/MMMM/yyyy").parse(date.split(":")[0]);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
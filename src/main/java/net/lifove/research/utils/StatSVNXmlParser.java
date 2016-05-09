package net.lifove.research.utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StatSVNXmlParser {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		
		String[] myArgs = {"","data/svnmetrics/hm_sopra01_2011.csv"};
		args=myArgs;
		Date startDate;

		startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2011-05-01");
		for(int i=1;i<=10;i++){
			args[0] = "sopra" + String.format("%02d", i) + "_2011";
			args[1] = "data/statsvn/sopra" + String.format("%02d", i) + "_2011.xml";
			new StatSVNXmlParser().run(args,startDate);
		}
		
		startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-04-22");
		for(int i=1;i<=10;i++){
			args[0] = "sopra" + String.format("%02d", i) + "_2012";
			args[1] = "data/statsvn/sopra" + String.format("%02d", i) + "_2012.xml";
			new StatSVNXmlParser().run(args,startDate);
		}
		
	}
	
	public void run(String[] args,Date startDate){
		String statSvnFilename = args[1];
		ArrayList<SVNCommit> commits = parseStatSVNLog(statSvnFilename);
		System.out.println(weeklyCounter(startDate,commits));
	}
	
	String weeklyCounter(Date startDate,ArrayList<SVNCommit> commits){
		String result="";
		HashMap<String,Integer> counter = new HashMap<String,Integer>(); // key: week+author, value: count
		ArrayList<String> committers = new ArrayList<String>();
		
		for(SVNCommit commit:commits){
			long dayInterval = (commit.date.getTime() - startDate.getTime())/86400000;
			
			//TODO weekly to daily
			int day = (int) dayInterval + 1; // int week = (int) dayInterval/7 + 1;
			String author = commit.author;
			
			if(!committers.contains(author))
				committers.add(author);
			
			String key = day + author; 
			
			if(!counter.containsKey(key)){
				counter.put(key, getAddedLines(commit));
			}
			else{
				counter.put(key, getAddedLines(commit) + counter.get(key));
			}
		}
		
		for(String committer:committers){		
			
			result += committer + ",";
			
			for(int i=1; i<=112;i++){
				String key = i + committer;
				
				if(counter.containsKey(key))
					result = result + counter.get(key) + ",";
				else
					result = result + 0 + ",";
			}
			result = result + "\n";
		}
		
		return result;
	}
	
	Integer getAddedLines(SVNCommit commit){
		int countLines = 0;
		
		for(AffectedFile file:commit.affectedFiles){
			countLines = countLines + file.locAdd;// file.locAdd;
		}
		return countLines;
	}
	
	Integer getRemovedLines(SVNCommit commit){
		int countLines = 0;
		
		for(AffectedFile file:commit.affectedFiles){
			countLines = countLines + file.locRem;
		}
		return countLines;
	}
	
	Integer getChangedLines(SVNCommit commit){
		int countLines = 0;
		
		for(AffectedFile file:commit.affectedFiles){
			countLines = countLines + (file.locAdd - file.locRem);
		}
		return countLines;
	}
	
	ArrayList<SVNCommit> parseStatSVNLog(String statSvnFilename){
		System.out.println("\n=================================");
		System.out.println("History Metrics");
		System.out.println("Target xml file: " + statSvnFilename);
		System.out.println("=================================");
		System.out.println("Extracting metrics...");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		ArrayList<SVNCommit> commits = new ArrayList<SVNCommit>();
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(statSvnFilename);
			Element docEle = dom.getDocumentElement();

			NodeList commitNodes = docEle.getElementsByTagName("Commit");
			 
			for(int i=0;i<commitNodes.getLength();i++){
				Element commit = (Element)commitNodes.item(i);
				commits.add(new SVNCommit(commit));				
			}
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return commits;
	}
}

class SVNCommit {
	Date date;
	String log;
	String author;
	int revisionID;
	ArrayList<AffectedFile> affectedFiles = new ArrayList<AffectedFile>();
	
	public SVNCommit(Element commit){
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin")); 
		
		// 2011-06-01 04:19
		try {
			date = new SimpleDateFormat("yyyy-MM-dd H:m").parse(commit.getAttribute("date"));
			//System.out.println(commit.getAttribute("date") + " " + date.toString());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		author = commit.getAttribute("author");
		log = commit.getElementsByTagName("Comment").item(0).getTextContent();
		String rvsId = commit.getAttribute("revision");
		if (!rvsId.equals(""))
			revisionID = Integer.parseInt(commit.getAttribute("revision"));
		
		NodeList affectedFiles = commit.getElementsByTagName("File");
		for(int i=0; i < affectedFiles.getLength(); i++){
			String action,path;
			int locAdd=0, locRem=0;
			
			action = ((Element)affectedFiles.item(i)).getAttribute("action");
			path =  ((Element)affectedFiles.item(i)).getElementsByTagName("Path").item(0).getTextContent();
			try{
				locAdd =  Integer.parseInt(((Element)affectedFiles.item(i)).getElementsByTagName("LocAdd").item(0).getTextContent());
			}catch(NullPointerException e){
				
			}
			try{
				locRem =  Integer.parseInt(((Element)affectedFiles.item(i)).getElementsByTagName("LocRem").item(0).getTextContent());
			}catch(NullPointerException e){
				
			}
			addAffectedFile(path,action,locAdd,locRem);
		}
	}
	
	void addAffectedFile(String path,String action,int locAdd, int locRem){
		affectedFiles.add(new AffectedFile(path,action,locAdd,locRem));
	}

	public Date getDate() {
		return date;
	}

	public String getLog() {
		return log;
	}

	public String getAuthor() {
		return author;
	}

	public int getRevisionID() {
		return revisionID;
	}

	public ArrayList<AffectedFile> getAffectedFiles() {
		return affectedFiles;
	}
}

class AffectedFile{
	String path;
	String action;
	int locAdd;
	int locRem;
	
	AffectedFile(String path,String action,int locAdd, int locRem){
		this.path = path;
		this.action = action;
		this.locAdd = locAdd;
		this.locRem = locRem;
	}

	public String getPath() {
		return path;
	}

	public String getAction() {
		return action;
	}

	public int getLocAdd() {
		return locAdd;
	}

	public int getLocRem() {
		return locRem;
	}
}
package net.lifove.research.utils.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

public class SVNCommit {
	long revisionID;
	String author="";
	String logMessage;
	ArrayList<String> bugIDs = new ArrayList<String>();
	ArrayList<String> ignoringWordsForBugID;
	HashMap<String,String> dictionaryForWrongBugID;
	
	public SVNCommit(Element commit,ArrayList<String> ignoringWordsForBugID,HashMap<String,String> dictionaryForWrongBugID){
		revisionID = Integer.parseInt(commit.getAttribute("revision"));
		if(commit.getElementsByTagName("author").item(0) != null)
			author = commit.getElementsByTagName("author").item(0).getTextContent();
		
		logMessage = commit.getElementsByTagName("msg").item(0).getTextContent();
		
		this.ignoringWordsForBugID = ignoringWordsForBugID;
		this.dictionaryForWrongBugID = dictionaryForWrongBugID;
		
		extractBugID();
		
	}
	
	void extractBugID(){
		Pattern severityPattern = Pattern.compile("([a-zA-Z]+-[0-9]+)");
	    Matcher matcher;
	    
		matcher = severityPattern.matcher(logMessage);

		if(matcher.groupCount()>1){
			System.out.println(matcher.groupCount() + "EXIT");
			System.exit(0);
		}
		
		while(matcher.find()){
			// group 1 returns corresponding texts for the pattern and make to upper case
			String bugID = matcher.group(1).toUpperCase();
			
			System.out.println("BUGID: " + bugID + "-" + revisionID + ":" + logMessage);
			String bugIDPrefix = bugID.split("-")[0];
			String bugIDNum = bugID.split("-")[1];
			
			// deal with typos for bug id prefix
			if(dictionaryForWrongBugID.containsKey(bugIDPrefix))
				bugID = dictionaryForWrongBugID.get(bugIDPrefix) + "-" + bugIDNum;
			
			// ignore commits, which have bug-like ids.
			if(!ignoringWordsForBugID.contains(bugIDPrefix))
				bugIDs.add(bugID);
		}
	}
	
	public ArrayList<String> getBugIDs(){
		return bugIDs;
	}
	
	public long getRevisionID(){
		return revisionID;
	}
	
	public String getLogMessage(){
		return logMessage;
	}
}

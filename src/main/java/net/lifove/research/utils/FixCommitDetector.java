package net.lifove.research.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.lifove.research.utils.datamodel.SVNCommit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author JC
 * Fix commit detector from bug IDs (IssueType=Bug,Resolution=Fixed)
 */
public class FixCommitDetector {
	
	boolean CONSIDERFIXKEYWORD = false;

	/**
	 * @param args args[0]: svn log in xml, args[1], Jira bug report information in xls
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FixCommitDetector runner = new FixCommitDetector();
		
		/*String[] subjects={"camel",
							"cayenne",
							"cxf",
							"derby",
							"felix",
							"hadoopCommon",
							"hbase",
							"hive",
							"lucene",
							"openejb",
							"wicket",
							"xercesj"
						};*/
		String[] subjects={"jackrabbit_kim",
				"lucene_kim",
				"httpclient_kim"
			};
		
		for(int i=0;i<subjects.length;i++){
			String[] temp =  {"data/svnlogs/apache/" + subjects[i].replace("_kim", "") +".xml.log",
								"data/bugReport/" + subjects[i] +".id",
								subjects[i]};
			args = temp;
			runner.run(args);
		}
	}
	
	void run(String[] args){
		
		System.out.println("#==" + args[2]);
		
		// Long: Reivsion #, String: bug id
		HashMap<String,ArrayList<SVNCommit>> bugIDsInCommitMessage;
		ArrayList<String> bugIDs = new ArrayList<String>();
		ArrayList<String> fixCommits = new ArrayList<String>();
		ArrayList<String> ignoringWordsForBugID = FileUtil.getLines("data/ignorewords.txt", false);
		ignoringWordsForBugID.add("FIXCOMMIT"); // consider commits which do not have any bug IDs but the keyword `fix'
		HashMap<String,String> dictionaryForWrongBugID = FileUtil.getHashMap("data/dictionaryForWrongBugIDs.txt", true, ":");
		
		bugIDsInCommitMessage = loadSVNLogMessage(args[0],ignoringWordsForBugID,dictionaryForWrongBugID);
		bugIDs = loadBugReportInfo(args[1]);
		int counterForLinkedBugs = 0;
		int counterForDislinkedBugs = 0;
		for(String bugID:bugIDs){
			
			if(bugIDsInCommitMessage.containsKey(bugID)){
				for(SVNCommit commit:bugIDsInCommitMessage.get(bugID)){
					fixCommits.add(commit.getRevisionID() + "");
				}
				counterForLinkedBugs++;
			}
			else{
				System.out.println("!!! no commit for this bug id: " + bugID);
				counterForDislinkedBugs++;
			}
		}
		
		// save fix commits into a file
		FileUtil.writeAFile(fixCommits, "data/fixCommits/" + args[2] +".fix");
		
		System.out.println("#BugIDs Fixed: " + bugIDs.size());
		System.out.println("#Linked BugID to commit: " + counterForLinkedBugs);
		System.out.println("#DisLinked BugID to commit: " + counterForDislinkedBugs);
	}

	HashMap<String,ArrayList<SVNCommit>> loadSVNLogMessage(String svnLogFile,ArrayList<String> ignoringWordsForBugID,HashMap<String,String> dictionaryForWrongBugID){
		HashMap<String,ArrayList<SVNCommit>> bugIDsInCommitMessage = new HashMap<String,ArrayList<SVNCommit>>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(svnLogFile);
			Element docEle = dom.getDocumentElement();

			NodeList commitNodes = docEle.getElementsByTagName("logentry");
			
			
			int counterAllCommitsConsidered = 0;
			int counterCommitsWithIssueID = 0;
			int counterCommitsWithFixKeyword = 0;
			for(int i=0;i<commitNodes.getLength();i++){
				Element commit = (Element)commitNodes.item(i);
				SVNCommit curCommit = new SVNCommit(commit,ignoringWordsForBugID,dictionaryForWrongBugID);
				
				/*if(curCommit.getRevisionID() > 757686)
					counterAllCommitsConsidered++;
				else
					continue;
				*/
				
				counterAllCommitsConsidered++;
				
				if(curCommit.getBugIDs().size()>0){
					counterCommitsWithIssueID++;
				
					for(String bugID:curCommit.getBugIDs()){
						if(!bugIDsInCommitMessage.containsKey(bugID)){
							ArrayList<SVNCommit> commits= new ArrayList<SVNCommit>();
							commits.add(curCommit);
							bugIDsInCommitMessage.put(bugID,commits);
						}
						else{
							bugIDsInCommitMessage.get(bugID).add(curCommit);
							
							if(curCommit.getBugIDs().size()>0)
								System.out.println("%%% Duplicated commits for the same bug id: " + curCommit.getRevisionID() + "(" + curCommit.getLogMessage().replace("\n","\n%%%") + ")," +
									bugIDsInCommitMessage.get(bugID).get(0).getRevisionID() + ": " + bugIDsInCommitMessage.get(bugID).get(0).getLogMessage().replace("\n","\n%%%"));
					
							//System.exit(0);
						}
					}
				}
				// no bug ids in commit log, FIXCOMMIT, include commit which have `fix keyword'
				else if(CONSIDERFIXKEYWORD==true){
					
					Pattern fixKeywordPattern = Pattern.compile("(^|\\s)fix|bugfix|quickfix");
				    Matcher matcher;
				    
				    matcher = fixKeywordPattern.matcher(curCommit.getLogMessage().toLowerCase());
					
					if(matcher.find()){
						counterCommitsWithFixKeyword++;
						String bugID = "FIXCOMMIT";
						
						System.out.println("*** FIX KEYWORD: "+ curCommit.getLogMessage().replace("\n", "\n***"));
						
						if(!bugIDsInCommitMessage.containsKey(bugID)){
							ArrayList<SVNCommit> commits= new ArrayList<SVNCommit>();
							commits.add(curCommit);
							bugIDsInCommitMessage.put(bugID,commits);
						}
						else
							bugIDsInCommitMessage.get(bugID).add(curCommit);
					}
				}
			}
			System.out.println("#All commits I have: " + counterAllCommitsConsidered);
			System.out.println("#commits with IssueID: " + counterCommitsWithIssueID);
			System.out.println("#commits with fix keyword: " + counterCommitsWithFixKeyword);
			
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return bugIDsInCommitMessage;
	}
	
	ArrayList<String> loadBugReportInfo(String bugReportInfo){
		return FileUtil.getLines(bugReportInfo, false);
	}
}
package net.lifove.research.weka.lpuutil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Line 
{
	private String path;
	private long revision;
	private int[] aBuggyLines;
	
	public Line(String path, long revision, int buggyLine)
	{
		init(path, revision);
		this.aBuggyLines = new int[1];
		this.aBuggyLines[0] = buggyLine;
	}
	
	public Line(String path, long revision, int[] aBuggyLines)
	{
		init(path, revision);
		this.aBuggyLines = new int[aBuggyLines.length];
		for (int i = 0; i < this.aBuggyLines.length; i++)
			this.aBuggyLines[i] = aBuggyLines[i];
	}
	
	public Line(String[] contents)
	{
		init(contents);
	}
	
	public Line(String[] contents, int beginIdx, int endIdx)
	{
		int size = endIdx - beginIdx + 1;
		String[] croppedContents = new String[size];
		for (int i = 0; i < croppedContents.length; i++)
			croppedContents[i] = contents[i + beginIdx];
		
		init(croppedContents);
	}
	
	public Line(String path, long revision, List<Integer> aBuggyLines)
	{
		init(path, revision, aBuggyLines);
	}
	
	public Line(String path, long revision, Set<Integer> aBuggyLines)
	{
		List<Integer> tempList = new ArrayList<Integer>();
		tempList.addAll(aBuggyLines);
		Collections.sort(tempList);
		
		init(path, revision, tempList);
	}
	
	private void init(String[] contents)
	{
		this.path = contents[0];
		this.revision = Integer.parseInt(contents[1]);
		int noBuggyLines = Integer.parseInt(contents[2]);
		this.aBuggyLines = null;
		
		if (noBuggyLines == -1)
		{
			List<Integer> buggyLineList = new ArrayList<Integer>();
			for (int i = 3; i < contents.length; i++)
			{
				try {
					int readInt = Integer.parseInt(contents[i]);
					buggyLineList.add(readInt);
				} catch (NumberFormatException e) {
				}
			}
			
			this.aBuggyLines = new int[buggyLineList.size()];
			for (int i = 0; i < this.aBuggyLines.length; i++)
				this.aBuggyLines[i] = buggyLineList.get(i);
		}
		else
		{
			this.aBuggyLines = new int[noBuggyLines];
			
			for (int i = 0; i < this.aBuggyLines.length; i++)
				this.aBuggyLines[i] = Integer.parseInt(contents[i + 3]);
		}
	}
	
	private void init(String path, long revision, List<Integer> aBuggyLines)
	{
		init(path, revision);
		if (aBuggyLines.size() <= 0)
			this.aBuggyLines = null;
		
		this.aBuggyLines = new int[aBuggyLines.size()];
		for (int i = 0; i < this.aBuggyLines.length; i++)
			this.aBuggyLines[i] = aBuggyLines.get(i);
	}
	
	private void init(String path, long revision)
	{
		this.path = path;
		this.revision = revision;
	}
	
	public void addLine(int buggyLine)
	{
		for (int i = 0; i < this.aBuggyLines.length; i++)
		{
			if (this.aBuggyLines[i] == buggyLine)
				return;
		}
		
		List<Integer> aPrvBuggyLines = new ArrayList<Integer>();
		for (int i = 0; i < this.aBuggyLines.length; i++)
			aPrvBuggyLines.add(this.aBuggyLines[i]);
		
		aPrvBuggyLines.add(buggyLine);
		
		this.aBuggyLines = null;
		this.aBuggyLines = new int[aPrvBuggyLines.size()];
		for (int i = 0; i < this.aBuggyLines.length; i++)
			this.aBuggyLines[i] = aPrvBuggyLines.get(i);
	}
	
	public void setPath(String newPath) { this.path = newPath; }
	public void setRevision(long revision) { this.revision = revision; }
	public void setBuggyLines(int[] aBuggyLines) {
		this.aBuggyLines = aBuggyLines;
	}
	public void setBuggyLines(int buggyLine) {
		this.aBuggyLines = null;
		this.aBuggyLines = new int[1];
		this.aBuggyLines[0] = buggyLine;
	}

	public String getPath() { return this.path; }
	public long getRevision() { return this.revision; }
	public int[] getBuggyLines() { return this.aBuggyLines; }
	
	public void setHit() { this.revision = 1; }
	public void setMiss() { this.revision = 0; }
	
	public void setEmpty()
	{
		this.path = null;
		this.revision = -1;
		this.aBuggyLines = null;
	}
	
	public boolean isEmpty()
	{
		if (this.path == null || this.aBuggyLines == null || this.aBuggyLines.length <= 0)
			return true;
		return false;
	}
	
	public String toString() 
	{
		if (isEmpty() == true)
			return null;
		
		String returnStr = path + "," + revision + "," + this.aBuggyLines.length;
		for (int i = 0; i < this.aBuggyLines.length; i++)
			returnStr += "," + this.aBuggyLines[i];
		
		return returnStr;
	}
	
	public boolean isMatched(Line other)
	{
		if (this.getPath().compareTo(other.getPath()) != 0)
			return false;
		
		if (this.getRevision() != other.getRevision())
			return false;
		
		return true;
	}
	
	public Line findIdenticalFile(List<Line> lines)
	{
		for (Line curr: lines)
		{	
			if (curr.getPath().compareTo(this.getPath()) == 0 && curr.getRevision() == this.getRevision())
				return curr;
		}
		
		return null;
	}
	
	public boolean isSuperset(Line other)
	{
		for (int currLine: this.getBuggyLines())
		{
			boolean bFound = false;
			for (int otherLine: other.getBuggyLines())
			{
				if (currLine == otherLine)
				{
					bFound = true;
					break;
				}
			}
			
			if (bFound == false)
				return false;
		}
		
		return true;
	}
	
	public boolean hasIntersection(Line other)
	{
		if (this.isEmpty() == true || other.isEmpty() == true)
			return false;
		
		for (int currLine: this.getBuggyLines())
		{
			for (int otherLine: other.getBuggyLines())
			{
				if (currLine == otherLine)
					return true;
			}
		}
		
		return false;
	}
	
	public boolean hasIntersectionPathRev(Line other)
	{
		if (isMatched(other) == false)
			return false;
		
		for (int currLine: this.getBuggyLines())
		{
			for (int otherLine: other.getBuggyLines())
			{
				if (currLine == otherLine)
					return true;
			}
		}
		
		return false;
	}
	
	public boolean hasIntersection(int[] otherLines)
	{
		if (this.isEmpty() == true || otherLines == null)
			return false;
		
		for (int currLine: this.getBuggyLines())
		{
			for (int otherLine: otherLines)
			{
				if (currLine == otherLine)
					return true;
			}
		}
		
		return false;
	}
	
	public static Line extractFromArff(String line)
	{
		String content = line.substring(1, line.length() - 1);
		String[] contents = content.split(",");
		
		String path = null;
		long revision;
		int linePos;
		// path
		{
			String[] innerContents = contents[0].split(" ");
			path = innerContents[1].substring(1, innerContents[1].length() - 1);
		}
		
		// revision
		{
			String[] innerContents = contents[1].split(" ");
			revision = Long.parseLong(innerContents[1]);
		}
		
		// line pos
		{
			String[] innerContents = contents[2].split(" ");
			linePos = Integer.parseInt(innerContents[1]);
		}
		
		return new Line(path, revision, linePos);
	}
	
	public static boolean contains(List<Line> lines, Line chkLine)
	{
		for (Line curr: lines)
		{
			if (curr.getPath().compareTo(chkLine.getPath()) != 0)
				continue;
			
			if (curr.getRevision() != chkLine.getRevision())
				continue;
			
			if (curr.getBuggyLines().length != chkLine.getBuggyLines().length)
				continue;
			
			boolean bMatched = true;
			for (int i = 0; i < curr.getBuggyLines().length; i++)
			{
				if (curr.getBuggyLines()[i] != chkLine.getBuggyLines()[i])
				{
					bMatched = false;
					break;
				}
			}
			
			if (bMatched == true)
				return true;
		}
		
		return false;
	}

	public static ArrayList<Line> loadFile(String path)
	{
		ArrayList<Line> lineInfo = new ArrayList<Line>();
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				// ignore null line
				if (thisLine.startsWith("null") == true)
					continue;
				
				String[] contents = thisLine.split("\\,");
				
				lineInfo.add(new Line(contents));
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lineInfo;
	}
	
	public static ArrayList<Line> loadFileWithMerge(String path)
	{
		ArrayList<Line> lineInfo = new ArrayList<Line>();
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				// ignore null line
				if (thisLine.startsWith("null") == true)
					continue;
								
				String[] aBuggyLineInfo = thisLine.split("\\,");	
				
				String srcPath = aBuggyLineInfo[0];
				long revision = Integer.parseInt(aBuggyLineInfo[1]);
				int noBuggyLines = Integer.parseInt(aBuggyLineInfo[2]);
				int[] aBuggyLines = new int[noBuggyLines];
				
				for (int i = 0; i < aBuggyLines.length; i++)
					aBuggyLines[i] = Integer.parseInt(aBuggyLineInfo[i + 3]);
				
				Line newLine = new Line(srcPath, revision, aBuggyLines);
				mergeLine(newLine, lineInfo);
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lineInfo;
	}
	
	public static List<Line> loadFileOldestRevision(String path)
	{
		List<Line> lineInfo = new ArrayList<Line>();
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				// ignore null line
				if (thisLine.startsWith("null") == true)
					continue;
				
				String[] contents = thisLine.split("\\,");
				Line newLine = new Line(contents);
				
				if (updateOldestRevision(lineInfo, newLine) == false)
					lineInfo.add(newLine);
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lineInfo;
	}
	
	private static boolean updateOldestRevision(List<Line> lines, Line newLine)
	{
		for (Line curr: lines)
		{
			if (curr.getPath().compareTo(newLine.getPath()) != 0)
				continue;
			
			if (curr.getBuggyLines().length != newLine.getBuggyLines().length)
				continue;
			
			boolean isIdentical = true;
			for (int i = 0; i < curr.getBuggyLines().length; i++)
			{
				if (curr.getBuggyLines()[i] != newLine.getBuggyLines()[i])
				{
					isIdentical = false;
					break;
				}
			}
			
			if (isIdentical == true)
			{
				if (newLine.getRevision() < curr.getRevision())
					curr.setRevision(newLine.getRevision());
				return true;
			}
		}
		
		return false;
	}
	
	public boolean mergeLine(Line newLine, int around)
	{
		if (newLine.isEmpty() == true)
			return false;
		
		if (this.getPath().compareTo(newLine.getPath()) != 0)
			return false;
		
		if (this.getRevision() != newLine.getRevision())
			return false;
		
		if (checkAround(this, newLine, around) == false)
			return false;
		
		for (int currLine: newLine.getBuggyLines())
			this.addLine(currLine);
		
		newLine.setEmpty();
		return true;
	}
	
	public static boolean checkAround(Line line, Line newLine, int around)
	{	
		for (int currLine: line.getBuggyLines())
		{
			for (int newCurrLine: newLine.getBuggyLines())
			{
				if (Math.abs(currLine - newCurrLine) <= around)
					return true;
			}
		}
		
		return false;
	}
	
	private static void mergeLine(Line newLine, List<Line> lines)
	{
		for (Line currLine: lines)
		{
			if (currLine.getPath().compareTo(newLine.getPath()) == 0 && currLine.getRevision() == newLine.getRevision())
			{
				for (int buggyLine: newLine.getBuggyLines())
					currLine.addLine(buggyLine);
				
				return;
			}
		}
		lines.add(newLine);
	}
	
	public static void separateLines(List<Line> lines)
	{
		int oriSize = lines.size();
		
		for (int i = 0; i < oriSize; i++)
		{
			Line curr = lines.get(i);
			if (curr.getBuggyLines().length <= 1)
				continue;
			
			int firstLine = curr.getBuggyLines()[0];
			for (int j = 1; j < curr.getBuggyLines().length; j++)
				lines.add(new Line(curr.getPath(), curr.getRevision(), curr.getBuggyLines()[j]));
			
			curr.setBuggyLines(firstLine);
		}
	}
	
	public static List<Line> beforeRevision(List<Line> lines, long revision)
	{
		List<Line> outputLines = new ArrayList<Line>();
		for (Line curr: lines)
		{
			if (curr.getRevision() < revision)
				outputLines.add(curr);
		}
		
		return outputLines;
	}
	
	public static Line extractFindBugsLine(String line, String packageKeyword, long revision)
	{
		int pos = findFindBugsFilePos(line);
		if (pos == -1)
			return null;
		
		String fileWithLine = line.substring(pos);
		int posLine = fileWithLine.lastIndexOf(":[line");
		if (posLine == -1)
			return null;
		
		String srcPath = null;
		if (packageKeyword != null)
		{
			srcPath = extractFindBugsLine(line, fileWithLine.substring(0, posLine), 
				packageKeyword);
		}
		else
			srcPath = fileWithLine.substring(0, posLine);
		
		if (srcPath == null)
			return null;
		
		String lineStr = fileWithLine.substring(posLine);
		
		int[] aBuggyLines = getFindBugsLines(lineStr);
		return new Line(srcPath, revision, aBuggyLines);
	}
	
	public static String extractFindBugsLine(String line, String srcFile, String packageKeyword)
	{
		String[] fileContents = srcFile.split("\\.");
		if (fileContents.length != 2)
			return null;
		
		String filename = fileContents[0];
		
		String[] lineContents = line.split(" ");
		
		for (String curr: lineContents)
		{
			if (curr.indexOf(packageKeyword) == -1)
				continue;
			
			if (curr.indexOf("." + filename + ".") == -1 &&
					curr.indexOf("." + filename + "$") == -1)
				continue;
			
			int pos = curr.indexOf("." + filename);
			return curr.substring(0, pos + 1).replaceAll("\\.", "/") + srcFile;
		}
		
		return null;
	}
	
	public static ArrayList<Line> loadJlintResult(String path, List<Line> fullLines, long revision)
	{
		ArrayList<Line> lineInfo = new ArrayList<Line>();
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				int posLine = thisLine.indexOf(".java:");
				if (posLine == -1)
					continue;
				
				int posLineEnd = thisLine.indexOf(":", posLine + 6);
				int posSrc = thisLine.substring(0, posLine).lastIndexOf("/bigstore/");
				
				String srcPath = thisLine.substring(posSrc, posLine + 5);
				String lineStr = thisLine.substring(posLine + 6, posLineEnd);
				
				int[] aBuggyLines = new int[1];
				
				try {
					aBuggyLines[0] = Integer.parseInt(lineStr);
				} catch (Exception e) {
					System.out.println(thisLine);
				}
				
				String refinedSrcPath = refineJlintSrcPath(srcPath, fullLines);
				if (refinedSrcPath == null)
					System.out.println(srcPath);
				
				lineInfo.add(new Line(refinedSrcPath, revision, aBuggyLines));
				
				/*
				String srcPath = fileWithLine.substring(0, posLine);
				String lineStr = fileWithLine.substring(posLine);
				
				int[] aBuggyLines = getLines(lineStr);
				lineInfo.add(new Line(srcPath, 0, aBuggyLines));
				*/
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lineInfo;
	}
	
	public static ArrayList<Line> loadPmdResult(String path, List<Line> fullLines, 
			String packageKeyword, long revision)
	{
		ArrayList<Line> lineInfo = new ArrayList<Line>();
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String firstLine = br.readLine();
			String[] columnInfo = splitPMDInfo(firstLine);
			
			int posPath = findPos(columnInfo, "File");
			int posLine = findPos(columnInfo, "Line");
			
			if (posPath == -1 || posLine == -1)
			{
				System.out.println("cannot find path or line position");
				br.close();
				return null;
			}
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				Line newLine = extractPMDLine(thisLine, posPath, posLine, fullLines, 
						packageKeyword, revision);
				/*
				String[] contents = splitPMDInfo(thisLine);
				
				String srcPath = contents[posPath];
				int buggyLine = Integer.parseInt(contents[posLine]);
				
				String refinedSrcPath = refinePMDSrcPath(srcPath, fullLines);
				if (refinedSrcPath == null)
					continue;
					
				lineInfo.add(new Line(refinedSrcPath, revision, buggyLine));
				*/
				if (newLine == null)
					continue;
								
				lineInfo.add(newLine);
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lineInfo;
	}
	
	public static Line extractPMDLine(String line, int posPath, int posLine, 
			List<Line> fullLines, String packageKeyword,long revision)
	{
		String[] contents = splitPMDInfo(line);
		
		String srcPath = contents[posPath];
		int buggyLine = 0;
		try {
			buggyLine = Integer.parseInt(contents[posLine]);
		} catch (NumberFormatException e) {
			return null;
		}
		
		String refinedSrcPath = refinePMDSrcPath(srcPath, packageKeyword, fullLines);
		if (refinedSrcPath == null)
			return null;
		
		return new Line(refinedSrcPath, revision, buggyLine);
	}
	
	public static String[] splitPMDInfo(String line)
	{
		String[] contents = line.split("\",\"");
		String[] newContents = new String[contents.length];
		for (int i = 0; i < contents.length; i++)
			newContents[i] = contents[i].replaceAll("\"", "");
		
		return newContents;
	}
	
	public static int findPos(String[] contents, String keyword)
	{
		for (int i = 0; i < contents.length; i++)
		{
			if (contents[i].compareToIgnoreCase(keyword) == 0)
				return i;
		}
		
		return -1;
	}
	
	private static String refinePMDSrcPath(String srcPath, String packageKeyword, 
			List<Line> fullLines)
	{
		int pos = srcPath.indexOf(packageKeyword);
		if (pos == -1)
		{
			System.out.println("Out of interest: " + srcPath);
			return null;
		}
		
		String afterKeyword = srcPath.substring(pos);
//		System.out.println("After trunk: " + afterTrunk);
		for (Line curr: fullLines)
		{
			if (curr.getPath().indexOf(afterKeyword) != -1)
				return curr.getPath();
		}
		
		return null;
	}
	
	private static String refineJlintSrcPath(String srcPath, List<Line> fullLines)
	{
		String afterTrunk = srcPath.substring(srcPath.indexOf("/trunk/"));
		int posTarget = afterTrunk.indexOf("/target/");
		if (posTarget == -1)
			return srcPath;
		
		int posClasses = afterTrunk.substring(posTarget).indexOf("classes/");
		
		String targetPath = null;
		if (posClasses != -1)
			targetPath = afterTrunk.substring(posTarget).substring(posClasses + 8);
		else
		{
			int posFile = afterTrunk.lastIndexOf('/');
			targetPath = afterTrunk.substring(posFile);
		}
		
		String targetDir = afterTrunk.substring(0, posTarget);
		
		for (Line curr: fullLines)
		{
			if (curr.getPath().indexOf(targetPath) != -1 &&
					curr.getPath().indexOf(targetDir) != -1)
			{
				return curr.getPath();
			}
		}
		
		return null;
	}
	
	private static int findFindBugsFilePos(String line)
	{
		int posAt = line.lastIndexOf(" At ");
		int posIn = line.lastIndexOf(" In ");
		
		if (posAt == -1 && posIn == -1)
			return -1;
		
		int pos;
		if (posAt == -1)
			pos = posIn;
		else if (posIn == -1)
			pos = posAt;
		else
		{
			if (posAt < posIn)
				pos = posIn;
			else
				pos = posAt;
		}
		
		return (pos + 4);
	}
	
	private static int[] getFindBugsLines(String line)
	{
		int pos = line.lastIndexOf("line ");
		int lastPos = line.length() - 1;
		int[] returnLines = null;
		if (pos != -1)
		{
			pos = line.lastIndexOf(' ') + 1;
			returnLines = new int[1];
			returnLines[0] = Integer.parseInt(line.substring(pos, lastPos));
		}
		else
		{
			pos = line.lastIndexOf(' ') + 1;
			String[] lines = line.substring(pos, lastPos).split("-");
			
			int lineBegin = Integer.parseInt(lines[0]);
			int lineEnd = Integer.parseInt(lines[1]);
			
			returnLines = new int[lineEnd - lineBegin + 1];
			for (int i = 0; i < returnLines.length; i++)
				returnLines[i] = lineBegin + i;
		}
		return returnLines;
	}
	
	public static void writeFile(String path, List<Line> aLines)
	{
		if (path == null)
			return;
		
		try {
			File file= new File(path);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			for (Line line: aLines)
			{
				if (line.isEmpty() == true)
					continue;
				
				dos.write((line.toString() + "\n").getBytes());
			}
			
			//dos.writeBytes();
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static List<String> loadRelativeLinesOnly(String path, List<Line> aLines)
	{
		List<String> selectedLines = new ArrayList<String>();
		String thisLine = null;
		
		boolean bFoundData = false;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				if (bFoundData == false && thisLine.toLowerCase().indexOf("@data") != -1)
				{
					selectedLines.add(thisLine);
					bFoundData = true;
					continue;
				}
				
				if (bFoundData == false)
				{
					selectedLines.add(thisLine);
					continue;
				}
				
				Line currLine = extractLineFromARFF(thisLine);
				for (Line line: aLines)
				{
					if (currLine.isMatched(line) == false)
						continue;
					
					if (currLine.hasIntersection(line) == true)
					{
						selectedLines.add(thisLine);
						break;
					}
				}
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			return null;
		}
		
		return selectedLines;
	}
	
	public static List<Line> loadLinesFromARFF(String path, long revision)
	{
		List<Line> selectedLines = new ArrayList<Line>();
		String thisLine = null;
		
		boolean bFoundData = false;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				if (bFoundData == false && thisLine.toLowerCase().indexOf("@data") != -1)
				{
					bFoundData = true;
					continue;
				}
				
				if (bFoundData == false)
				{
					continue;
				}
				
				Line currLine = extractLineFromARFF(thisLine);
				if (currLine.getRevision() == revision)
					selectedLines.add(currLine);
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			return null;
		}
		
		return selectedLines;
	}
	
	private static Line extractLineFromARFF(String line)
	{
		final int POS_PATH	= 0;
		final int POS_REV	= 1;
		final int POS_LINE	= 2;
		
		final int POS_CONTENT	= 1;
		
		if (line.length() <= 2)
			return null;
		
		String trimmedLine = line.substring(1, line.length() - 1);
		String[] contents = trimmedLine.split(",");
		
		String pathWithColon = contents[POS_PATH].split(" ")[POS_CONTENT];
		String path = pathWithColon.substring(1, pathWithColon.length() - 1);
		
		long revision = Long.parseLong(contents[POS_REV].split(" ")[POS_CONTENT]);
		int lineNumber = Integer.parseInt(contents[POS_LINE].split(" ")[POS_CONTENT]);
		
		return new Line(path, revision, lineNumber);
	}
}

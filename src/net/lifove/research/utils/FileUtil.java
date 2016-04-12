package net.lifove.research.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FileUtil {
	
	static public ArrayList<String> getLines(String file,boolean removeHeader){
		ArrayList<String> lines = new ArrayList<String>();
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				lines.add(thisLine);
			} // end while 
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
			//System.exit(0);
		}
		
		if(removeHeader)
			lines.remove(0);
		
		return lines;
	}
	
	static public ArrayList<String> getStringInASpecificColumn(String file,boolean removeHeader,int index){
		ArrayList<String> lines = new ArrayList<String>();
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				String[] columns = thisLine.split(",");
				lines.add(columns[index]);
			} // end while 
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
			//System.exit(0);
		}
		
		if(removeHeader)
			lines.remove(0);
		
		return lines;
	}
	
	/**
	 * @param file file name, from which we generate HashTable. The file should have two columns divided by any splitter.
	 * @param removeHeader The file may have a header (the first line) and can be excluded by "removeHeader" parameter, true.
	 * @param divider Splitter such as ",", ":", and so on.
	 * @return HashMap<String,String>
	 */
	static public HashMap<String,String> getHashMap(String file,boolean removeHeader,String divider){
		HashMap<String,String> hashMap = new HashMap<String,String>();
		ArrayList<String> lines = getLines(file,removeHeader);
		
		for(String line:lines){
			String[] keyAndValue = line.split(divider);
			hashMap.put(keyAndValue[0], keyAndValue[1]);
		}
		
		return hashMap;
	}
	
	static ArrayList<String> loadListOfFiles(String path){
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		File dir = new File(path);
		
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		};
		
		File[] files = dir.listFiles(fileFilter);
		
		for(File file:files){
			String fileName = file.getName();
			listOfFiles.add(fileName);
		}
		
		return listOfFiles;
	}
	
	public static ArrayList<String> loadListOfAllsubDirs(File dir) {
		ArrayList<String> dirs = new ArrayList<String>();
		
		if (dir.isDirectory()) {
			dirs.add(dir.getPath());
			String[] children = dir.list();		
			for (int i=0; i<children.length; i++) {
				dirs.addAll(loadListOfAllsubDirs(new File(dir, children[i])));
			}
		}
		return dirs;
	}
	
	public static String getFirstLine(String file){
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			thisLine = br.readLine();
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return thisLine;
	}
	
	static public ArrayList<String> getArffHeaderLines(String arffFile){
		ArrayList<String> lines = new ArrayList<String>();
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(arffFile));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				if(thisLine.startsWith("@"))
					lines.add(thisLine);
			} // end while 
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lines;
	}
	
	static public ArrayList<String> getArffBodyLines(String arffFile){
		ArrayList<String> lines = new ArrayList<String>();
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(arffFile));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				if(!thisLine.startsWith("@") && !thisLine.trim().equals("") && !thisLine.startsWith("%"))
					lines.add(thisLine);
			} // end while 
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}	
		return lines;
	}
	
	public static void writeAFile(ArrayList<String> lines, String targetFileName){
		try {
			File file= new File(targetFileName);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			for(String line:lines){
				dos.write((line+"\n").getBytes());
			}
			//dos.writeBytes();
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void writeAFile(String lines, String targetFileName){
		try {
			File file= new File(targetFileName);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			dos.writeBytes(lines);
				
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	static public void print(ArrayList<String> lines,int targetColumn){
		for(String line:lines){
			if(targetColumn >= 0 )
				System.out.println(line.split(",")[targetColumn]);
			else
				System.out.println(line);
		}
	}
}

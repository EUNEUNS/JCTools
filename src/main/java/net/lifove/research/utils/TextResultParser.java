package net.lifove.research.utils;

import java.io.File;
import java.util.ArrayList;

public class TextResultParser {

	/**
	 * This class extract required values in txt or csv files
	 * @param args: 0: target directory (retrieve all files under the target dir), 1: n fields (valid lines's requirement), 2: # of lines to get (should meet the requirements)
	 */
	public static void main(String[] args) {
		
		if(args.length == 3)
			new TextResultParser().run(args);
		else
			System.out.println("USAGE\n0: target directory (retrieve all files under the target dir), 1: n fields (valid lines's requirement), 2: # of lines to get (should meet the requirements)");
	}
	
	void run(String[] args){
		String targetDir = args[0];
		int numOfFieldsToBeValidLine = Integer.parseInt(args[1]);
		int numOfLinesToGet = Integer.parseInt(args[2]);
		
		ArrayList<String> finalLines = new ArrayList<String>();
		
		//Retrieve all fines under the targetDir
		ArrayList<String>  subdirs = FileUtil.loadListOfAllsubDirs(new File(targetDir));
		
		boolean headeradded = false;
		for(String dir:subdirs){
			String dirName  = dir.substring(dir.lastIndexOf(File.separator)+1);
			for(String file:FileUtil.loadListOfFiles(dir)){
				ArrayList<String> lines = FileUtil.getLines(dir + File.separator + file, false);
				
				String shortFileName = file;
				
				int i=0;
				for(String line:lines){
					String[] fields = line.split(",");
					if(fields.length<numOfFieldsToBeValidLine)
						continue;
					
					// add head
					if(!headeradded){
						finalLines.add("Metrics,Project," + line);
						headeradded = true;
					}
					else{
						if(i>0){
							// add records
							finalLines.add(dirName + "," + shortFileName +"," +line);
						}
					}
					i++;
					
					if(i==numOfLinesToGet)
						break;
				}
				
			}
		}
		
		for(String line:finalLines){
			System.out.println(line);
		}
	}

}
